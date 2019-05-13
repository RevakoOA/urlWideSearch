package com.just_me.urlwidesearch

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.just_me.urlwidesearch.searching.UrlPageResult
import java.lang.IndexOutOfBoundsException

class UrlsAdapter : ListAdapter<UrlPageResult, UrlsAdapter.UrlItemViewHolder>(DIFF_CALLBACK) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlItemViewHolder {
	 val inflater = LayoutInflater.from(parent.context)
	 return UrlItemViewHolder(inflater.inflate(R.layout.item_result, parent, false))
  }

  override fun onBindViewHolder(holder: UrlItemViewHolder, position: Int) {
	 try {
		getItem(position)?.let {
		  holder.bind(it)
		}
	 } catch (ignored: IndexOutOfBoundsException) {}
  }

  class UrlItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
	 val vId: TextView = itemView.findViewById(R.id.vId)
	 val vUrl: TextView = itemView.findViewById(R.id.vUrl)
	 val vResult: View = itemView.findViewById(R.id.vResult)
	 val vTextOccurrencesTitle: TextView = itemView.findViewById(R.id.vTextOccurrencesTitle)
	 val vFoundedPlacesCount: TextView = itemView.findViewById(R.id.vFoundedPlacesCount)
	 val vUrlOccurrencesTitle: TextView = itemView.findViewById(R.id.vUrlOccurrencesTitle)
	 val vFoundedUrlsCount: TextView = itemView.findViewById(R.id.vFoundedUrlsCount)

	 fun bind(result: UrlPageResult) {
		vId.text = result.id.toString()
		vUrl.text = result.url
		when(result) {
		  is UrlPageResult.SucceedUrlPageResult -> {
			 vResult.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green))
			 vTextOccurrencesTitle.visibility = VISIBLE
			 vTextOccurrencesTitle.setText(R.string.text_occurrence_count)
			 vFoundedPlacesCount.text = result.foundedPlaces.size.toString()
			 vFoundedUrlsCount.text = result.foundedUrls.size.toString()
			 vFoundedPlacesCount.visibility = VISIBLE
			 vUrlOccurrencesTitle.visibility = VISIBLE
			 vFoundedUrlsCount.visibility = VISIBLE
		  }
		  is UrlPageResult.FailedUrlPageResult -> {
			 vResult.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.red))
			 vTextOccurrencesTitle.text =
				String.format(itemView.context.getString(R.string.error_occurred), result.throwable.message)
			 vFoundedPlacesCount.visibility = GONE
			 vUrlOccurrencesTitle.visibility = GONE
			 vFoundedUrlsCount.visibility = GONE
		  }
		}
	 }
  }

  companion object {
	 val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UrlPageResult>() {
		override fun areItemsTheSame(oldItem: UrlPageResult, newItem: UrlPageResult): Boolean {
		  return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: UrlPageResult, newItem: UrlPageResult): Boolean {
		  return when {
			 oldItem is UrlPageResult.SucceedUrlPageResult && newItem is UrlPageResult.SucceedUrlPageResult -> {
				oldItem == newItem
			 }
			 oldItem is UrlPageResult.FailedUrlPageResult && newItem is UrlPageResult.FailedUrlPageResult -> {
				oldItem.throwable.message == newItem.throwable.message
			 }
			 else -> false
		  }
		}
	 }
  }

}