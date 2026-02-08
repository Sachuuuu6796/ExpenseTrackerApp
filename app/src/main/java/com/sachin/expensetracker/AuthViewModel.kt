package com.sachin.expensetracker

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object CodeSent : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var verificationId: String? = null

    init {
        // Check if user is already logged in
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser!!.uid)
        }
    }

    // Phone Authentication - Send OTP
    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber") // India country code
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification successful
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(e.message ?: "Verification failed")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@AuthViewModel.verificationId = verificationId
                    _authState.value = AuthState.CodeSent
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Verify OTP
    fun verifyCode(code: String) {
        if (verificationId == null) {
            _authState.value = AuthState.Error("Verification ID is null")
            return
        }

        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithCredential(credential)
    }

    // Sign in with credential
    private fun signInWithCredential(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(auth.currentUser!!.uid)
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.message ?: "Authentication failed"
                    )
                }
            }
    }

    // Google Sign In
    fun signInWithGoogle(credential: AuthCredential) {
        _authState.value = AuthState.Loading
        signInWithCredential(credential)
    }

    // Sign Out
    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Initial
    }

    // Reset state
    fun resetState() {
        _authState.value = AuthState.Initial
    }
}