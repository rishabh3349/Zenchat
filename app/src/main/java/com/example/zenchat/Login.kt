package com.example.zenchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Login:AppCompatActivity() {
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_login)
        supportActionBar?.hide()

    }
}