package com.sachin.expensetracker

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            authViewModel.signInWithGoogle(credential)
        } catch (e: ApiException) {
            android.util.Log.e("GoogleSignIn", "Error code: ${e.statusCode}, Message: ${e.message}")
            Toast.makeText(context, "Google sign in failed: ${e.statusCode} - ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onLoginSuccess()
            }
            is AuthState.CodeSent -> {
                showOtpField = true
                Toast.makeText(context, "OTP sent successfully", Toast.LENGTH_SHORT).show()
            }
            is AuthState.Error -> {
                Toast.makeText(
                    context,
                    (authState as AuthState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo
            AppLogo(modifier = Modifier.size(80.dp))

            Text(
                text = "ExT",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )

            Text(
                text = "Expense Tracker",
                fontSize = 16.sp,
                color = SecondaryGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Phone Login Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SoftBlack
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Login with Phone",
                        fontWeight = FontWeight.Bold,
                        color = WhiteText
                    )

                    if (!showOtpField) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10) phoneNumber = it },
                            label = { Text("Phone Number", color = SecondaryGray) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = GoldAccent)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = CharcoalBorder,
                                cursorColor = GoldAccent
                            ),
                            prefix = { Text("+91 ", color = SecondaryGray) }
                        )

                        Button(
                            onClick = {
                                if (phoneNumber.length == 10) {
                                    authViewModel.sendVerificationCode(
                                        phoneNumber,
                                        context as Activity
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please enter valid 10-digit number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = DeepBlack
                            ),
                            enabled = authState !is AuthState.Loading
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = DeepBlack
                                )
                            } else {
                                Text("Send OTP")
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("Enter OTP", color = SecondaryGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = CharcoalBorder,
                                cursorColor = GoldAccent
                            )
                        )

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    authViewModel.verifyCode(otpCode)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please enter 6-digit OTP",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = DeepBlack
                            ),
                            enabled = authState !is AuthState.Loading
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = DeepBlack
                                )
                            } else {
                                Text("Verify OTP")
                            }
                        }

                        TextButton(
                            onClick = {
                                showOtpField = false
                                otpCode = ""
                                authViewModel.resetState()
                            }
                        ) {
                            Text("Change Number", color = GoldAccent)
                        }
                    }
                }
            }

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = CharcoalBorder)
                Text(
                    text = "  OR  ",
                    color = SecondaryGray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = CharcoalBorder)
            }

            // Google Sign In Button
            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("489354160418-l8brvc8no95ppi3tcfg0ijralgppi0gv.apps.googleusercontent.com") // Replace with your client ID
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GoldAccent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)
                )
            ) {
                Text("Sign in with Google")
            }
        }
    }
}