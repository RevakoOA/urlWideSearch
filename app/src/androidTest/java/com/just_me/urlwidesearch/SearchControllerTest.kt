package com.just_me.urlwidesearch

import android.app.Application
import android.content.Context
import org.junit.Test
import androidx.test.core.app.ApplicationProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import com.just_me.urlwidesearch.searching.SearchController
import com.just_me.urlwidesearch.searching.UrlPageTask
import java.io.File

class SearchControllerTest {

  @Test
  fun search() {
	 val app = (ApplicationProvider.getApplicationContext() as Application)
	 val controller = SearchController(app, 3, 100)
	 val syncObject = Object()
	 controller.search("http://www.google.com/search?q=urban", "urban", {}, {
		synchronized(syncObject) {
		  syncObject.notify();
		}
	 })
	 synchronized(syncObject) {
		syncObject.wait();
	 }
  }

  @Test
  fun volleyTest() {
// Instantiate the RequestQueue.
	 val queue = Volley.newRequestQueue(ApplicationProvider.getApplicationContext())
	 val url = "http://www.google.com"
	 val syncObject = Object()

// Request a string response from the provided URL.
	 val stringRequest = StringRequest(
		Request.Method.GET, url,
		Response.Listener<String> { response ->
		  // Display the first 500 characters of the response string.
		  assert(response.isNotBlank())
		  synchronized(syncObject) {
			 syncObject.notify();
		  }
		},
		Response.ErrorListener {
		  throw it
		})

// Add the request to the RequestQueue.
	 queue.add(stringRequest)
	 synchronized(syncObject) {
		syncObject.wait();
	 }
  }

  @Test
  fun volleySyncTest() {
// Instantiate the RequestQueue.
	 val queue = Volley.newRequestQueue(ApplicationProvider.getApplicationContext())
	 val url = "http://www.google.com"

	 val futureRequest = RequestFuture.newFuture<String>();

// Request a string response from the provided URL.
	 val stringRequest = StringRequest(
		Request.Method.GET, url, futureRequest, futureRequest
	 )

// Add the request to the RequestQueue.
	 queue.add(stringRequest)

	 assert(futureRequest.get().isNotBlank())
  }

  @Test
  fun volleySyncTestWithCustomQueue() {
// Instantiate the RequestQueue.
	 val app = (ApplicationProvider.getApplicationContext() as Application)
	 val cache = DiskBasedCache(File(app.cacheDir, "Volley"))
	 val queue = RequestQueue(cache, BasicNetwork(HurlStack()), 3).apply {
		start()
	 }
	 val url = "http://www.google.com"

	 val futureRequest = RequestFuture.newFuture<String>();

// Request a string response from the provided URL.
	 val stringRequest = StringRequest(
		Request.Method.GET, url, futureRequest, futureRequest
	 )

// Add the request to the RequestQueue.
	 queue.add(stringRequest)

	 assert(futureRequest.get().isNotBlank())
  }

  @Test
  fun findUrlsInResponseBig() {
	 val appContext = ApplicationProvider.getApplicationContext<Context>()
	 val tupicalGoogleResponse = RawReader.readRawFile(appContext, R.raw.tupical_google_page)
	 val urlsList = UrlPageTask.parseResponseUrls(tupicalGoogleResponse, SearchController.SIMPLE_URL_PATTERN)
	 assert(urlsList.size == 15)
  }

  @Test
  fun findUrlsInResponseSmall() {
	 val appContext = ApplicationProvider.getApplicationContext<Context>()
	 val tupicalGoogleResponse = RawReader.readRawFile(appContext, R.raw.tupical_my_ip_page)
	 val urlsList = UrlPageTask.parseResponseUrls(tupicalGoogleResponse, SearchController.SIMPLE_URL_PATTERN)
	 assert(urlsList.size == 5)
  }

}