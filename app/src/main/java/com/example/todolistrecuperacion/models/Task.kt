package com.example.todolistrecuperacion.models

import java.io.Serializable

data class Task(val uid: String = "", val date: String = "", val text: String = "", var completed: Boolean = false): Serializable
