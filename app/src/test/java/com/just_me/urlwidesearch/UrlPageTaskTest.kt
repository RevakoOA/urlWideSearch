package com.just_me.urlwidesearch

import org.junit.Test

import org.junit.Assert.*

class UrlPageTaskTest {

  @Test
  fun parseResponse() {
    val task = UrlPageTask("", 0, "x");
    val result = task.parseResponse("body text bodyil, dody", "body");
    assert(result.size == 2)
    assert(result[0] == 0)
    assert(result[1] == 10)
  }

  @Test
  fun parseResponseUrls() {
    val task = UrlPageTask("", 0, "x");
    val result = task.parseResponseUrls(" http://trello.com text thttp://gravity.no " +
       "bodyil, http://google.com.ua ", "http://");
    assert(result.size == 2)
    assert(result[0] == "http://trello.com")
    assert(result[1] == "http://google.com.ua")
  }
}