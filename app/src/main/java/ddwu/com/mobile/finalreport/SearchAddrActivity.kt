package ddwu.com.mobile.finalreport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager

import ddwu.com.mobile.finalreport.databinding.ActivitySearchAddrBinding
import ddwu.com.mobile.finalreport.ui.ParkingAdapter


class SearchAddrActivity : AppCompatActivity() {

    lateinit var searchAddrBinding : ActivitySearchAddrBinding
    lateinit var adapter : ParkingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchAddrBinding = ActivitySearchAddrBinding.inflate(layoutInflater)
        setContentView(searchAddrBinding.root)

        adapter = ParkingAdapter()
        searchAddrBinding.rvParking.adapter = adapter
        searchAddrBinding.rvParking.layoutManager = LinearLayoutManager(this)

    }


}