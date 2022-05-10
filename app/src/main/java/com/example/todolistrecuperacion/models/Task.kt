package com.example.todolistrecuperacion.models

import java.io.Serializable

data class Task(
  var id: String = "",
  val date: Long = 0,
  val text: String = "",
  var completed: Boolean = false
): Serializable
