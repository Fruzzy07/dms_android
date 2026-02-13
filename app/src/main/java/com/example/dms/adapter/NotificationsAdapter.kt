package com.example.dms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dms.R
import com.example.dms.ui.NotificationsFragment

class NotificationsAdapter(
    private val notifications: List<NotificationsFragment.NotificationItem>
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.notificationIcon)
        val title: TextView = itemView.findViewById(R.id.notificationTitle)
        val description: TextView = itemView.findViewById(R.id.notificationDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.icon.setImageResource(notification.icon)
        holder.title.text = notification.title
        holder.description.text = notification.description
    }

    override fun getItemCount() = notifications.size
}
