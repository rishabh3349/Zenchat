package com.example.zenchat.signUp_LogIn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenchat.R
import com.google.firebase.auth.FirebaseAuth

class email_FP: AppCompatActivity()  {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fp_email)
        supportActionBar?.hide()
        firebaseAuth = FirebaseAuth.getInstance()
        val reset:Button=findViewById(R.id.only_email)
        val email: EditText =findViewById(R.id.reset)
        val progressBar=findViewById<ProgressBar>(R.id.progressBar)
        reset.setOnClickListener{
            progressBar.visibility= View.VISIBLE
            reset.visibility=View.GONE
            val email=email.text.toString().trim()
            if(email.isEmpty()){
                progressBar.visibility= View.GONE
                reset.visibility=View.VISIBLE
                Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show()
            }
            else{
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    progressBar.visibility= View.GONE
                    reset.visibility=View.VISIBLE
                    val intent = Intent(this, ResetPass::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}