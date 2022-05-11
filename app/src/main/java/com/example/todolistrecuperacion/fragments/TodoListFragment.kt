package com.example.todolistrecuperacion.fragments

import RecyclerViewAdapter
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistrecuperacion.MainActivity
import com.example.todolistrecuperacion.R
import com.example.todolistrecuperacion.databinding.FragmentTodoListBinding
import com.example.todolistrecuperacion.databinding.ItemTaskBinding
import com.example.todolistrecuperacion.databinding.TaskFormBinding
import com.example.todolistrecuperacion.models.Task
import com.example.todolistrecuperacion.models.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.gms.tasks.Task as GoogleTask


class TodoListFragment: Fragment() {

  private lateinit var binding: FragmentTodoListBinding

  private lateinit var user: User
  private lateinit var fireAuth: FirebaseAuth
  private val db: FirebaseFirestore = Firebase.firestore

  private lateinit var taskAdapter: RecyclerViewAdapter<Task>


  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    this.binding = FragmentTodoListBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    super.onViewCreated(view, savedInstanceState)

    this.fireAuth = Firebase.auth

    this.getUser()
    .addOnCompleteListener {
      if (it.isSuccessful) {
        this.user = it.result.toObject(User::class.java)!!
      }

      this.initTasks()
    }

    this.binding.addTaskBtn.setOnClickListener {
      this.addTask()
    }

  }


//  override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
//    super.onCreateContextMenu(menu, v, menuInfo)
//
//    this.activity?.menuInflater?.inflate(R.menu.task_menu, menu)
//  }
//
//  override fun onContextItemSelected(item: MenuItem): Boolean {
//    if (item.title == "Delete Task") {
//
//    }
//
//    return true
//  }


  private fun snackbar(message: String, duration: Int = 2000) {
    val activity = this.activity as MainActivity
    Snackbar.make(activity.binding.root, message, duration).show()
  }

  private fun getUser(): GoogleTask<DocumentSnapshot> {
    return this.db.collection("users")
           .document(this.fireAuth.uid.toString())
           .get()
  }

  private fun getTasks(): GoogleTask<QuerySnapshot> {
    val collectionPath = "users/${this.fireAuth.uid}/tasks"
    return this.db.collection(collectionPath).get()
  }

  private fun addTask(task: Task): GoogleTask<DocumentReference> {
    val collectionPath = "users/${this.fireAuth.uid}/tasks"
    return this.db.collection(collectionPath).add(task)
  }

  private fun updateTask(task: Task): GoogleTask<Void> {
    val taskPath = "users/${this.fireAuth.uid}/tasks/${task.id}"
    return this.db.document(taskPath).set(task)
  }

  private fun deleteTask(task: Task): GoogleTask<Void> {
    val taskPath = "users/${this.fireAuth.uid}/tasks/${task.id}"
    return this.db.document(taskPath).delete()
  }

  private fun displayTasks(tasks: ArrayList<Task>) {
    this.taskAdapter = RecyclerViewAdapter(R.layout.item_task, tasks)

    // Drawables of the state of the task
    val taskCompletedDrawable = R.drawable.ic_baseline_check_24
    val taskPendingDrawable = R.drawable.ic_outline_push_pin_24

    this.taskAdapter.setOnBindViewHolderListener { taskView, task, index ->
      this.registerForContextMenu(taskView)

      val taskBinding = ItemTaskBinding.bind(taskView)

      taskBinding.taskDescription.text = task.text
      taskBinding.taskDate.text = DateFormat.getDateInstance().format(task.date)
      taskBinding.taskCompleted.setImageResource(
        if (task.completed) taskCompletedDrawable
        else                taskPendingDrawable
      )
    }
    this.taskAdapter.setOnItemClickListener { taskView, task, index ->
      if (taskView == null) return@setOnItemClickListener

      val taskBinding = ItemTaskBinding.bind(taskView)

      task.completed = !task.completed

      taskBinding.taskCompleted.setImageResource(
        if (task.completed) taskCompletedDrawable
        else                taskPendingDrawable
      )

      this.updateTask(task)
    }
    this.taskAdapter.setOnItemLongClickListener { taskView, task, index ->
      if (taskView == null) return@setOnItemLongClickListener(true)

      taskView.showContextMenu()

      AlertDialog.Builder(this.requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
      .setTitle("Are you sure you want to delete this task?")
      .setPositiveButton("Ok") { _, _ ->
        this.deleteTask(task)
        this.taskAdapter.removeItem(task)
      }
      .setNegativeButton("Cancel") { _, _ -> }
      .show()

      true
    }

    this.binding.taskList.adapter = this.taskAdapter
    this.binding.taskList.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
  }

  private fun addTask() {
    val taskFormView = View.inflate(this.context, R.layout.task_form, null)
    val taskFormBinding = TaskFormBinding.bind(taskFormView)

    AlertDialog.Builder(this.requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
      .setTitle("Task Form")
      .setView(taskFormView)
      .setPositiveButton("Save") { dialogInterface, n ->
        val taskDescription = taskFormBinding.taskDescription.text.toString()
        if (taskDescription.isEmpty())
          this.snackbar("Task wasn't saved, description can't be empty", 2500)
        else {
          val task = Task(text = taskDescription, date = Calendar.getInstance().timeInMillis)
          this.addTask(task)
          .addOnCompleteListener {
            if (it.isSuccessful) {
              this.snackbar("Task was created correctly")

              task.id = it.getResult().id
              this.updateTask(task)
              .addOnCompleteListener {
                this.taskAdapter.addItem(task)
                this.binding.noTaskMessage.isVisible = this.taskAdapter.itemCount == 0
              }
            } else {
              this.snackbar("There was an error creating the task, try again later")
            }
          }
        }
      }
      .setNegativeButton("Cancel") { dialogInterface, n -> }
      .setOnCancelListener { }
      .create().show()
  }

  private fun initTasks() {
    this.getTasks()
    .addOnCompleteListener {
      if (it.isSuccessful) {
        val tasks = it.result.toObjects(Task::class.java) as ArrayList

        this.displayTasks(tasks)

        this.binding.noTaskMessage.isVisible = tasks.size == 0
      }
    }
  }

}
