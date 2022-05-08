package com.example.todolistrecuperacion

import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResult
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.example.todolistrecuperacion.databinding.ActivityLoginBinding
import com.example.todolistrecuperacion.models.User
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLoginBinding

  private lateinit var fireAuth: FirebaseAuth
  private lateinit var googleAuth: GoogleSignInClient

  private val db: FirebaseFirestore = Firebase.firestore
  private lateinit var account: GoogleSignInAccount

  private val googleResponse = registerForActivityResult(StartActivityForResult()) { handleGoogleResponse(it) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.binding = ActivityLoginBinding.inflate(layoutInflater)

    setContentView(this.binding.root)

    this.fireAuth = Firebase.auth


    this.binding.loginBtn.setOnClickListener {
      this.loginWithGoogle()
    }

  }

  private fun snackbar(message: String, duration: Int = 2000) {
    Snackbar.make(this.binding.root, message, duration).show()
  }

  private fun goToMainActivity() {

  }

  private fun loginWithGoogle() {
    val defaulWebClientId = this.getString(R.string.default_web_client_id)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(defaulWebClientId)
      .requestEmail()
      .build()

    this.googleAuth = GoogleSignIn.getClient(this, gso)

    this.googleResponse.launch(this.googleAuth.signInIntent)
  }

  private fun handleGoogleResponse(activityResult: ActivityResult) {
    if (activityResult.resultCode == RESULT_OK) {
      try {
        val taskSignIn = GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
        this.account = taskSignIn.getResult(ApiException::class.java)

        this.googleAuth.signOut()

        val credential = GoogleAuthProvider.getCredential(this.account.idToken, null)

        this.fireAuth.signInWithCredential(credential)
          .addOnCompleteListener(this) {

            if (it.isSuccessful) {

              this.db.collection("users")
                .document(this.fireAuth.uid.toString())
                .get()
                .addOnCompleteListener {
                  if (it.isSuccessful) {
                    this.goToMainActivity()
                  } else {
                    val user = User(
                      this.fireAuth.uid.toString(),
                      this.account.displayName.toString(),
                      this.account.photoUrl.toString()
                    )

                    this.db.collection("users")
                      .document(this.fireAuth.uid.toString())
                      .set(user)
                      .addOnCompleteListener {
                        if (it.isSuccessful) {
                          this.goToMainActivity()
                        }
                      }
                  }
                }
            } else {
              this.snackbar("An error ocurred in login process")
            }

          }
      } catch (e: ApiException) {
        this.snackbar("An error ocurred in Google Play service")
      }
    } else {
      this.snackbar("An error ocurred")
    }
  }

}
