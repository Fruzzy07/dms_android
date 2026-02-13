package com.example.dms.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dms.R
import com.example.dms.adapter.NotificationsAdapter

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val notifications = mutableListOf<NotificationItem>()

    data class NotificationItem(
        val icon: Int,
        val title: String,
        val description: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        
        recyclerView = view.findViewById(R.id.notificationsRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Загружаем уведомления (пока заглушка)
        loadNotifications()
        
        return view
    }

    private fun loadNotifications() {
        // Временные данные для демонстрации
        notifications.clear()
        notifications.add(
            NotificationItem(
                R.drawable.ic_money,
                "Напоминание об оплате",
                "Напоминаем о необходимости своевременной оплаты проживания в общежитии. Проверьте статус платежа в личном кабинете."
            )
        )
        notifications.add(
            NotificationItem(
                R.drawable.ic_profile,
                "Обновите данные",
                "Пожалуйста, проверьте и обновите вашу личную информацию в профиле студента."
            )
        )
        notifications.add(
            NotificationItem(
                android.R.drawable.ic_dialog_alert,
                "Требуется внимание",
                "В вашем личном кабинете есть информация, требующая вашего внимания."
            )
        )
        notifications.add(
            NotificationItem(
                R.drawable.ic_housing,
                "Статус проживания обновлён",
                "Ваш статус проживания был обновлён. Проверьте информацию в разделе 'Проживание'."
            )
        )
        
        recyclerView.adapter = NotificationsAdapter(notifications)
    }
}
