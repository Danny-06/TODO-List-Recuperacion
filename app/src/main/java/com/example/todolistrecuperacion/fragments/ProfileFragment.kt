package com.example.todolistrecuperacion.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.todolistrecuperacion.R
import com.example.todolistrecuperacion.databinding.FragmentProfileBinding


class ProfileFragment: Fragment() {

  private lateinit var binding: FragmentProfileBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    this.binding = FragmentProfileBinding.inflate(layoutInflater)
    return this.binding.root
  }

}
