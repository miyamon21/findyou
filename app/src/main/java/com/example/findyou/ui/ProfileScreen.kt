package com.example.findyou.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.findyou.CommonDivider
import com.example.findyou.CommonProgressSpinner
import com.example.findyou.DestinationScreen
import com.example.findyou.FindYouViewModel
import com.example.findyou.navigateTo

enum class Gender {
    MALE,
    FEMALE,
    ANY
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: FindYouViewModel) {
    val inProgress = viewModel.inProgress.value
    if (inProgress) {
        CommonProgressSpinner()
    } else {
        val userData = viewModel.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.name ?: "")
        }
        var username by rememberSaveable {
            mutableStateOf(userData?.userName ?: "")
        }
        var bio by rememberSaveable {
            mutableStateOf(userData?.bio ?: "")
        }
        var gender by rememberSaveable {
            mutableStateOf(Gender.valueOf(userData?.gender?.uppercase() ?: "ANY"))
        }
        var genderPreferences by rememberSaveable {
            mutableStateOf(Gender.valueOf(userData?.genderPreference?.uppercase() ?: "ANY"))
        }

        val scrollState = rememberScrollState()

        Column {
            ProfileContent(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(8.dp),
                viewModel = viewModel,
                name = name,
                username = username,
                bio = bio,
                gender = gender,
                genderPreference = genderPreferences,
                onNameChange = { name = it },
                onUserNameChange = { username = it },
                onBioChange = { bio = it },
                onGenderChange = { gender = it },
                onGenderPreferenceChange = { genderPreferences = it },
                onSave = {},
                onBack = {
                    navigateTo(
                        navController = navController,
                        DestinationScreen.Swipe.route
                    )
                },
                onLogout = {
                    viewModel.onLogout()
                    navigateTo(navController, DestinationScreen.Login.route)
                }
            )
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier,
    viewModel: FindYouViewModel,
    name: String,
    username: String,
    bio: String,
    gender: Gender,
    genderPreference: Gender,
    onNameChange: (String) -> Unit,
    onUserNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onGenderPreferenceChange: (Gender) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val imageUrl = viewModel.userData.value?.imageUrl

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = "Back", modifier = Modifier.clickable { onBack.invoke() })
            Text(text = "Save", modifier = Modifier.clickable { onSave.invoke() })
        }

        CommonDivider()

        ProfileImage()

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.background(Color.LightGray),
                colors = TextFieldDefaults.colors(Color.Black),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "UserName", modifier = Modifier.width(100.dp))
            TextField(
                value = username, onValueChange = onUserNameChange,
                modifier = Modifier.background(Color.Transparent),
                colors = TextFieldDefaults.colors(Color.Black)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Bio", modifier = Modifier.width(100.dp))
            TextField(
                value = bio, onValueChange = onBioChange,
                modifier = Modifier
                    .background(Color.Transparent)
                    .height(150.dp),
                colors = TextFieldDefaults.colors(Color.Black),
                singleLine = false
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "I am a:", modifier = Modifier
                    .height(100.dp)
                    .padding(8.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.MALE,
                        onClick = { onGenderChange(Gender.MALE) }
                    )
                    Text(text = "Man", modifier = Modifier
                        .padding(4.dp)
                        .clickable { onGenderChange(Gender.MALE) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.FEMALE,
                        onClick = { onGenderChange(Gender.FEMALE) }
                    )
                    Text(text = "Woman", modifier = Modifier
                        .padding(4.dp)
                        .clickable { onGenderChange(Gender.FEMALE) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.ANY,
                        onClick = { onGenderChange(Gender.ANY) }
                    )
                    Text(text = "Any", modifier = Modifier
                        .padding(4.dp)
                        .clickable { onGenderChange(Gender.ANY) })
                }
            }
        }

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "I am a:", modifier = Modifier
                    .height(100.dp)
                    .padding(8.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = genderPreference == Gender.MALE,
                        onClick = { onGenderPreferenceChange(Gender.MALE) }
                    )
                    Text(text = "Man", modifier = Modifier
                        .padding(4.dp)
                        .clickable { onGenderPreferenceChange(Gender.MALE) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = genderPreference == Gender.FEMALE,
                        onClick = { onGenderPreferenceChange(Gender.FEMALE) }
                    )
                    Text(text = "Woman", modifier = Modifier
                        .padding(4.dp)
                        .clickable { onGenderPreferenceChange(Gender.FEMALE) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = genderPreference == Gender.ANY,
                        onClick = { onGenderPreferenceChange(Gender.ANY) }
                    )
                    Text(text = "Any", modifier = Modifier
                        .padding(4.dp)
                        .clickable { onGenderPreferenceChange(Gender.ANY) })
                }
            }
        }

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Logout",
                modifier = Modifier.clickable { onLogout.invoke() }
            )
        }
    }
}

@Composable
fun ProfileImage() {

}