package com.example.neopidorapp.feature_auth.presentation

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(

): ViewModel() {

    val firebaseAuth = FirebaseAuth.getInstance()


}