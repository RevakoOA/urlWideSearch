package com.just_me.urlwidesearch

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.volley.Cache
import com.android.volley.Request.Method.GET
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import java.io.File
import java.lang.Exception
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean


class SearchController(app: Application, val maxThread: Int, val pagesLimit: Int) {

  private val TAG: String = SearchController::class.java.simpleName

  val cache: Cache = DiskBasedCache(File(app.cacheDir, "Volley"))
  val queue: RequestQueue by lazy { RequestQueue(cache, BasicNetwork(HurlStack()), maxThread) }
  val executor: ExecutorService by lazy { return@lazy Executors.newFixedThreadPool(maxThread) }
  val pageTasksList: MutableList<UrlPageTask> = mutableListOf()
  val pageResultsList: MutableList<UrlPageResult> = mutableListOf()

  fun search(
	 startUrl: String, textToSearch: String, updateCallback: (finishedId: Int) -> Unit,
	 finishingCallback: (SearchResult) -> Unit
  ) {
	 queue.cancelAll { true }

	 pageTasksList.clear()
	 pageTasksList.add(UrlPageTask(startUrl, 0, textToSearch, this::unifiedHandler))
	 runAllAvailableTasks()
  }

  private fun unifiedHandler(urlPageResult: UrlPageResult) {
	 if (pageResultsList.find { it.url == urlPageResult.url } != null) return
	 pageResultsList.add(urlPageResult)
	 when (urlPageResult) {
		is UrlPageResult.SucceedUrlPageResult -> {
		  val availableSlots = pagesLimit - pageTasksList.count()
			 pageTasksList.addAll(urlPageResult.foundedUrls.take(availableSlots).map { url ->
				UrlPageTask(url, pageTasksList.last().id + 1, urlPageResult.searchingText, this::unifiedHandler)
			 })
		}
		is UrlPageResult.FailedUrlPageResult -> {

		}
	 }
	 runAllAvailableTasks()
  }

  fun runAllAvailableTasks() {
	 pageTasksList.filter {
		!it.isProccedToExecuting.get()
	 }.forEach {
		it.isProccedToExecuting.set(true)
		executor.execute(it)
	 }
  }

}

class SearchResult(val results: List<UrlPageResult>) {
}

typealias Callback = (UrlPageResult) -> Unit

class UrlPageTask(val url: String, val id: Int, val searchingText: String, val callback: Callback) : Runnable {

  val isProccedToExecuting: AtomicBoolean = AtomicBoolean(false);

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
		val foundedUrls = parseResponseUrls(response, "http://")
		sleepIfNeed();
		callback(UrlPageResult.SucceedUrlPageResult(url, id, searchingText, foundedPlaces, foundedUrls))
	 } catch (e: Throwable) {
		sleepIfNeed();
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

  private fun makeRequest(url: String, timeOut: Long = 10): String {
	 val requestFuture = RequestFuture.newFuture<String>();
	 val request = StringRequest(GET, url, requestFuture, requestFuture)
	 try {
		return requestFuture.get(timeOut, TimeUnit.SECONDS)
	 } catch (e: Throwable) {
		throw e
	 }
  }

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

  @VisibleForTesting
  fun parseResponseUrls(body: String, urlPattern: String): List<String> {
	 return body.split(" ", ignoreCase = true).filter { it.startsWith(urlPattern) }
  }

  companion object {
	 private val TAG: String = UrlPageTask::class.java.simpleName
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