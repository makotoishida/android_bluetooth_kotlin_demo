package com.example.bluetoothkotlindemo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


private const val SCAN_PERIOD: Long = 30000 // 最長30秒で自動停止。

class DeviceScanActivity : AppCompatActivity() {

    private lateinit var mLeDeviceListAdapter: LeDeviceListAdapter
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mHandler: Handler
    private var mScanning: Boolean = false
    private lateinit var mBtnStartScan: Button
    private lateinit var mBtnStopScan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device_scan)
        actionBar?.setTitle(R.string.device_scan_title)

        mHandler = Handler()

        mBtnStartScan = findViewById<Button>(R.id.btnStartScan)
        mBtnStartScan.setOnClickListener { startScan() }

        mBtnStopScan = findViewById<Button>(R.id.btnStopScan)
        mBtnStopScan.setOnClickListener { stopScan() }

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter


        val lstDevices = findViewById<ListView>(R.id.lstDevices)
        mLeDeviceListAdapter = LeDeviceListAdapter()
        lstDevices.adapter = mLeDeviceListAdapter
        lstDevices.setOnItemClickListener { l: AdapterView<*>?, v: View?, position: Int, id: Long -> onListItemClick(l, v, position, id) }

    }

    override fun onResume() {
        super.onResume()
        startScan()
    }

    override fun onPause() {
        super.onPause()
        stopScan()
    }

    private fun startScan() {
        Log.d("Debug", "startScan")

        // 一定時間後に自動停止。
        mHandler.postDelayed({ stopScan() }, SCAN_PERIOD)

        mLeDeviceListAdapter.clear()
        mBluetoothAdapter.startLeScan(leScanCallback)
        mScanning = true

        mBtnStartScan.isEnabled = false
        mBtnStopScan.isEnabled = true
    }

    private fun stopScan() {
        Log.d("Debug", "stopScan")

        mBluetoothAdapter.stopLeScan(leScanCallback)
        mScanning = false

        mBtnStartScan.isEnabled = true
        mBtnStopScan.isEnabled = false
    }

    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            mLeDeviceListAdapter.addDevice(device)
        }
    }

    protected fun onListItemClick(l: AdapterView<*>?, v: View?, position: Int, id: Long) {
        val device: BluetoothDevice = mLeDeviceListAdapter.getDevice(position) ?: return
        val intent = Intent(this, DeviceDetailActivity::class.java)
        intent.putExtra(DEVICE_NAME, device.name)
        intent.putExtra(DEVICE_ADDRESS, device.address)
        if (mScanning) {
            stopScan()
        }
        startActivity(intent)
    }

    // Adapter for holding devices found through scanning.
    private inner class LeDeviceListAdapter : BaseAdapter() {
        private val mLeDevices: ArrayList<BluetoothDevice> = ArrayList()
        private val mInflater: LayoutInflater = this@DeviceScanActivity.layoutInflater

        fun addDevice(device: BluetoothDevice) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device)
                notifyDataSetChanged()
            }
        }

        fun getDevice(position: Int): BluetoothDevice {
            return mLeDevices[position]
        }

        fun clear() {
            mLeDevices.clear()
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return mLeDevices.size
        }

        override fun getItem(i: Int): Any {
            return mLeDevices[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup?): View {
            var view = view
            val viewHolder: ViewHolder

            if (view == null) {
                view = mInflater.inflate(R.layout.listitem_device, null)
                viewHolder = ViewHolder()
                viewHolder.deviceAddress = view.findViewById<TextView>(R.id.device_address)
                viewHolder.deviceName = view.findViewById<TextView>(R.id.device_name)
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }

            val device = mLeDevices[i]
            val deviceName = device.name
            viewHolder.deviceName?.text = if (deviceName != null && deviceName.length > 0) deviceName else device.address
            viewHolder.deviceAddress?.text = device.address

            return view!!
        }

    }

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }
}