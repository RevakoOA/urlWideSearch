package com.just_me.urlwidesearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.just_me.urlwidesearch.searching.SearchResult
import com.just_me.urlwidesearch.searching.SearchUpdate
import kotlinx.android.synthetic.main.frag_result.*
import kotlin.math.roundToInt

class ResultFragment : Fragment() {

  private lateinit var adapter: UrlsAdapter
  private lateinit var viewModel: MainActivityViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
	 super.onCreate(savedInstanceState)
	 viewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
	 adapter = UrlsAdapter()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
	 return inflater.inflate(R.layout.frag_result, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
	 super.onViewCreated(view, savedInstanceState)
	 pauseResume.setOnClickListener {
		if (pauseResume.text.toString() == context!!.getString(R.string.pause)) {
		  viewModel.pause()
		  pauseResume.setText(R.string.resume)
		} else if (pauseResume.text.toString() == context!!.getString(R.string.resume)) {
		  viewModel.resume()
		  pauseResume.setText(R.string.pause)
		}
	 }
	 cancel.setOnClickListener {
		if (cancel.text.toString() == context!!.getString(R.string.cancel)) {
		  viewModel.cancelAllTasks()
		  pauseResume.setText(R.string.canceled)
		  cancel.setText(R.string.go_back)
		} else {
		  fragmentManager?.popBackStack()
		}
	 }
	 recyclerParcedUrls.layoutManager = LinearLayoutManager(context)
	 recyclerParcedUrls.adapter = adapter
  }

  fun updateProgress(updateResult: SearchUpdate) {
	 progresBar.progress = (updateResult.progress * 100).roundToInt()
	 adapter.submitList(updateResult.currentResults)
	 adapter.notifyDataSetChanged()
  }

  fun finishedSearching(result: SearchResult) {
	 updateProgress(SearchUpdate(1f, result.results))
	 pauseResume.setText(R.string.finished)
	 cancel.setText(R.string.go_back)
  }

  override fun onDestroy() {
	 super.onDestroy()
	 viewModel.cancelAllTasks()
  }

  companion object {
	 val TAG: String = ResultFragment::class.java.simpleName
  }
}