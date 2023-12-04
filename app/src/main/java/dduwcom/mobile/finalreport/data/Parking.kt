package dduwcom.mobile.finalreport.data

import com.google.gson.annotations.SerializedName

data class ParkingRoot (
    @SerializedName("row")
    val parkings: List<Parking>
)

data class Parking (
    val addr: String,
    val name: String,
    val capacity: Int,
    val cur_parking: Int,
)