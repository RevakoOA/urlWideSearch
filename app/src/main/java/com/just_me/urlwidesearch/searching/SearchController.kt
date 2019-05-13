package com.just_me.urlwidesearch.searching

import android.app.Application
import android.os.Handler
import com.android.volley.Cache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*
import java.io.File
import java.util.concurrent.*


/**
 * [SearchController] works with thread pool and provide wide searching in the graph on web pages.
 * @param maxThread - threads will be created in ThreadPool.
 * @param pagesLimit - how many pages should be analyzed before stop if there will be more.
 */
class SearchController(app: Application, val maxThread: Int, val pagesLimit: Int) {

  var mainHandler = Handler(app.mainLooper)
  val cache: Cache = DiskBasedCache(File(app.cacheDir, "Volley"))
  val queue: RequestQueue by lazy {
	 return@lazy RequestQueue(cache, BasicNetwork(HurlStack()), maxThread).apply {
		start()
	 }
  }
  val executor: ExecutorService by lazy { return@lazy Executors.newFixedThreadPool(maxThread) }
  /**
	* List of tasks to compute. Size never exceed [pagesLimit].
	*/
  private val pageTasksList: MutableList<UrlPageTask> = mutableListOf()

  private val pageTasksFutures: MutableMap<Int, Future<*>> = mutableMapOf()

  /**
	* Accumulator of results for all tasks.
	*/
  private val pageResultsList: MutableList<UrlPageResult> = mutableListOf()
  var updateCallback: (SearchUpdate) -> Unit = {}
  var finishedCallback: (SearchResult) -> Unit = {}

  /**
	* Searching entry.
	* @param startUrl - root url to start search from.
	* @param textToSearch - text you want to find.
	* @param updateCallback - callback for updating UI about current progress.
	* @param finishingCallback - callback that notifies about finishing searching with final result.
	*/
  fun search(
	 startUrl: String, textToSearch: String, updateCallback: (SearchUpdate) -> Unit,
	 finishingCallback: (SearchResult) -> Unit
  ) {
	 cancelAllTasks()
	 this.finishedCallback = finishingCallback
	 this.updateCallback = updateCallback
	 pageTasksList.add(
		UrlPageTask(
		  queue,
		  startUrl,
		  0,
		  textToSearch,
		  this::unifiedHandler
		)
	 )
	 runAllAvailableTasks()
  }

  /**
	* Provided to each [UrlPageTask] as result callback.
	*/
  private fun unifiedHandler(urlPageResult: UrlPageResult) {
	 if (pageResultsList.find { it.url == urlPageResult.url } != null) return
	 pageResultsList.add(urlPageResult)
	 when (urlPageResult) {
		is UrlPageResult.SucceedUrlPageResult -> {
		  val availableSlots = pagesLimit - pageTasksList.count()
		  var taskId = pageTasksList.last().id
		  pageTasksList.addAll(urlPageResult.foundedUrls.take(availableSlots).map { url ->
			 taskId += 1
			 UrlPageTask(
				queue,
				url,
				taskId,
				urlPageResult.searchingText,
				this::unifiedHandler
			 )
		  })
		}
		is UrlPageResult.FailedUrlPageResult -> {

		}
	 }
	 runAllAvailableTasks()
	 val progress = pageTasksList.filter { it.isDone }.count().toFloat() / pagesLimit
	 mainHandler.post {
		updateCallback(SearchUpdate(progress, pageResultsList))
	 }
	 if (isAllTasksFinished()) {
		mainHandler.post {
		  finishedCallback(SearchResult(pageResultsList))
		}
	 }
  }

  private fun isAllTasksFinished(): Boolean {
	 return pageTasksList.find { !it.isDone } == null
  }

  /**
	* Add all tasks to the thread pool where they will be treated in order of adding.
	*/
  fun runAllAvailableTasks() {
	 val availableTasks = pageTasksList.filter {
		!it.isProccedToExecuting.get()
	 }
	 availableTasks.forEach {
		it.isProccedToExecuting.set(true)
		pageTasksFutures[it.id] = executor.submit(it)
	 }
  }

  fun pauseAlltasks() {
	 pageTasksList.forEach { it.pause() }
  }

  fun resumeAlltasks() {
	 pageTasksList.forEach { it.resume() }
  }

  fun cancelAllTasks() {
	 queue.cancelAll { true }
	 pageTasksList.forEach {
		it.cancel()
	 }
	 pageTasksFutures.forEach {
		it.value.cancel(true)
	 }
	 pageTasksFutures.clear()
  }

  companion object {
	 private val TAG: String = SearchController::class.java.simpleName
	 val SIMPLE_URL_PATTERN = Regex(
		"(http://)([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*"
	 )
  }
}

class SearchResult(val results: List<UrlPageResult>)

/**
 * @param progress - parsed pages out of pages limit.
 */
class SearchUpdate(val progress: Float, val currentResults: List<UrlPageResult>)