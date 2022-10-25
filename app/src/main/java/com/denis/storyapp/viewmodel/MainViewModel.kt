package com.denis.storyapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denis.storyapp.data.Resource
import com.denis.storyapp.data.model.stories.AddResponse
import com.denis.storyapp.data.model.stories.StoriesResponse
import com.denis.storyapp.data.model.stories.Story
import com.denis.storyapp.data.remote.ApiConfig
import com.denis.storyapp.utils.UserPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreferences) : ViewModel() {
    private val _stories = MutableLiveData<Resource<ArrayList<Story>>>()
    val stories: LiveData<Resource<ArrayList<Story>>> = _stories

    suspend fun getAllStories() {
        _stories.postValue(Resource.Loading())
        val client =
            ApiConfig.getApiClient().getAllStories(token = "Bearer ${pref.getToken().first()}")

        client.enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(call: Call<StoriesResponse>, response: Response<StoriesResponse>) {

                if (response.isSuccessful) {
                    response.body()?.let {
                        val listStory = it.listStory
                        _stories.postValue(Resource.Success(ArrayList(listStory)))
                    }
                } else {
                    val errorResponse = Gson().fromJson(
                        response.errorBody()?.charStream(),
                        AddResponse::class.java
                    )
                    _stories.postValue(Resource.Error(errorResponse.message))
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                Log.e(
                    MainViewModel::class.java.simpleName,
                    "onFailure getStories"
                )
                _stories.postValue(Resource.Error(t.message))
            }
        })
    }
}
