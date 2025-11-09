package com.example.firebaseapptest.ui.view.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseapptest.R
import com.example.firebaseapptest.ui.event.AppEvent
import com.example.firebaseapptest.ui.state.AppState
import com.example.firebaseapptest.ui.view.screen.components.SimpleCard

@Composable
fun Login(
    state: AppState,
    onEvent: (AppEvent) -> Unit,
    snackbarHostState: SnackbarHostState
) {

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        SimpleCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 18.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                val email = remember { mutableStateOf("") }
                TextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Email") },
                    singleLine = true,
                )
                val password = remember { mutableStateOf("") }
                val passwordVisible = remember { mutableStateOf(false) }
                TextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible.value)
                            ImageVector.vectorResource(R.drawable.visibility_24dp)
                        else ImageVector.vectorResource(R.drawable.visibility_lock_24dp)

                        IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible.value) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        onEvent(AppEvent.OnLogin(email.value, password.value))
                    },
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(state.showLoginSnackbar) {
        if (state.showLoginSnackbar) {
            snackbarHostState.showSnackbar(
                message = state.loginSnackbarMessage
            )
        }
    }
}