package com.denis.storyapp.data.model.stories

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Story(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("photoUrl")
    val photoUrl: String?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("lat")
    val lat: Float? = null,
    @SerializedName("lon")
    val lon: Float? = null,
): Parcelable