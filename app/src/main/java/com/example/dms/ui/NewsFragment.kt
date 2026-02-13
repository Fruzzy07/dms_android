package com.example.dms.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dms.R
import com.example.dms.adapters.NewsAdapter
import com.example.dms.models.ApiResponse
import com.example.dms.models.News
import com.example.dms.network.RetrofitClient
import com.example.dms.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private val newsList: MutableList<News> = mutableListOf()
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_news, container, false)

        sessionManager = SessionManager(requireContext())

        recyclerView = view.findViewById(R.id.newsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addButton = view.findViewById(R.id.btnAddNews)

        adapter = NewsAdapter(
            newsList,
            sessionManager.getUserRole(),
            ::onEditClick,
            ::onDeleteClick
        )
        recyclerView.adapter = adapter

        updateAddButtonVisibility()
        fetchNews()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateAddButtonVisibility()
        if (sessionManager.getToken() != null) {
            fetchNews()
        }
    }

    private fun updateAddButtonVisibility() {
        val role = sessionManager.getUserRole()
        Log.d("NewsFragment", "Role: $role")

        if (role.equals("admin", true) || role.equals("manager", true)) {
            addButton.visibility = View.VISIBLE
            addButton.setOnClickListener { openAddNews() }
        } else {
            addButton.visibility = View.GONE
        }
    }

    private fun fetchNews() {
        val token = sessionManager.getToken() ?: return
        val api = RetrofitClient.getInstance(token)

        api.getAllNews().enqueue(object : Callback<ApiResponse<List<News>>> {

            override fun onResponse(
                call: Call<ApiResponse<List<News>>>,
                response: Response<ApiResponse<List<News>>>
            ) {
                if (!isAdded) return

                val body = response.body()
                val data = body?.data

                if (response.isSuccessful && data != null) {
                    newsList.clear()
                    newsList.addAll(data)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e(
                        "NewsFragment",
                        "getAllNews failed: code=${response.code()} message=${response.message()} apiMessage=${body?.message}"
                    )
                    context?.let {
                        Toast.makeText(it, "Ошибка загрузки новостей", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<News>>>, t: Throwable) {
                if (!isAdded) return
                Log.e("NewsFragment", "getAllNews network failure", t)

                context?.let {
                    Toast.makeText(it, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun onEditClick(news: News) {
        val ctx = context ?: return
        val newsId = news.id ?: return

        val intent = Intent(ctx, AddEditNewsActivity::class.java).apply {
            putExtra("news_id", newsId)
            putExtra("news_title", news.title)
            putExtra("news_description", news.description)
        }
        startActivity(intent)
    }

    private fun onDeleteClick(news: News) {
        val ctx = context ?: return
        val token = sessionManager.getToken() ?: return
        val newsId = news.id ?: return

        val api = RetrofitClient.getInstance(token)

        api.deleteNews(newsId).enqueue(object : Callback<Void> {

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (!isAdded) return

                if (response.isSuccessful) {
                    Toast.makeText(ctx, "Новость удалена", Toast.LENGTH_SHORT).show()
                    fetchNews()
                } else {
                    Toast.makeText(ctx, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(ctx, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openAddNews() {
        val ctx = context ?: return
        startActivity(Intent(ctx, AddEditNewsActivity::class.java))
    }
}
