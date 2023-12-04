package ddwu.com.mobile.finalreport.data

import com.google.gson.annotations.SerializedName

data class ParkingRoot(
    @SerializedName("GetParkingInfo")
    val getParkingInfo: GetparkingInfo,)
data class GetparkingInfo(
    @SerializedName("list_total_count")
    val listTotalCount: Long,
    @SerializedName("row")
    val parkings: List<Parking>,
)
data class Parking(
    @SerializedName("PARKING_NAME")
    var parkingName: String,
    @SerializedName("ADDR")
    var addr: String,
    @SerializedName("TEL")
    var tel: String,
    @SerializedName("CAPACITY")
    var capacity: Long,
    @SerializedName("RATES")
    var rates: Long,
    @SerializedName("CUR_PARKING")
    var curParking: Long,
    @SerializedName("TIME_RATE")
    var timeRate: Long,
    @SerializedName("ADD_RATES")
    var addRates: Long,
    @SerializedName("ADD_TIME_RATE")
    var addTimeRate: Long,
    @SerializedName("WEEKDAY_BEGIN_TIME")
    var weekdayBeginTime: Long,
    @SerializedName("WEEKDAY_END_TIME")
    var weekdayEndTime: Long,
    @SerializedName("WEEKEND_BEGIN_TIME")
    var weekendBeginTime: Long,
    @SerializedName("WEEKEND_END_TIME")
    var weekendEndTime: Long,
    @SerializedName("HOLIDAY_BEGIN_TIME")
    var holidayBeginTime: Long,
    @SerializedName("HOLIDAY_END_TIME")
    var holidayEndTime: Long
)
