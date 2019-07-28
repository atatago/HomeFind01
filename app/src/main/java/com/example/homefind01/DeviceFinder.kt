package com.example.homefind01

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.AsyncTask
import android.util.Log

class DeviceFinder(context: Context, serviceType:String, port:Int) {
    private val TAG = Thread.currentThread().stackTrace[2].className

    private var context = context
    private var nsdManager: NsdManager? = null
    private val SERVICE_NAME = "DeviceFinder"
    private val serviceType = serviceType

    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var infoList = ArrayList<NsdServiceInfo>()
    private var devices: ArrayList<HostInfo> = ArrayList()
    private var onFinished: ((List<HostInfo>) -> Unit)? = null
    private val serviceInfo = NsdServiceInfo().apply {
        serviceName = SERVICE_NAME
        this.serviceType = serviceType
        this.port = port
    }
    private var findTask: FindTask? = null

    init {
    }

    class FindTask : AsyncTask<Long, Int, Int>() {
        private val TAG = Thread.currentThread().stackTrace[2].className
        var finder: DeviceFinder? = null

        override fun doInBackground(vararg waitSec: Long?): Int {
            var sec: Long = 5
            if (waitSec.count() > 0) {
                if (waitSec[0] != null) {
                    sec = waitSec[0]!!
                }
            }

            for (i in 0..sec) {
                Log.d(TAG, "Task running... : ${i}")
                Thread.sleep(1000)
            }
            Log.d(TAG, "Task finish")
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            finder?.nextResolve()
        }
    }

    fun StartResolve(findSec: Long, onFinished: (List<HostInfo>) -> Unit) {
        Log.d(TAG, "Start resolve")
        this.onFinished = onFinished
        devices = ArrayList()
        nextResolve()

        if (nsdManager == null) {
            InitListener()
            nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
                registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            }
        }

        findTask = FindTask().also {
            it.finder = this
        }
        findTask?.execute(findSec)
    }

    internal fun nextResolve() {
        Log.d(TAG, "Next resolve")
        if (infoList.count() > 0) {
            nsdManager?.resolveService(infoList[0], resolveListener)
            infoList.removeAt(0)
        } else {
            nsdManager?.unregisterService(registrationListener)
            nsdManager = null
            onFinished?.invoke(devices)
        }
    }

    private fun InitListener() {
        registrationListener = object : NsdManager.RegistrationListener {
            override fun onRegistrationFailed(var1: NsdServiceInfo, var2: Int) {
                Log.d(TAG, "onRegistrationFailed : ${var2}")
            }

            override fun onUnregistrationFailed(var1: NsdServiceInfo, var2: Int) {
                Log.d(TAG, "onUnregistrationFailed")
            }

            override fun onServiceRegistered(var1: NsdServiceInfo) {
                Log.d(TAG, "onServiceRegistered")
                nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            }

            override fun onServiceUnregistered(var1: NsdServiceInfo) {
                Log.d(TAG, "onServiceUnregistered")
            }
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(info: NsdServiceInfo?) {
                if (!info?.serviceName?.toLowerCase().equals(SERVICE_NAME.toLowerCase()) && info is NsdServiceInfo) {
                    Log.d(TAG, "onServiceFound : ${info.serviceName} : ${info.host} : ${info.serviceType} : ${info.port}")
                    infoList.add(info)
                }
            }

            override fun onStopDiscoveryFailed(message: String?, code: Int) {
                Log.d(TAG, "onStopDiscoveryFailed : ${message}")
            }

            override fun onStartDiscoveryFailed(message: String?, p1: Int) {
                Log.d(TAG, "onStartDiscoveryFailed : ${message}")
            }

            override fun onDiscoveryStarted(p0: String?) {
                Log.d(TAG, "onDiscoveryStarted")
            }

            override fun onDiscoveryStopped(p0: String?) {
                Log.d(TAG, "onDiscoveryStopped")
            }

            override fun onServiceLost(p0: NsdServiceInfo?) {
                Log.d(TAG, "onServiceLost")
            }
        }

        resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo?, value: Int) {
                Log.d(TAG, "onResolveFailed : ${info.toString()}")
            }

            override fun onServiceResolved(info: NsdServiceInfo?) {
                Log.d(TAG, "Resolves -> ServiceName : ${info?.serviceName}, Host : ${info?.host}, Port : ${info?.port}")

                if (info is NsdServiceInfo) {
                    devices.add(HostInfo(info.serviceName, info.host, info.port))
                }
                nextResolve()
            }
        }
    }
}
