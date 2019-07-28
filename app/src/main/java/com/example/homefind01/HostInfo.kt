package com.example.homefind01

import java.net.InetAddress

class HostInfo(name:String, ipAddress:InetAddress, port:Int) {
    var Name: String = name
    var IpAddress: InetAddress? = ipAddress
    var Port: Int = port
}
