package com.example.todolistrecuperacion.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import com.example.todolistrecuperacion.MainActivity
import com.example.todolistrecuperacion.databinding.FragmentProfileBinding
import com.example.todolistrecuperacion.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.google.android.gms.tasks.Task as GoogleTask


class ProfileFragment: Fragment() {

  private lateinit var binding: FragmentProfileBinding

  private lateinit var user: User

  private lateinit var fireAuth: FirebaseAuth
  private lateinit var googleAuth: GoogleSignInClient
  private val db: FirebaseFirestore = Firebase.firestore
  private val storage = FirebaseStorage.getInstance().reference

  private var filePath: Uri? = null

  private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
    if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult

    this.filePath = it.data?.data

    this.changeProfileImage()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.binding = FragmentProfileBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.fireAuth = Firebase.auth

    this.binding.selectProfileImage.setOnClickListener {
      this.selectFile("image/*")
    }

    // Get user and show its data
    this.getUser()
    .addOnCompleteListener {
      if (!it.isSuccessful) return@addOnCompleteListener

      this.user = it.result.toObject(User::class.java)!!

      val lastName = if (this.user.lastname == "null") "" else this.user.lastname
      this.binding.userName.text = "${this.user.name} ${lastName}"
      this.binding.userEmail.text = this.fireAuth.currentUser?.email

      if (this.user.image.isEmpty()) return@addOnCompleteListener

      Picasso.get()
      .load(this.user.image)
      .into(this.binding.profilePic)
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

  private fun updateUser(user: User): GoogleTask<Void> {
    return this.db.document("users/${user.id}")
           .set(user)
  }

  // Launch intent to let the the user choose a file
  private fun selectFile(accept: String = "*/*") {
    val intent = Intent()
    intent.type = accept
    intent.action = Intent.ACTION_GET_CONTENT

    val intent2 = Intent.createChooser(intent, "Select Image from here...")

    resultLauncher.launch(intent2)
  }

  // Upload image to Firebase
  private fun changeProfileImage() {
    val imageRef = this.storage
    .child("images/${this.user.id}")

    imageRef
    .putFile(this.filePath!!)
    .addOnCompleteListener {
      if (!it.isSuccessful) {
        this.snackbar("There was an error uploading the file")
        return@addOnCompleteListener
      }
      imageRef.downloadUrl.addOnCompleteListener {
        val user = User(
          this.user.id,
          this.user.name,
          this.user.lastname,
          it.result.toString()
        )

        this.updateUser(user)
        .addOnCompleteListener {
          if (!it.isSuccessful) {
            this.snackbar("There was an error updating the user")
            return@addOnCompleteListener
          }

          Picasso.get().load(user.image).into(this.binding.profilePic)
        }
      }
    }
  }

}
