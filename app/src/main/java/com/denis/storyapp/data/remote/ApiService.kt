package com.denis.storyapp.data.remote

import com.denis.storyapp.data.model.login.LoginRequest
import com.denis.storyapp.data.model.login.LoginResponse
import com.denis.storyapp.data.model.login.RegisterRequest
import com.denis.storyapp.data.model.stories.AddResponse
import com.denis.storyapp.data.model.stories.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ): Call<AddResponse>

    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @GET("stories")
    fun getAllStories(
        @Header("Authorization") token: String,
    ): Call<StoriesResponse>

    @Multipart
    @POST("stories")
    fun uploadImage(
        @Header("Authorization") auth: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<AddResponse>
}