package ddwu.com.mobile.finalreport.data

import com.google.gson.annotations.SerializedName

data class ParkingRoot(val getparkinginfo: Getparkinginfo,)
data class Getparkinginfo(
    @SerializedName("row")
    val parkings: List<Parking>,
)
data class Parking(
    @SerializedName("parking_name")
    val parkingName: String,
    val addr: String,
    val tel: String,
    val capacity: Long,
    val rates: Long,
    @SerializedName("cur_parking")
    val curParking: Long,
    @SerializedName("time_rate")
    val timeRate: Long,
    @SerializedName("add_rates")
    val addRates: Long,
    @SerializedName("add_time_rate")
    val addTimeRate: Long,
    @SerializedName("weekday_begin_time")
    val weekdayBeginTime: Long,
    @SerializedName("weekday_end_time")
    val weekdayEndTime: Long,
    @SerializedName("weekend_begin_time")
    val weekendBeginTime: Long,
    @SerializedName("weekend_end_time")
    val weekendEndTime: Long,
    @SerializedName("holiday_begin_time")
    val holidayBeginTime: Long,
    @SerializedName("holiday_end_time")
    val holidayEndTime: Long,
)
