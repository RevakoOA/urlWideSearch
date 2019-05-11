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
import java.lang.reflect.Modifier.PRIVATE
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object SearchController {

  private val TAG: String = SearchController::class.java.simpleName

  lateinit var cache: Cache
  lateinit var queue: RequestQueue
  lateinit var executor: ExecutorService


  fun init(app: Application) {
	 val cacheDir = File(app.cacheDir, "Volley")
	 cache = DiskBasedCache(cacheDir)
  }

  fun search(startUrl: String, textToSearch: String, maxThread: Int, maxPages: Int) {
	 if (this::queue.isInitialized) {
		queue.cancelAll { true }
	 } else {
		queue = RequestQueue(cache, BasicNetwork(HurlStack()), maxThread)
	 }
	 if (this::executor.isInitialized) {
		executor.shutdown()
	 }
	 executor = Executors.newFixedThreadPool(maxThread)
  }

}

typealias Callback = (UrlPageResult) -> Unit

class UrlPageTask(val url: String, val id: Int, val searchingText: String): Callable<UrlPageResult> {

  var isDone: Boolean = false
  	private set
  var isCancelled: Boolean = false
  	private set
  private val callbackList: List<Callback> = mutableListOf()

  fun addCallback(callback: Callback) {
	 if (isDone) {

	 }
  }

  @WorkerThread
  override fun call(): UrlPageResult {
	 try {
		val response = makeRequest(url);
		val foundedPlaces = parseResponse(response, searchingText)
		val foundedUrls = parseResponseUrls(response, "http://")
		return UrlPageResult.SucceedUrlPageResult(url, id, searchingText, foundedPlaces, foundedUrls)
	 } catch (e: Throwable) {
		return UrlPageResult.FailedUrlPageResult(e)
	 }
  }

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
		  foundPosition = body.indexOf(searchingText, foundPosition+1, ignoreCase);
		}
	 } catch (ignored: Exception) {}

	 return listOfFoundPlaces
  }

  @VisibleForTesting
  fun parseResponseUrls(body: String, urlPattern: String): List<String> {
		return body.split(" ", ignoreCase = true).filter { it.startsWith(urlPattern) }
  }

  companion object{
	 private val TAG: String = UrlPageTask::class.java.simpleName
  }
}

sealed class UrlPageResult {
  data class SucceedUrlPageResult(val url: String, val id: Int, val searchingText: String,
											 val foundedPlaces: List<Int>, val foundedUrls: List<String>): UrlPageResult()
  data class FailedUrlPageResult(val throwable: Throwable): UrlPageResult()
}