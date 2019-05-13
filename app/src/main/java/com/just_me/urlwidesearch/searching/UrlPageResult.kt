package com.just_me.urlwidesearch.searching

/**
 * Base type for [UrlPageTask] result of parsing one page. Contains all information about task.
 * @param url - url of parsed page
 * @param id - id of task
 * @param searchingText - text that was searching on the page
 */
sealed class UrlPageResult(open val url: String, open val id: Int, open val searchingText: String) {

  override fun equals(other: Any?): Boolean {
	 return when(other) {
		is UrlPageResult -> {
		  this.id == other.id && this.url == other.url && this.searchingText == other.searchingText
		}
		else -> false
	 }
  }

  /**
	* Will returned in case of no errors occurred.
	* @param foundedPlaces - list of positions where [searchingText] was found.
	* @param foundedUrls - list of urls to look further.
	*/
  data class SucceedUrlPageResult(
	 override val url: String, override val id: Int, override val searchingText: String,
	 val foundedPlaces: List<Int>, val foundedUrls: List<String>
  ) : UrlPageResult(url, id, searchingText) {
	 override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as SucceedUrlPageResult

		if (url != other.url) return false
		if (id != other.id) return false
		if (searchingText != other.searchingText) return false
		if (foundedPlaces != other.foundedPlaces) return false
		if (foundedUrls != other.foundedUrls) return false

		return true
	 }

	 override fun hashCode(): Int {
		var result = url.hashCode()
		result = 31 * result + id
		result = 31 * result + searchingText.hashCode()
		result = 31 * result + foundedPlaces.hashCode()
		result = 31 * result + foundedUrls.hashCode()
		return result
	 }
  }

  /**
	* Will returned in case some error occurred.
	* @param throwable - error that was occurred.
	*/
  data class FailedUrlPageResult(
	 override val url: String, override val id: Int, override val searchingText: String, val throwable: Throwable
  ) :
	 UrlPageResult(url, id, searchingText)
}