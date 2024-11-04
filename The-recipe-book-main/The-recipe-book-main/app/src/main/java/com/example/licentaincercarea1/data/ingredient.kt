package com.example.licentaincercarea1.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ingredient (
    @SerializedName(value="isChecked")
    var isChecked:Boolean=false,
    @SerializedName(value = "Nume")
    var nume: String = "",
    @SerializedName(value = "Cantitate")
    var cantitate: Int = 0,
    @SerializedName(value="Masura")
    var masura:String=""
): Parcelable