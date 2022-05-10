package com.example.todolistrecuperacion

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.todolistrecuperacion.databinding.ActivityMainBinding
import com.example.todolistrecuperacion.fragments.ProfileFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

  lateinit var binding: ActivityMainBinding

  private lateinit var fireAuth: FirebaseAuth


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.binding = ActivityMainBinding.inflate(layoutInflater)

    this.setContentView(this.binding.root)

//    this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//    this.supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#444444")))
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menu.add("Profile")
    menu.add("Log out")

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when(item.title) {

      "Profile" -> {
        this.goToFragment(ProfileFragment())
      }

      "Log out" -> {
//        this.fireAuth.signOut()
        this.finish()
        this.goToLoginActivity()
      }

    }

    return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
//    super.onBackPressed()
  }

  private fun snackbar(message: String, duration: Int = 2000) {
    Snackbar.make(this.binding.root, message, duration).show()
  }

  private fun goToLoginActivity() {
    val intent = Intent(this, LoginActivity::class.java)
    intent.putExtra("Source", "MainActivity")
    this.startActivity(intent)
  }

  private fun goToFragment(fragmentInstance: Fragment) {
    this.supportFragmentManager.beginTransaction().apply {
      this.replace(R.id.nav_host_fragment_content_main, fragmentInstance)
      this.commit()
    }
  }

}
