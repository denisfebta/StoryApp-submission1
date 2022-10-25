package com.denis.storyapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.denis.storyapp.utils.UserPreferences

class ViewModelFactory(private val pref: UserPreferences) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(pref) as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(pref) as T
        }
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(pref) as T
        }
        if (modelClass.isAssignableFrom(AddViewModel::class.java)) {
            return AddViewModel(pref) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.simpleName)
    }
}