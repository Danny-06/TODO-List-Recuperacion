package com.example.todolistrecuperacion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.todolistrecuperacion.databinding.ActivityMainBinding
import com.example.todolistrecuperacion.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var user: User

  private lateinit var fireAuth: FirebaseAuth

  private val db: FirebaseFirestore = Firebase.firestore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.binding = ActivityMainBinding.inflate(layoutInflater)

    this.setContentView(this.binding.root)

    this.fireAuth = Firebase.auth

    this.db.collection("users")
      .document(this.fireAuth.uid.toString())
      .get()
      .addOnCompleteListener {
        if (it.isSuccessful) {
          this.user = it.result.toObject(User::class.java)!!
          this.binding.userName.text = this.user.name
        }
      }
  }

}
