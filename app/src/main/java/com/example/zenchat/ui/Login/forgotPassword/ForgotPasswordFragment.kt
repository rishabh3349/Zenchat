package com.example.zenchat.ui.Login.forgotPassword

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.zenchat.R
import com.example.zenchat.data.model.AuthState
import com.example.zenchat.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val vm:ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        performClicks()
        observeAuthState()
    }
    fun performClicks(){
        binding.backToSignInFp.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.fp.setOnClickListener {
            val email = binding.email.text.toString().trim().lowercase()

            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                vm.fp(email)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid email",
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
                    binding.fp.isEnabled = false
                    binding.fp.visibility = View.GONE
                }

                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.fp.isEnabled = true
                    binding.fp.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Reset link sent successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(R.id.action_forgot_to_signIn)
                }

                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.fp.isEnabled = true
                    binding.fp.visibility = View.VISIBLE


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