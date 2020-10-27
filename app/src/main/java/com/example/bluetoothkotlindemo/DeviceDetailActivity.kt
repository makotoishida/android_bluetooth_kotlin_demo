package com.example.bluetoothkotlindemo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothkotlindemo.BluetoothLeService.LocalBinder


class DeviceDetailActivity : AppCompatActivity() {

    private var mConnected: Boolean = false
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()
    private lateinit var mDeviceAddress: String
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    private lateinit var mTxtConnectionState: TextView
    private lateinit var mTxtReceived: TextView
    private lateinit var mGattServicesList: ExpandableListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_detail)

        val deviceName = intent?.getStringExtra(DEVICE_NAME) ?: ""
        mDeviceAddress = intent?.getStringExtra(DEVICE_ADDRESS) ?: ""
        Log.d("Debug", "Device Name: ${deviceName}, Address: ${mDeviceAddress}")

        val txtDeviceName = findViewById<TextView>(R.id.txtDeviceName)
        txtDeviceName.setText(deviceName)

        val txtDeviceAddress = findViewById<TextView>(R.id.txtDeviceAddress)
        txtDeviceAddress.setText(mDeviceAddress)

        actionBar?.setTitle(deviceName)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mTxtConnectionState = findViewById(R.id.txtConnectionState)
        mTxtReceived = findViewById(R.id.txtReceived)

        mGattServicesList = findViewById<View>(R.id.gatt_services_list) as ExpandableListView
        mGattServicesList.setOnChildClickListener(servicesListClickListner)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
        Log.d("Debug", "BluetoothLEService bind done.")
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d("Debug", "Connect request result=$result")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
        Log.d("Debug", "BluetoothLEService bind released.")
    }

    // Code to manage Service lifecycle.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Log.d("Debug", "mBluetoothLeService onServiceConnected.")

            mBluetoothLeService = (service as LocalBinder).service
            if (!(mBluetoothLeService?.initialize() ?: false)) {
                Log.e("Debug", "Unable to initialize Bluetooth")
                return
            }
            Log.e("Debug", "mBluetoothLeService initialized successfully.")

            // Automatically connects to the device upon successful start-up initialization.
            Log.d("Debug", "mBluetoothLeService connecting to device ${mDeviceAddress}.")
            mBluetoothLeService?.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d("Debug", "BluetoothLEService onServiceDisconnected.")
            mBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a result of read or notification operations.
    private val gattUpdateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            Log.d("Debug", "gattUpdateReceiver.onReceive: action=${action}")

            when (action){
                ACTION_GATT_CONNECTED -> {
                    mConnected = true
                    updateConnectionState(R.string.connected)
                }
                ACTION_GATT_DISCONNECTED -> {
                    mConnected = false
                    updateConnectionState(R.string.disconnected)
                    clearUI()
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    displayGattServices(mBluetoothLeService?.getSupportedGattServices())
                }
                ACTION_DATA_AVAILABLE -> {
                    var hexStr = intent.getStringExtra(EXTRA_DATA)
                    var str = intent.getStringExtra(EXTRA_DATA_STRING)
                    displayData("${str}\n${hexStr}")
                }
            }
        }
    }

    private fun updateConnectionState(resourceId: Int) {
        runOnUiThread { mTxtConnectionState.setText(resourceId) }
    }

    private fun displayData(data: String?) {
        if (data != null) {
            mTxtReceived.setText(data)
        }
    }

    private fun clearUI() {
        mGattServicesList.setAdapter(null as SimpleExpandableListAdapter?)
        mTxtReceived.setText("")
    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private val servicesListClickListner = OnChildClickListener { parent, v, groupPosition, childPosition, id ->
        if (mGattCharacteristics == null) {
            return@OnChildClickListener false
        }

        val characteristic = mGattCharacteristics[groupPosition][childPosition]
        val charaProp = characteristic.properties
        if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService!!.setCharacteristicNotification(mNotifyCharacteristic!!, false)
                mNotifyCharacteristic = null
            }
            mBluetoothLeService!!.readCharacteristic(characteristic)
        }
        if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
            mNotifyCharacteristic = characteristic
            mBluetoothLeService!!.setCharacteristicNotification(
                    characteristic, true)
        }
        return@OnChildClickListener true
    }


    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private fun displayGattServices(gattServices: List<BluetoothGattService?>?) {
        if (gattServices == null) return
        var uuid: String?
        val unknownServiceString: String = resources.getString(R.string.unknown_service)
        val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
                mutableListOf()
        mGattCharacteristics = arrayListOf<ArrayList<BluetoothGattCharacteristic>>()

        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            uuid = gattService?.uuid?.toString()
            currentServiceData[LIST_NAME] = GattAttributes.lookup(uuid, "${unknownServiceString} ${gattCharacteristicData.size}")
            currentServiceData[LIST_UUID] = uuid ?: ""
            gattServiceData += currentServiceData

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristics = gattService?.characteristics ?: arrayListOf()
            val charas: ArrayList<BluetoothGattCharacteristic> = arrayListOf<BluetoothGattCharacteristic>()

            // Loops through available Characteristics.
            gattCharacteristics.forEach { gattCharacteristic ->
                charas += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                uuid = gattCharacteristic.uuid.toString()
                currentCharaData[LIST_NAME] = "  " + GattAttributes.lookup(uuid, "${unknownCharaString} ${gattCharacteristicGroupData.size }")
                currentCharaData[LIST_UUID] = "  " + (uuid ?: "")
                gattCharacteristicGroupData += currentCharaData
            }
            mGattCharacteristics.add(charas)
            gattCharacteristicData += gattCharacteristicGroupData
        }

        val gattServiceAdapter = SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2, arrayOf(LIST_NAME, LIST_UUID), intArrayOf(android.R.id.text1, android.R.id.text2),
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2, arrayOf(LIST_NAME, LIST_UUID), intArrayOf(android.R.id.text1, android.R.id.text2))
        mGattServicesList.setAdapter(gattServiceAdapter)

    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_GATT_CONNECTED)
        intentFilter.addAction(ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(ACTION_DATA_AVAILABLE)
        return intentFilter
    }

}