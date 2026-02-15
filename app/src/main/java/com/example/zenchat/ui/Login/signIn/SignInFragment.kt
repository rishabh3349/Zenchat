package com.example.zenchat.ui.Login.signIn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.zenchat.R
import com.example.zenchat.databinding.FragmentSignInBinding
import com.example.zenchat.data.model.AuthState
import com.example.zenchat.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_sign_in) {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val vm: SignInViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignInBinding.bind(view)

        setupClicks()
        observeAuthState()
    }

    private fun setupClicks() {

        binding.goToSignup.setOnClickListener {
            findNavController().navigate(
                R.id.action_signIn_to_signup
            )
        }

        binding.goToForgot.setOnClickListener {
            findNavController().navigate(
                R.id.action_signIn_to_forgot
            )
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                vm.signIn(email, password)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter valid credentials",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun observeAuthState() {
        vm.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonLogin.visibility = View.GONE
                    binding.buttonLogin.isEnabled = false
                }

                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogin.visibility = View.VISIBLE

                    binding.buttonLogin.isEnabled = true

                    startActivity(Intent(requireContext(),HomeActivity::class.java))
                }

                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                    binding.buttonLogin.visibility = View.VISIBLE

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}