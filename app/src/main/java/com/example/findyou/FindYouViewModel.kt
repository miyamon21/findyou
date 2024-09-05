package com.example.findyou

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.findyou.data.COLLECTION_USER
import com.example.findyou.data.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class FindYouViewModel @Inject constructor(
    val auth : FirebaseAuth,
    val db : FirebaseFirestore,
    val storage: FirebaseStorage
) :ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String?>>(Event(null))

    fun onSignUp(username : String, email:String,password : String){
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()){
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(COLLECTION_USER).whereEqualTo("username", username).get()
            .addOnSuccessListener { newUser ->
                if (newUser.isEmpty) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Create user profile in database
                            } else {
                                handleException(task.exception, "Signup Failed")
                            }
                        }
                } else {
                    handleException(customMessage = "username already exists")
                }
                inProgress.value = false
            }
            .addOnFailureListener{ failMsg->
                handleException(failMsg)
            }
    }

    private fun handleException(exception: Exception? = null,customMessage : String = ""){
        Log.e("FindYou", "FindYou Exception",exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "${customMessage}: $errorMsg"

        popupNotification.value = Event(message)
        inProgress.value = false
    }
}