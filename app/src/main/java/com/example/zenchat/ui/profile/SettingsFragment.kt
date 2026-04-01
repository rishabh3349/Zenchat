package com.example.zenchat.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.zenchat.MainActivity
import com.example.zenchat.R
import com.example.zenchat.data.model.User
import com.example.zenchat.databinding.FragmentSettingsBinding
import com.google.android.datatransport.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {
	private var _binding: FragmentSettingsBinding? = null
	private val binding get() = _binding!!

	@Inject
	lateinit var firebaseAuth: FirebaseAuth

	@Inject
	lateinit var databaseReference: DatabaseReference

	private val viewModel: SettingsViewModel by viewModels()
	private var currentName: String = ""
	private var currentEmail: String = ""

	private var darkListener: CompoundButton.OnCheckedChangeListener? = null
	private var notificationsListener: CompoundButton.OnCheckedChangeListener? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentSettingsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		(activity as? AppCompatActivity)?.supportActionBar?.hide()

		bindProfile()
		attachSwitchListeners()
		bindClicks()
		collectState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun bindProfile() {
		val user = firebaseAuth.currentUser
		currentEmail = user?.email?.trim().orEmpty()
		currentName = getString(R.string.app_name)
		renderProfile(currentName, currentEmail)
		fetchProfileFromFirebase()
	}

	private fun renderProfile(name: String, email: String) {
		binding.titleTv.text = name
		binding.emailTv.text = if (email.isNotBlank()) email else getString(R.string.email_placeholder)

		val firstName = name.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
		val initial = firstName.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
			?: name.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
			?: getString(R.string.r)
		binding.initialText.text = initial
	}

	private fun fetchProfileFromFirebase() {
		val uid = firebaseAuth.currentUser?.uid ?: return
		databaseReference.child("user").child(uid)
			.addListenerForSingleValueEvent(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val user = snapshot.getValue(User::class.java)
					val firebaseName = user?.name?.trim().orEmpty()
					val firebaseEmail = user?.email?.trim().orEmpty()
					currentName = if (firebaseName.isNotBlank()) firebaseName else getString(R.string.app_name)
					if (firebaseEmail.isNotBlank()) {
						currentEmail = firebaseEmail
					}
					renderProfile(currentName, currentEmail)
				}

				override fun onCancelled(error: DatabaseError) {
					Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
				}
			})
	}

	private fun attachSwitchListeners() {
		darkListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
			viewModel.setDarkMode(isChecked)
			AppCompatDelegate.setDefaultNightMode(
				if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
			)
		}
		notificationsListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
			viewModel.setNotifications(isChecked)
			Toast.makeText(
				requireContext(),
				if (isChecked) getString(R.string.notifications_enabled) else getString(R.string.notifications_disabled),
				Toast.LENGTH_SHORT
			).show()
		}
		binding.darkModeSwitch.setOnCheckedChangeListener(darkListener)
		binding.notificationsSwitch.setOnCheckedChangeListener(notificationsListener)
	}

	private fun setDarkSwitchSilently(checked: Boolean) {
		binding.darkModeSwitch.setOnCheckedChangeListener(null)
		binding.darkModeSwitch.isChecked = checked
		binding.darkModeSwitch.setOnCheckedChangeListener(darkListener)
	}

	private fun setNotificationsSwitchSilently(checked: Boolean) {
		binding.notificationsSwitch.setOnCheckedChangeListener(null)
		binding.notificationsSwitch.isChecked = checked
		binding.notificationsSwitch.setOnCheckedChangeListener(notificationsListener)
	}

	private fun collectState() {
		binding.versionTv.text =
			getString(R.string.version_placeholder) + " " + BuildConfig.VERSION_NAME

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				launch {
					viewModel.darkModeEnabled.collect { enabled ->
						if (binding.darkModeSwitch.isChecked != enabled) {
							setDarkSwitchSilently(enabled)
						}
					}
				}
				launch {
					viewModel.notificationsEnabled.collect { enabled ->
						if (binding.notificationsSwitch.isChecked != enabled) {
							setNotificationsSwitchSilently(enabled)
						}
					}
				}
			}
		}
	}

	private fun bindClicks() {
		binding.editTv.setOnClickListener {
			showEditNameDialog()
		}

		binding.rowDarkMode.setOnClickListener {
			binding.darkModeSwitch.isChecked = !binding.darkModeSwitch.isChecked
		}

		binding.rowNotifications.setOnClickListener {
			binding.notificationsSwitch.isChecked = !binding.notificationsSwitch.isChecked
		}

		binding.rowShare.setOnClickListener {
			val text = "Try Zenchat — a clean chat experience.\n\nDownload and share with friends!\nhttps://drive.google.com/file/d/15p_B7ZmgShrGD1vU5B40bXcW-4X2CdRl/view?usp=sharing"
			startActivity(
				Intent(Intent.ACTION_SEND).apply {
					type = "text/plain"
					putExtra(Intent.EXTRA_TEXT, text)
				}
			)
		}

		binding.rowSource.setOnClickListener {
			openUrl("https://github.com/rishabh3349/Zenchat")
		}

		binding.instagram.setOnClickListener { openUrl("https://www.instagram.com/__._rishabh/") }
		binding.github.setOnClickListener { openUrl("https://github.com/rishabh3349/") }
		binding.linkedin.setOnClickListener { openUrl("https://www.linkedin.com/in/rishabh-sharma-9a8815254/") }

		binding.rowLogout.setOnClickListener {
			firebaseAuth.signOut()
			startActivity(Intent(requireContext(), MainActivity::class.java))
			requireActivity().finish()
		}
	}

	private fun showEditNameDialog() {
		val editText = EditText(requireContext()).apply {
			inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
			setText(currentName)
			setSelection(text.length)
			setPadding(48, 32, 48, 16)
		}

		val dialog = AlertDialog.Builder(requireContext())
			.setTitle(getString(R.string.edit_profile))
			.setMessage(getString(R.string.edit_name_prompt))
			.setView(editText)
			.setPositiveButton(getString(R.string.save), null)
			.setNegativeButton(getString(R.string.cancel), null)
			.show()
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
			val newName = editText.text.toString().trim()
			if (newName.isBlank()) {
				editText.error = getString(R.string.name_cannot_be_empty)
				return@setOnClickListener
			}
			updateNameInFirebase(newName)
			dialog.dismiss()
		}
	}

	private fun updateNameInFirebase(newName: String) {
		val uid = firebaseAuth.currentUser?.uid ?: return
		databaseReference.child("user").child(uid).child("name")
			.setValue(newName)
			.addOnSuccessListener {
				currentName = newName
				renderProfile(currentName, currentEmail)
				Toast.makeText(requireContext(), getString(R.string.name_updated), Toast.LENGTH_SHORT).show()
			}
			.addOnFailureListener {
				Toast.makeText(requireContext(), it.message ?: getString(R.string.try_again), Toast.LENGTH_SHORT).show()
			}
	}

	private fun openUrl(url: String) {
		runCatching {
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
		}.onFailure {
			Toast.makeText(requireContext(), "Can't open link", Toast.LENGTH_SHORT).show()
		}
	}
}
