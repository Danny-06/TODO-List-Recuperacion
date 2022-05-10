package com.example.todolistrecuperacion

import android.content.Intent
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
import com.google.android.gms.tasks.Task as GoogleTask


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


    this.setContentView(this.binding.root)


    this.fireAuth = Firebase.auth

    // Check if intent was sent by an activity
    if (this.intent?.getStringExtra("Source") != null) {
      this.fireAuth.signOut()
    }


    // Redirect to MainActivity if user is logged
    if (this.fireAuth.currentUser != null) {
      this.goToMain()
    }


    // Login with Google when click the button
    this.binding.loginBtn.setOnClickListener {
      this.loginWithGoogle()
    }

  }

  private fun snackbar(message: String, duration: Int = 2000) {
    Snackbar.make(this.binding.root, message, duration).show()
  }

  private fun goToMain() {
    val intent = Intent(this, MainActivity::class.java)
    this.startActivity(intent)
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
          .addOnCompleteListener(this) { it ->

            if (it.isSuccessful) {

              // Check if there is a user already stored
              this.db.collection("users")
                .document(this.fireAuth.uid.toString())
                .get()
                .addOnCompleteListener {
                  // 'toObject' can throw an error if the 'data class' does not defined initial values for the properties
                  // https://stackoverflow.com/questions/38802269/firebase-user-is-missing-a-constructor-with-no-arguments
                  val user = it.result.toObject(User::class.java)

                  if (user != null) {
                    this.goToMain()
                  } else {
                    val user = User(
                      this.fireAuth.uid.toString(),
                      this.account.givenName.toString(),
                      this.account.familyName.toString(),
                      this.account.photoUrl.toString()
                    )

                    this.addUser(user)
                    .addOnCompleteListener {
                      if (it.isSuccessful) {
                        this.goToMain()
                        this.finish()
                      } else {
                        this.snackbar("An error ocurred when saving user data in the database")
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
      this.snackbar("No account was chosen or there was an error")
    }
  }

  private fun addUser(user: User): GoogleTask<Void> {
    return this.db.collection("users")
           .document(this.fireAuth.uid.toString())
           .set(user)
  }

}
