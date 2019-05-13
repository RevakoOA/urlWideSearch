package com.just_me.urlwidesearch.searching

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

typealias Callback = (UrlPageResult) -> Unit

/**
 * [UrlPageTask] is a long operation that should be run in a @WorkerThread.
 * During this task page will be loaded from internet as string and parsed for urls and [searchingText]
 * To get the result from this operation use [callback]. Task can be paused and resumed, but this will not be done
 * immediately.
 */
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
  var isCancelled = AtomicBoolean(false)
	 private set
  var isPaused: AtomicBoolean = AtomicBoolean(false)

  @WorkerThread
  override fun run() {
	 try {
		sleepIfNeed()
		val response = makeRequest(url)
		sleepIfNeed()
		val foundedPlaces =
		  parseResponse(response, searchingText)
		sleepIfNeed()
		val foundedUrls =
		  parseResponseUrls(response, SearchController.SIMPLE_URL_PATTERN)
		sleepIfNeed()
		_isDone.set(true)
		finishWith(
		  UrlPageResult.SucceedUrlPageResult(
			 url,
			 id,
			 searchingText,
			 foundedPlaces,
			 foundedUrls
		  )
		)
	 } catch (e: Throwable) {
		sleepIfNeed();
		_isDone.set(true)
		finishWith(UrlPageResult.FailedUrlPageResult(url, id, searchingText, e))
	 }
  }

  @WorkerThread
  private fun sleepIfNeed() {
	 while (isPaused.get()) {
		Thread.sleep(200);
	 }
  }

  private fun finishWith(urlPageResult: UrlPageResult) {
	 if (isCancelled.get()) return
	 callback(urlPageResult)
  }

  fun pause() = isPaused.set(true)

  fun resume() = isPaused.set(false)

  // After cancel no callback will be running
  fun cancel() = isCancelled.set(true)

  private fun makeRequest(url: String, timeOut: Long = 100): String {
	 val requestFuture = RequestFuture.newFuture<String>();
	 val request = StringRequest(Request.Method.GET, url, requestFuture, requestFuture)
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