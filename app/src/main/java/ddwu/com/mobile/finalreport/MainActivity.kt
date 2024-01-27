package ddwu.com.mobile.finalreport

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ddwu.com.mobile.finalreport.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        mainBinding.btnMyAddr.setOnClickListener {
            val intent = Intent(this, MyAddrActivity::class.java)
            startActivity(intent)
        }

        mainBinding.btnSearchAddr.setOnClickListener {
            val intent = Intent(this, SearchAddrActivity::class.java)
            startActivity(intent)
        }


    }
}