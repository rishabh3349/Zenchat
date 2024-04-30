package com.example.zenchat.signUp_LogIn

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.zenchat.R
import com.example.zenchat.dataAndAdapter.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()

        val name:EditText=findViewById(R.id.name)
        val email:EditText=findViewById(R.id.email)
        val pass:EditText=findViewById(R.id.password)
        val cpass:EditText=findViewById(R.id.confirm_password)
        val reg: Button =findViewById(R.id.register)
        reg.setOnClickListener{
            val name=name.text.toString().trim()
            if(name.isEmpty()){
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else{
                val email=email.text.toString().trim()
                if(email.isEmpty()){
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                }
                else{
                    val pass=pass.text.toString().trim()
                    if(pass.isEmpty()){
                        Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val cpass=cpass.text.toString().trim()
                        if(cpass.isEmpty()){
                            Toast.makeText(this, "Please re-enter password", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            if(pass!=cpass){
                                Toast.makeText(this, "Password did not match", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        firebaseAuth.currentUser?.sendEmailVerification()
                                            ?.addOnCompleteListener{
                                                addUserToDatabase(name,email,firebaseAuth.currentUser?.uid!!)
                                                val intent = Intent(this, After_SP::class.java)
                                                finish()
                                                startActivity(intent)
                                            }
                                    }
                                    else {
                                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun addUserToDatabase(name:String,email:String,uid:String){
        mDbRef=FirebaseDatabase.getInstance().getReference()
        mDbRef.child("user").child(uid).setValue(User(name,email,uid))
    }
}