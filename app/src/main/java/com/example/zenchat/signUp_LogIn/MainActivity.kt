package com.example.zenchat.signUp_LogIn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.zenchat.user_list.OnLogin
import com.example.zenchat.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var login: Button

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


        login=findViewById(R.id.buttonLogin)
        progressBar=findViewById(R.id.progressBar)
        val email:EditText=findViewById(R.id.editTextUsername)
        val pass:EditText=findViewById(R.id.editTextPassword)
        login.setOnClickListener {
            login.visibility= View.GONE
            progressBar.visibility=View.VISIBLE
            val email=email.text.toString().trim()
            if(email.isEmpty()){
                Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show()
                login.visibility= View.VISIBLE
                progressBar.visibility=View.GONE
            }
            else{
                val pass=pass.text.toString().trim()
                if(pass.isEmpty()){
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                    login.visibility= View.VISIBLE
                    progressBar.visibility=View.GONE
                }
                else{
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            login.visibility= View.VISIBLE
                            progressBar.visibility=View.GONE
                            val intent = Intent(this, OnLogin::class.java)
                            finish()
                            startActivity(intent)

                        }
                        else {
                            login.visibility= View.VISIBLE
                            progressBar.visibility=View.GONE
                            Toast.makeText(this, "User doesnot exist. Please SignUp.", Toast.LENGTH_SHORT).show()
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