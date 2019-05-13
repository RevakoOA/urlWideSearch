package com.just_me.urlwidesearch

import android.app.Application
import androidx.lifecycle.ViewModel
import com.just_me.urlwidesearch.searching.SearchController

class MainActivityViewModel : ViewModel() {
  var searchController: SearchController? = null
	 private set(value) {
		field?.cancelAllTasks()
		field = value
	 }

  fun initController(app: Application, maxTreads: Int, maxUrlCounts: Int) {
	 searchController = SearchController(app, maxTreads, maxUrlCounts)
  }

  fun cancelAllTasks() {
	 searchController?.cancelAllTasks()
  }

  fun pause() {
	 searchController?.pauseAlltasks()
  }

  fun resume() {
	 searchController?.resumeAlltasks()
  }
}