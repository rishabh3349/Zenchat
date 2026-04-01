package com.example.zenchat.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zenchat.data.model.User
import com.example.zenchat.databinding.FragmentHomeBinding
import com.example.zenchat.ui.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
	
	private var _binding: FragmentHomeBinding? = null
	private val binding
		get() = _binding
	private lateinit var adapter: HomeUserAdapter
	private var usersListener: ValueEventListener? = null
	
	@Inject
	lateinit var firebaseAuth: FirebaseAuth
	
	@Inject
	lateinit var mDbRef: DatabaseReference
	
	override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
		_binding = FragmentHomeBinding.inflate(inflater, container, false)
		return binding?.root!!
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		setUpAdapter()
		setUI()
	}
	
	private fun setUI() {
		binding?.retryButton?.setOnClickListener {
			binding?.retryButton?.visibility = View.GONE
			binding?.errorIv?.visibility = View.GONE
			binding?.progressBar?.visibility = View.VISIBLE
			attachUsersListener()
		}
	}
	
	private fun setUpAdapter() {
		adapter = HomeUserAdapter { user ->
			val intent = Intent(requireContext(), ChatActivity::class.java)
			intent.putExtra("name", user.name)
			intent.putExtra("uid", user.uid)
			intent.putExtra("email", user.email)
			startActivity(intent)
		}
		
		binding?.userListRv?.apply {
			layoutManager = LinearLayoutManager(requireContext())
			adapter = this@HomeFragment.adapter
		}
		attachUsersListener()
	}

	private fun attachUsersListener() {
		usersListener?.let { mDbRef.child("user").removeEventListener(it) }
		usersListener = object : ValueEventListener {
			
			override fun onDataChange(snapshot: DataSnapshot) {
				
				val currentUid = firebaseAuth.currentUser?.uid
				
				val users = snapshot.children.mapNotNull {
					it.getValue(User::class.java)
				}.filter { it.uid != currentUid }
				
				adapter.submitList(users)
				
				binding?.retryButton?.visibility = View.GONE
				binding?.errorIv?.visibility = View.GONE
				binding?.progressBar?.visibility = View.GONE
			}
			
			override fun onCancelled(error: DatabaseError) {
				binding?.progressBar?.visibility = View.GONE
				binding?.retryButton?.visibility = View.VISIBLE
				binding?.errorIv?.visibility = View.VISIBLE
				
				Toast.makeText(
					requireContext(),
					"Please try again",
					Toast.LENGTH_SHORT
				).show()
			}
		}
		mDbRef.child("user").addValueEventListener(usersListener as ValueEventListener)
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		usersListener?.let { mDbRef.child("user").removeEventListener(it) }
		usersListener = null
		_binding = null
	}
}