package com.just_me.urlwidesearch

import com.just_me.urlwidesearch.searching.SearchController
import com.just_me.urlwidesearch.searching.UrlPageTask
import org.junit.Test

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
			"bodyil, >http://google.com.ua ", SearchController.SIMPLE_URL_PATTERN
	 )
	 assert(result.size == 3)
	 assert(result[0] == "http://www.trello.com")
	 assert(result[1] == "http://gravity.no")
	 assert(result[2] == "http://google.com.ua")
  }
}