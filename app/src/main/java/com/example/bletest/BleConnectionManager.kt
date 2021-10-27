package com.example.bletest

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class BleConnectionManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val eventChannel = Channel<BleEvent>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = ContextCompat.getSystemService(context, BluetoothManager::class.java) as BluetoothManager
        bluetoothManager.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        event(BleEvent.Connected)
                        Log.d(TAG, "device connected")
                        bluetoothGatt?.discoverServices()
                        Log.d(TAG, "services discovery started")
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        event(BleEvent.Disconnected)
                        Log.d(TAG, "device disconnected")
                    }
                }
            } else {
                event(BleEvent.ConnectionChangeFailed)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                event(BleEvent.ServicesDiscovered)
                Log.d(TAG, "services discovered")
            }
        }


    }

    fun getGattServices(): List<BluetoothGattService> {
        return bluetoothGatt?.services ?: emptyList()
    }

    fun connect(deviceAddress: String): Boolean {
        return try {
            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
            true
        } catch (exception: IllegalArgumentException) {
            false
        }
    }

    private fun event(event: BleEvent) {
        scope.launch {
            eventChannel.send(event)
        }
    }

    fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    companion object {
        private const val TAG = "BleConnectionManager"
    }
}