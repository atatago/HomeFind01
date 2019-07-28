package com.example.homefind01

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = Thread.currentThread().stackTrace[2].className
    private var finder: DeviceFinder? = null;

    private val SERVICE_TYPE = "_googlecast._tcp"
    private var deviceList: List<HostInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        finder = DeviceFinder(this, SERVICE_TYPE, 5353)

        //Android9以降で必要
        val wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("multicastLock")
        lock.setReferenceCounted(true)
        lock.acquire()

        button1.setOnClickListener {
            finder?.StartResolve(5, { devices ->
                deviceList = devices
                if (deviceList is List<HostInfo>) {
                    for (d in deviceList!!) {
                        Log.d(TAG, "Result device -> Name : ${d.Name}, IPAddress : ${d.IpAddress.toString()}, Port:${d.Port}")
                    }
                }
            })
        }
    }
}
