package com.example.bluetoothkotlindemo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)

        val deviceName = intent?.getStringExtra(DEVICE_NAME) ?: ""
        val deviceAddress = intent?.getStringExtra(DEVICE_ADDRESS) ?: ""
        Log.d("Debug", "Device Name: ${deviceName}, Address: ${deviceAddress}")

        val txtDeviceName = findViewById<TextView>(R.id.txtDeviceName)
        txtDeviceName.setText(deviceName)

        val txtDeviceAddress = findViewById<TextView>(R.id.txtDeviceAddress)
        txtDeviceAddress.setText(deviceAddress)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnDisconnect = findViewById<Button>(R.id.btnDisconnect)
        val txtReceived = findViewById<TextView>(R.id.txtReceived)
        val txtSend = findViewById<EditText>(R.id.txtSend)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnConnect.setOnClickListener { connect(deviceAddress) }
        btnDisconnect.setOnClickListener { disconnect() }
        btnSend.setOnClickListener { send() }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

    }

    fun connect(address: String){
        Log.d("Debug", "connect")

        val device: BluetoothDevice = mBluetoothAdapter.getRemoteDevice(address)
        Log.d("Debug", "Device: ${device.name}")

        // Bluetoothデバイスの種別に応じた接続処理。
        // 参考：
        // https://developer.android.com/guide/topics/connectivity/bluetooth-le?hl=ja
        // https://jitaku.work/it/bluetooth/matome1/
        // https://www.hiramine.com/programming/bluetoothcommunicator/03_connect_disconnect.html
        // https://sites.google.com/a/gclue.jp/fab-zang-docs/sumafo-lian-xie/02-arduino-android
        // https://github.com/bauerjj/Android-Simple-Bluetooth-Example/blob/master/app/src/main/java/com/mcuhq/simplebluetooth/MainActivity.java
        // http://mcuhq.com/27/simple-android-bluetooth-application-with-arduino-example

    }

    fun disconnect(){
        Log.d("Debug", "disconnect")
    }

    fun send(){
        Log.d("Debug", "send")
    }
}