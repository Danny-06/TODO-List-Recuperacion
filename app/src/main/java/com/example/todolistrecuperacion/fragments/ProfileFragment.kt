package com.example.todolistrecuperacion.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.todolistrecuperacion.MainActivity
import com.example.todolistrecuperacion.R
import com.example.todolistrecuperacion.databinding.FragmentProfileBinding
import com.example.todolistrecuperacion.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.tasks.Task as GoogleTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ProfileFragment: Fragment() {

  private lateinit var binding: FragmentProfileBinding

  private lateinit var user: User

  private lateinit var fireAuth: FirebaseAuth
  private lateinit var googleAuth: GoogleSignInClient
  private val db: FirebaseFirestore = Firebase.firestore

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.binding = FragmentProfileBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.fireAuth = Firebase.auth

    this.getUser()
    .addOnCompleteListener {
      if (it.isSuccessful) {
        this.user = it.result.toObject(User::class.java)!!
        this.binding.test.text = "${user.name} ${user.lastname}\n${this.fireAuth.currentUser?.email}\n${user.image}"
      }
    }

  }


  private fun snackbar(message: String, duration: Int = 2000) {
    val activity = this.activity as MainActivity
    Snackbar.make(activity.binding.root, message, duration).show()
  }

  private fun getUser(): GoogleTask<DocumentSnapshot> {
    return this.db.collection("users")
      .document(this.fireAuth.uid.toString())
      .get()
  }

}
