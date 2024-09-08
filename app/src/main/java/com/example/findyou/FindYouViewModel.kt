package com.example.findyou

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.findyou.data.COLLECTION_USER
import com.example.findyou.data.Event
import com.example.findyou.data.UserData
import com.example.findyou.ui.Gender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FindYouViewModel @Inject constructor(
    val auth : FirebaseAuth,
    val db : FirebaseFirestore,
    val storage: FirebaseStorage
) :ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String?>>(Event(null))
    var signedIn = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)

    init {
        val currentUser = auth.currentUser
        signedIn.value= currentUser != null
        currentUser?.uid?.let { getUserData(userId = it) }
    }

    /*
    * Authenticationへ登録
    */
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
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
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

    /*
    * FireStoneへ登録・上書き
    */
    private fun createOrUpdateProfile(
        name : String? = null,
        username : String? = null,
        bio : String? = null,
        imageUrl : String? = null,
        gender : Gender? = null,
        genderPreference : Gender? = null
    ){
        val userId = auth.currentUser?.uid
        val userData = UserData(
            userId = userId,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            bio = bio ?: userData.value?.bio,
            gender = gender?.toString() ?: userData.value?.gender,
            genderPreference = genderPreference?.toString() ?: userData.value?.genderPreference
        )

        userId?.let {userId ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(userId).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener { handleException(it, "Cannot Update user") }
                    } else {
                        db.collection(COLLECTION_USER).document(userId).set(userData)
                        inProgress.value = false
                        getUserData(userId)
                    }
                }
                .addOnFailureListener {
                    handleException(it,"Cannot crate user")
                }
        }
    }

    private fun getUserData(userId : String){
        inProgress.value = true
        db.collection(COLLECTION_USER).document(userId)
            .addSnapshotListener { value, error ->
                if (error != null){
                    handleException(error,"Cannot retrieve user data")
                }
                if (value != null){
                    val user = value.toObject<UserData>()
                    userData.value = user
                    inProgress.value = false
                }
            }
    }

    fun onLogin(email: String,password: String){
        if (email.isEmpty() || password.isEmpty()){
            handleException(customMessage = "Please fill in all fields")
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                }else{
                    handleException(task.exception,customMessage = "Login failed")
                }
            }
            .addOnFailureListener {
                handleException(it,"Login failed")
            }
    }

    fun onLogout(){
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Logged out")
    }

    fun updateProfileData(
        name: String,
        username: String,
        bio: String,
        gender: Gender,
        genderPreference: Gender
    ){
        createOrUpdateProfile(
            name=name,
            username=username,
            bio=bio,
            gender = gender,
            genderPreference = genderPreference

        )

    }


    private fun uploadImage(uri : Uri,onSuccess : (Uri) -> Unit){

        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener {
                handleException(it)
                inProgress.value =false
            }
    }

    fun uploadProfileImage(uri: Uri){
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())
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