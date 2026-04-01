package com.example.zenchat.ui.home

import androidx.recyclerview.widget.RecyclerView
import com.example.zenchat.data.model.User
import com.example.zenchat.databinding.UserRowBinding

class HomeItemViewHolder(
	val binding: UserRowBinding,
	
) : RecyclerView.ViewHolder(binding.root) {
	fun setUI(user : User , onClick: (User) -> Unit) {
		with(binding) {
			user.apply {
				userName.text = name
				root.setOnClickListener {
					onClick(user)
				}
			}
		}
	}
}