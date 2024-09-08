package com.example.findyou

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.findyou.data.COLLECTION_CHAT
import com.example.findyou.data.COLLECTION_USER
import com.example.findyou.data.ChatData
import com.example.findyou.data.ChatUser
import com.example.findyou.data.Event
import com.example.findyou.data.UserData
import com.example.findyou.ui.Gender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.lang.reflect.Field
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

    val matchProfiles = mutableStateOf<List<UserData>>(listOf())
    val inProgressProfiles = mutableStateOf(false)

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
                                populateCards()
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
                    populateCards()
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


    private fun populateCards() {
        inProgressProfiles.value = true

        val g =
            if (userData.value?.gender.isNullOrEmpty()) "ANY" else userData.value!!.gender!!.uppercase()
        val gPref =
            if (userData.value?.genderPreference.isNullOrEmpty()) "ANY" else userData.value!!.genderPreference!!.uppercase()

        val cardsQuery = when (Gender.valueOf(gPref)) {
            Gender.MALE -> db.collection(COLLECTION_USER)
                .whereEqualTo("gender", Gender.MALE)

            Gender.FEMALE -> db.collection(COLLECTION_USER).whereEqualTo("gender", Gender.FEMALE)
            Gender.ANY -> db.collection(COLLECTION_USER).whereEqualTo("gender", Gender.ANY)
        }

        val userGender = Gender.valueOf(g)

        cardsQuery.where(
            Filter.and(
                Filter.notEqualTo("userId", userData.value?.userId),
                Filter.or(
                    Filter.equalTo("genderPreference", userGender),
                    Filter.equalTo("genderPreference", Gender.ANY)
                )
            )
        )
            .addSnapshotListener { value, error ->
                if (error != null) {
                    inProgressProfiles.value = false
                    handleException(error)
                }
                if (value != null) {
                    val potentials = mutableListOf<UserData>()
                    value.documents.forEach {
                        it.toObject<UserData>()?.let { potential ->
                            var showUser = true
                            if (userData?.value?.swipeLeft?.contains(potential.userId) == true ||
                                userData?.value?.swipeRight?.contains(potential.userId) == true ||
                                userData?.value?.matches?.contains(potential.userId) == true
                            )
                                showUser = false
                            if (showUser) {
                                potentials.add(potential)
                            }
                        }
                    }

                    matchProfiles.value = potentials
                    inProgressProfiles.value = false
                }
            }
    }

    fun onDislike(selectedUser : UserData){
        db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
            .update("swipeLeft", FieldValue.arrayUnion(selectedUser.userId))
    }

    fun onLike(selectedUser: UserData){
        val reciprocalMatch = selectedUser.swipeRight.contains(userData.value?.userId)
        if (!reciprocalMatch){
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("swipeRight", FieldValue.arrayUnion(selectedUser.userId))
        } else {
            popupNotification.value = Event("Match!")

            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("swipeRight",FieldValue.arrayRemove(userData.value?.userId))
            db.collection(COLLECTION_USER).document(selectedUser.userId ?: "")
                .update("matches",FieldValue.arrayUnion(userData.value?.userId))
            db.collection(COLLECTION_USER).document(userData.value?.userId ?: "")
                .update("matches",FieldValue.arrayUnion(selectedUser.userId)
            )

            //new Collection Chat
            val chatKey = db.collection(COLLECTION_CHAT).document().id
            val chatData = ChatData(
                chatKey,
                ChatUser(
                    userData.value?.userId,
                    if (userData.value?.username.isNullOrEmpty()) userData.value?.username else userData.value?.name,
                    userData.value?.imageUrl
                ),
                ChatUser(
                    selectedUser.userId,
                    if (selectedUser.username.isNullOrEmpty()) selectedUser.username else selectedUser.name,
                    selectedUser.imageUrl
                )
            )
            db.collection(COLLECTION_CHAT).document(chatKey).set(chatData)
        }
    }
}