package com.appointmed.mobile.notifications.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appointmed.mobile.R
import com.appointmed.mobile.data.model.NotificationItem

class NotificationsAdapter(
    private val clickListener: NotificationClickListener
) : ListAdapter<NotificationItem, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.notificationTitle)
        private val messageView: TextView = itemView.findViewById(R.id.notificationMessage)
        private val timestampView: TextView = itemView.findViewById(R.id.notificationTimestamp)

        fun bind(item: NotificationItem, clickListener: NotificationClickListener) {
            titleView.text = item.title
            messageView.text = item.message
            timestampView.text = item.createdAt ?: ""
            itemView.alpha = if (item.isRead) 0.6f else 1.0f
            itemView.setOnClickListener { clickListener.onNotificationClicked(item) }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}
