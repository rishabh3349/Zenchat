package com.example.zenchat.data.model

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("name")
    @Expose
    var name: String? = null,
    
    @SerializedName("email")
    @Expose
    var email: String? = null,
    
    @SerializedName("uid", alternate = ["uId", "uID"])
    @Expose
    var uid: String? = null,
) : Parcelable