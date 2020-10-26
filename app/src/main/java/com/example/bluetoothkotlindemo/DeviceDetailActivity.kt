package com.example.bluetoothkotlindemo

import android.bluetooth.*
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private var mBluetoothLeService: BluetoothLeService? = null

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

        // Bluetoothデバイスの種別に応じた接続処理。
        // 参考：
        // https://developer.android.com/guide/topics/connectivity/bluetooth-le?hl=ja
        // https://jitaku.work/it/bluetooth/matome1/
        // https://www.hiramine.com/programming/bluetoothcommunicator/03_connect_disconnect.html
        // https://sites.google.com/a/gclue.jp/fab-zang-docs/sumafo-lian-xie/02-arduino-android
        // https://github.com/bauerjj/Android-Simple-Bluetooth-Example/blob/master/app/src/main/java/com/mcuhq/simplebluetooth/MainActivity.java
        // http://mcuhq.com/27/simple-android-bluetooth-application-with-arduino-example


        mBluetoothLeService = BluetoothLeService(null)
        val isConnected = mBluetoothLeService?.connect(address) ?: false
        if (isConnected) {
            mBluetoothLeService?.setCharacteristicNotification(BluetoothGattCharacteristic(UUID_HEART_RATE_MEASUREMENT, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ), true)
        }
    }

    fun disconnect(){
        Log.d("Debug", "disconnect")
        mBluetoothLeService?.setCharacteristicNotification(BluetoothGattCharacteristic(UUID_HEART_RATE_MEASUREMENT, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ), false)
        mBluetoothLeService?.disconnect()
    }

    fun send(){
        Log.d("Debug", "send")
//        mBluetoothLeService?.
    }
}