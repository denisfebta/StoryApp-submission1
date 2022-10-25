package com.denis.storyapp.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.denis.storyapp.data.Resource
import com.denis.storyapp.data.model.login.RegisterRequest
import com.denis.storyapp.data.model.stories.AddResponse
import com.denis.storyapp.data.remote.ApiConfig
import com.denis.storyapp.utils.UserPreferences
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val pref: UserPreferences) : ViewModel() {
    private val _userInfo = MutableLiveData<Resource<String>>()
    val userInfo: LiveData<Resource<String>> = _userInfo

    fun register(name: String, email: String, password: String) {
        _userInfo.postValue(Resource.Loading())
        val client = ApiConfig.getApiClient().register(RegisterRequest(name, email, password))

        client.enqueue(object : Callback<AddResponse> {
            override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.message.toString()
                    _userInfo.postValue(Resource.Success(message))
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        AddResponse::class.java
                    )
                    _userInfo.postValue(Resource.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                Log.e(
                    RegisterViewModel::class.java.simpleName,
                    "onFailure register"
                )
                _userInfo.postValue(Resource.Error(t.message))
            }
        })
    }
}
