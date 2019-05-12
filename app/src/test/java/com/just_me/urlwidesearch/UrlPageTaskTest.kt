package com.just_me.urlwidesearch

import org.junit.Test

import org.junit.Assert.*

class UrlPageTaskTest {

  @Test
  fun parseResponse() {
	 val result = UrlPageTask.parseResponse("body text bodyil, dody", "body");
	 assert(result.size == 2)
	 assert(result[0] == 0)
	 assert(result[1] == 10)
  }

  @Test
  fun parseResponseUrls() {
	 val result = UrlPageTask.parseResponseUrls(
		" http://www.trello.com text zhttp://gravity.no " +
			"bodyil, >http://google.com.ua ", SearchController.TUPICAL_URL_PATTERN
	 )
	 assert(result.size == 3)
	 assert(result[0] == "http://www.trello.com")
	 assert(result[1] == "http://gravity.no")
	 assert(result[2] == "http://google.com.ua")
  }
}