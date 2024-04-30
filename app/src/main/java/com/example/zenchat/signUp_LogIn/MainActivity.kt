package com.example.zenchat.signUp_LogIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.zenchat.user_list.OnLogin
import com.example.zenchat.R
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()

        val fp:Button=findViewById(R.id.textViewForgotPassword)
        fp.setOnClickListener {
            val intent = Intent(this, email_FP::class.java)
            startActivity(intent)
        }


        val signUp:Button=findViewById(R.id.newUser)
        signUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }


        val login:Button=findViewById(R.id.buttonLogin)
        val email:EditText=findViewById(R.id.editTextUsername)
        val pass:EditText=findViewById(R.id.editTextPassword)
        login.setOnClickListener {
            val email=email.text.toString().trim()
            if(email.isEmpty()){
                Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show()
            }
            else{
                val pass=pass.text.toString().trim()
                if(pass.isEmpty()){
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                }
                else{
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, OnLogin::class.java)
                            finish()
                            startActivity(intent)
                        }
                        else {
                            Toast.makeText(this, "User doesnot exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()

        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, OnLogin::class.java)
            startActivity(intent)
        }
    }
}