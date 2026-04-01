package com.example.zenchat.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.zenchat.data.model.User
import com.example.zenchat.databinding.UserRowBinding

class HomeUserAdapter(private val onUserClick: (User) -> Unit) :
	ListAdapter<User, HomeItemViewHolder>(DIFF_CALLBACK) {
	
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val binding = UserRowBinding.inflate(layoutInflater, parent, false)
		return HomeItemViewHolder(binding)
	}
	
	
	override fun onBindViewHolder(holder: HomeItemViewHolder, position: Int) {
		holder.setUI(getItem(position), onUserClick)
	}
	
	companion object {
		private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<User>() {
			override fun areItemsTheSame(oldItem: User, newItem: User) =
				oldItem.uid == newItem.uid
			
			override fun areContentsTheSame(oldItem: User, newItem: User) =
				oldItem == newItem
		}
	}
	
}