package com.example.zenchat.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.zenchat.MainActivity
import com.example.zenchat.R
import com.example.zenchat.databinding.ActivityBottomNavBinding
import com.example.zenchat.ui.home.HomeFragment
import com.example.zenchat.ui.profile.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomNavActivity : AppCompatActivity() {
	private lateinit var binding: ActivityBottomNavBinding
	private lateinit var fm: FragmentManager
	
	@Inject
	lateinit var firebaseAuth: FirebaseAuth
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBottomNavBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setUI(savedInstanceState)
	}
	
	fun setUI(savedInstanceState: Bundle?) {
		fm = supportFragmentManager
		
		if (savedInstanceState == null) {
			showHome()
		} else {
			when (fm.findFragmentById(R.id.fragment_container)) {
				is SettingsFragment -> showHome()
				else -> showSettings()
			}
		}
	}
	
	fun showHome() {
		if (fm.findFragmentById(R.id.fragment_container) !is HomeFragment) {
			fm.beginTransaction()
				.replace(R.id.fragment_container, HomeFragment())
				.commit()
		}
	}
	
	fun showSettings() {
		if (fm.findFragmentById(R.id.fragment_container) !is SettingsFragment) {
			fm.beginTransaction()
				.replace(R.id.fragment_container, SettingsFragment())
				.commit()
		}
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu, menu)
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == R.id.logout) {
			firebaseAuth.signOut()
			val intent = Intent(this@BottomNavActivity, MainActivity::class.java)
			startActivity(intent)
			finish()
			return true
		}
		return true
	}
	
	@Deprecated("Deprecated in Java")
	override fun onBackPressed() {
		AlertDialog.Builder(this)
			.setMessage("Are you sure you want to exit?")
			.setCancelable(false)
			.setPositiveButton("Yes") { _, _ ->
				finishAffinity()
				super.onBackPressed()
			}
			.setNegativeButton("No", null)
			.show()
	}
}
