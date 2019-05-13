package com.just_me.urlwidesearch

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.just_me.urlwidesearch.searching.SearchController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val mAvailableProcessors = Runtime.getRuntime().availableProcessors()

  private lateinit var viewModel: MainActivityViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
	 super.onCreate(savedInstanceState)
	 setContentView(R.layout.activity_main)
	 viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
	 maxTreads.hint = String.format(getString(R.string.hint_max_threads), mAvailableProcessors)
//	 startUrl.text.replace(0, 0, "http://www.google.com/search?q=urban")
//	 textToSearch.text.replace(0, 0, "urban")
//	 maxTreads.text.replace(0, 0, (mAvailableProcessors).toString())
//	 maxUrlCounts.text.replace(0, 0, 2000.toString())

	 startSearch.setOnClickListener {
		hideKeyboard()
		val startUrl = startUrl.text.toString()
		val textToSearch = textToSearch.text.toString()
		val maxTreads = maxTreads.text.toString().toInt()
		val maxUrlCounts = maxUrlCounts.text.toString().toInt()
		val fragment = ResultFragment()
		viewModel.initController(application, maxTreads, maxUrlCounts)
		viewModel.searchController?.search(startUrl, textToSearch, fragment::updateProgress, fragment::finishedSearching)
		supportFragmentManager.beginTransaction()
		  .replace(R.id.container, fragment, ResultFragment.TAG)
		  .addToBackStack(ResultFragment.TAG)
		  .commit()
	 }
  }

  private fun hideKeyboard() {
	 val view = this.currentFocus
	 view?.let { v ->
		val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
		imm?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
	 }
  }
}
