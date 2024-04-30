package com.example.zenchat.signUp_LogIn

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.zenchat.R

class ResetPass :AppCompatActivity () {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)
        supportActionBar?.hide()
        val back: Button =findViewById(R.id.back_to_login)
        back.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}