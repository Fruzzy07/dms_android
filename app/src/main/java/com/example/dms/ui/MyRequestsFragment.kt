package com.example.dms.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.dms.R
import com.example.dms.adapter.MyRequestsAdapter
import com.example.dms.models.*
import com.example.dms.network.RetrofitClient
import com.example.dms.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyRequestsFragment : Fragment() {

    private lateinit var rvRequests: RecyclerView
    private val requests = mutableListOf<MyRequest>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvRequests = view.findViewById(R.id.rvRequests)
        rvRequests.layoutManager = LinearLayoutManager(requireContext())

        // 🔥 Анимация появления карточек
        rvRequests.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down)
    }

    override fun onResume() {
        super.onResume()
        loadRequests()
    }

    private fun loadRequests() {
        requests.clear()

        val token = SessionManager(requireContext()).getToken()
        if (!token.isNullOrEmpty()) {
            loadLiveRequestsFromApi(token)
        } else {
            loadLocalRequests()
        }
    }

    private fun loadLiveRequestsFromApi(token: String) {
        val api = RetrofitClient.getInstance(token)
        api.getLiveRequests().enqueue(object : Callback<ApiResponse<List<RequestLive>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<RequestLive>>>,
                response: Response<ApiResponse<List<RequestLive>>>
            ) {
                if (!isAdded) return
                val data = response.body()?.data
                if (response.isSuccessful && data != null) {
                    data.forEach { req ->
                        val desc = req.room?.let {
                            "Этаж ${it.floor?.floorNumber ?: "-"}, комната ${it.roomNumber}"
                        } ?: "Комната #${req.roomId}"

                        requests.add(
                            MyRequest(
                                id = req.id,
                                type = RequestType.LIVE,
                                title = "Запрос на проживание",
                                description = desc,
                                status = req.status,
                                createdAt = req.createdAt
                            )
                        )
                    }
                    loadLocalSportsRequests()
                    updateRecyclerView()
                } else {
                    loadLocalRequests()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<RequestLive>>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Ошибка загрузки: ${t.message}", Toast.LENGTH_SHORT).show()
                loadLocalRequests()
            }
        })
    }

    private fun loadLocalSportsRequests() {
        val sportsPref = requireContext().getSharedPreferences("sports_requests", 0)
        sportsPref.all.forEach { entry ->
            val parts = entry.value.toString().split("|")
            if (parts.size >= 4) {
                val sport = parts[0]
                val teacher = parts[1]
                val time = parts[2]
                val status = parts[3]

                requests.add(
                    MyRequest(
                        id = entry.key.toIntOrNull() ?: 0,
                        type = RequestType.SPORTS,
                        title = "Запись на физкультуру",
                        description = "$sport, $teacher, $time",
                        status = status,
                        createdAt = null
                    )
                )
            }
        }
    }

    private fun loadLocalRequests() {
        loadLocalSportsRequests()

        val livePref = requireContext().getSharedPreferences("live_requests", 0)
        livePref.all.forEach { entry ->
            val parts = entry.value.toString().split("|")
            if (parts.size >= 3) {
                val room = parts[0]
                val floor = parts[1]
                val status = parts[2]

                requests.add(
                    MyRequest(
                        id = entry.key.toIntOrNull() ?: 0,
                        type = RequestType.LIVE,
                        title = "Запись на проживание",
                        description = "$floor, комната $room",
                        status = status,
                        createdAt = null
                    )
                )
            }
        }

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        val sortedRequests = requests.sortedByDescending { it.createdAt ?: "" }

        rvRequests.adapter = MyRequestsAdapter(sortedRequests)

        // 🔥 Запуск анимации
        rvRequests.scheduleLayoutAnimation()
    }
}
