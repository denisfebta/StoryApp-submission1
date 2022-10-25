package com.denis.storyapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denis.storyapp.data.Resource
import com.denis.storyapp.data.model.stories.AddResponse
import com.denis.storyapp.data.remote.ApiConfig
import com.denis.storyapp.utils.UserPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddViewModel(private val pref: UserPreferences) : ViewModel() {
    private val _uploadStory = MutableLiveData<Resource<String>>()
    val uploadStory: LiveData<Resource<String>> = _uploadStory

    suspend fun uploadImage(
        imageMultipart: MultipartBody.Part, description: RequestBody,
    ) {
        _uploadStory.postValue(Resource.Loading())
        val client = ApiConfig.getApiClient().uploadImage(
            auth = "Bearer ${pref.getToken().first()}",
            imageMultipart,
            description
        )

        client.enqueue(object : Callback<AddResponse> {
            override fun onResponse(
                call: Call<AddResponse>,
                response: Response<AddResponse>
            ) {
                if (response.isSuccessful) {
                    _uploadStory.postValue(Resource.Success(response.body()?.message))
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        AddResponse::class.java
                    )
                    _uploadStory.postValue(Resource.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                Log.e(
                    AddViewModel::class.java.simpleName,
                    "onFailure upload"
                )
                _uploadStory.postValue(Resource.Error(t.message))
            }
        })
    }
}