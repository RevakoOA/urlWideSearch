package com.just_me.urlwidesearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mAvailableProcessors = Runtime.getRuntime().availableProcessors()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startSearch.setOnClickListener {
            val startUrl = startUrl.text.toString()
            val textToSearch = textToSearch.text.toString()
            val maxTreads = maxTreads.text.toString().toInt()
            val maxUrlCounts = maxUrlCounts.text.toString().toInt()

            SearchController(application, maxTreads, maxUrlCounts).search(startUrl, textToSearch, {}, {})
        }
    }
}
