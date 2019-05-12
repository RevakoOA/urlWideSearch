package com.just_me.urlwidesearch

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.volley.Cache
import com.android.volley.Request.Method.GET
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import com.just_me.urlwidesearch.SearchController.Companion.TUPICAL_URL_PATTERN
import java.io.File
import java.lang.Exception
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean


class SearchController(app: Application, val maxThread: Int, val pagesLimit: Int) {

  val cache: Cache = DiskBasedCache(File(app.cacheDir, "Volley"))
  val queue: RequestQueue by lazy {
	 return@lazy RequestQueue(cache, BasicNetwork(HurlStack()), maxThread).apply {
		start()
	 }
  }
  val executor: ExecutorService by lazy { return@lazy Executors.newFixedThreadPool(maxThread) }
  val pageTasksList: MutableList<UrlPageTask> = mutableListOf()
  val pageResultsList: MutableList<UrlPageResult> = mutableListOf()
  var finishedCallback: (SearchResult) -> Unit = {}

  fun search(
	 startUrl: String, textToSearch: String, updateCallback: (finishedId: Int) -> Unit,
	 finishingCallback: (SearchResult) -> Unit
  ) {
	 queue.cancelAll { true }
//TODO cancel each task in pageTasksList
	 pageTasksList.clear()
	 this.finishedCallback = finishingCallback
	 pageTasksList.add(UrlPageTask(queue, startUrl, 0, textToSearch, this::unifiedHandler))
	 runAllAvailableTasks()
  }

  private fun unifiedHandler(urlPageResult: UrlPageResult) {
	 if (pageResultsList.find { it.url == urlPageResult.url } != null) return
	 pageResultsList.add(urlPageResult)
	 when (urlPageResult) {
		is UrlPageResult.SucceedUrlPageResult -> {
		  val availableSlots = pagesLimit - pageTasksList.count()
		  pageTasksList.addAll(urlPageResult.foundedUrls.take(availableSlots).map { url ->
			 UrlPageTask(queue, url, pageTasksList.last().id + 1, urlPageResult.searchingText, this::unifiedHandler)
		  })
		}
		is UrlPageResult.FailedUrlPageResult -> {

		}
	 }
	 runAllAvailableTasks()

	 if (isAllTasksFinished()) {
		finishedCallback(SearchResult(pageResultsList))
	 }
  }

  fun isAllTasksFinished(): Boolean {
	 return pageTasksList.find { !it.isDone } == null
  }

  fun runAllAvailableTasks() {
	 val availableTasks = pageTasksList.filter {
		!it.isProccedToExecuting.get()
	 }
	 availableTasks.forEach {
		it.isProccedToExecuting.set(true)
		executor.execute(it)
	 }
  }

  companion object {
	 private val TAG: String = SearchController::class.java.simpleName
	 val TUPICAL_URL_PATTERN = Regex(
		"(http://)([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*")
  }
}

class SearchResult(val results: List<UrlPageResult>) {
}

typealias Callback = (UrlPageResult) -> Unit

class UrlPageTask(
  val queue: RequestQueue,
  val url: String,
  val id: Int,
  val searchingText: String,
  val callback: Callback
) : Runnable {

  val isProccedToExecuting: AtomicBoolean = AtomicBoolean(false);
  val _isDone = AtomicBoolean(false)
  val isDone: Boolean
	 get() = _isDone.get()
  var isCancelled: Boolean = false
	 private set
  var isPaused: AtomicBoolean = AtomicBoolean(false)

  @WorkerThread
  override fun run() {
	 try {
		sleepIfNeed();
		val response = makeRequest(url);
		sleepIfNeed();
		val foundedPlaces = parseResponse(response, searchingText)
		sleepIfNeed();
		val foundedUrls = parseResponseUrls(response, TUPICAL_URL_PATTERN)
		sleepIfNeed();
		_isDone.set(true)
		callback(UrlPageResult.SucceedUrlPageResult(url, id, searchingText, foundedPlaces, foundedUrls))
	 } catch (e: Throwable) {
		sleepIfNeed();
		_isDone.set(true)
		callback(UrlPageResult.FailedUrlPageResult(url, id, searchingText, e))
	 }
  }

  @WorkerThread
  private fun sleepIfNeed() {
	 while (isPaused.get()) {
		Thread.sleep(200);
	 }
  }

  fun pause() = isPaused.set(true)

  fun resume() = isPaused.set(false)

  private fun makeRequest(url: String, timeOut: Long = 100): String {
	 val requestFuture = RequestFuture.newFuture<String>();
	 val request = StringRequest(GET, url, requestFuture, requestFuture)
	 queue.add(request)
	 try {
		return requestFuture.get(timeOut, TimeUnit.SECONDS)
	 } catch (e: Throwable) {
		throw e
	 }
  }

  companion object {
	 private val TAG: String = UrlPageTask::class.java.simpleName

	 @VisibleForTesting
	 fun parseResponse(body: String, searchingText: String, ignoreCase: Boolean = false): List<Int> {
		var foundPosition = body.indexOf(searchingText, 0);
		val listOfFoundPlaces = mutableListOf<Int>()
		try {
		  while (foundPosition != -1) {
			 listOfFoundPlaces.add(foundPosition)
			 foundPosition = body.indexOf(searchingText, foundPosition + 1, ignoreCase);
		  }
		} catch (ignored: Exception) {
		}

		return listOfFoundPlaces
	 }

	 /**
	  * Returns a list with all links contained in the input
	  */
	 @VisibleForTesting
	 fun parseResponseUrls(text: String, pattern: Regex): List<String> {
		return pattern.findAll(text).toList().map {
		  it.value
		}
	 }
  }
}

sealed class UrlPageResult(val url: String, val id: Int, val searchingText: String) {
  class SucceedUrlPageResult(
	 url: String, id: Int, searchingText: String,
	 val foundedPlaces: List<Int>, val foundedUrls: List<String>
  ) : UrlPageResult(url, id, searchingText)

  class FailedUrlPageResult(url: String, id: Int, searchingText: String, val throwable: Throwable) :
	 UrlPageResult(url, id, searchingText)
}