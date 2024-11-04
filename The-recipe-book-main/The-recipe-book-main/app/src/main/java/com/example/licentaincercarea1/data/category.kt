package com.example.licentaincercarea1.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class category (
    @SerializedName(value="name")
    var Name:String="",
    @SerializedName(value="image")
    var Thumb:String="",
    @SerializedName(value="desc")
    var Description:String="",
):Parcelable