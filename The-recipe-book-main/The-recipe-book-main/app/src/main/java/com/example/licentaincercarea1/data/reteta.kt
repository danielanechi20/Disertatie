package com.example.licentaincercarea1.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class reteta (
    @SerializedName(value="Nume")
    var Nume:String="",
    @SerializedName(value="Thumb")
    var Thumb:String="",
    @SerializedName(value = "Ingrediente")
    var ingrediente: List<ingredient> = emptyList(),
    @SerializedName(value="P")
    var P:String="",
    @SerializedName(value = "Lipsa")
    var lipsa: List<ingredient> = emptyList(),
        ):Parcelable

