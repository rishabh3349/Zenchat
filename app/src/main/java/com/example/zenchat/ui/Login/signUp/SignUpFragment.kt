package com.example.zenchat.ui.Login.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.zenchat.R
import com.example.zenchat.data.model.AuthState
import com.example.zenchat.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val vm:SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        observeAuthState()
    }


    private fun setupClicks(){
        binding.backToSignIn.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.register.setOnClickListener{
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val name = binding.name.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && password==confirmPassword) {
                vm.signUp(name,email, password)
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
                    binding.register.visibility = View.GONE
                    binding.register.isEnabled = false
                }

                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.register.isEnabled = true
                    binding.register.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Sign Up successful. Please verify the email using link sent on your email and login again.",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(R.id.action_signUp_to_signIn)
                }

                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.register.isEnabled = true
                    binding.register.visibility = View.VISIBLE

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