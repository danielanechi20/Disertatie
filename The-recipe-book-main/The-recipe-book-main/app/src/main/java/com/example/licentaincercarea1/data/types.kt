package com.example.licentaincercarea1.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class types (
    @SerializedName(value="tip")
    var tip:String="",
    @SerializedName(value="denumiri")
    var denumiri:List<ingredient> = emptyList()
):Parcelable{
}