package com.example.dms.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dms.R
import com.example.dms.models.MyRequest

class MyRequestsAdapter(
    private val list: List<MyRequest>
) : RecyclerView.Adapter<MyRequestsAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val statusStripe: View = view.findViewById(R.id.statusStripe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val item = list[position]

        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.tvStatus.text = getStatusText(item.status)

        when (item.status.lowercase()) {
            "accepted", "принят" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_accepted)
                holder.statusStripe.setBackgroundColor(Color.parseColor("#27AE60"))
            }

            "rejected", "отклонен" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                holder.statusStripe.setBackgroundColor(Color.parseColor("#EB5757"))
            }

            else -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.statusStripe.setBackgroundColor(Color.parseColor("#F2994A"))
            }
        }
    }

    private fun getStatusText(status: String): String {
        return when (status.lowercase()) {
            "accepted" -> "Принят"
            "rejected" -> "Отклонен"
            "pending" -> "На рассмотрении"
            else -> status
        }
    }
}
