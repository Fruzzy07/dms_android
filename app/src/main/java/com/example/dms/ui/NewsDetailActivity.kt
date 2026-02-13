package com.example.dms.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dms.R
import com.example.dms.models.News

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val date = intent.getStringExtra("date")

        findViewById<TextView>(R.id.detailTitle).text = title
        findViewById<TextView>(R.id.detailDescription).text = description
        findViewById<TextView>(R.id.detailDate).text = date
    }
}
