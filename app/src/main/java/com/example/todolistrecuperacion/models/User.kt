package com.example.todolistrecuperacion.models

import java.io.Serializable

data class User(
  val id: String = "",
  val name: String = "",
  val lastname: String = "",
  val image: String = ""
): Serializable
