package com.example.bletest

import android.app.Application
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DeviceDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private var connectionManager: BleConnectionManager? = null

    fun connect(deviceAddress: String): Boolean {
        connectionManager?.close()
        return BleConnectionManager(context).let { ble ->
            connectionManager = ble
            listenForEvents(ble.eventsFlow)
            ble.connect(deviceAddress)
        }
    }

    private fun listenForEvents(flow: Flow<BleEvent>) {
        viewModelScope.launch {
            flow.collect {
                when (it) {
                    BleEvent.ServicesDiscovered -> {
                        val services = connectionManager?.getGattServices() ?: emptyList()
                        if (services.isEmpty()) {
                            Log.i("printGattTable", "No service and characteristic available, call discoverServices() first?")
                        }
                        services.forEach { service ->
                            val characteristicsTable = service.characteristics.joinToString(
                                separator = "\n|--",
                                prefix = "|--"
                            ) { it.uuid.toString() }
                            Log.i("printGattTable", "\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable"
                            )
                        }
                    }
                }
            }
        }
    }



    fun close() {
        connectionManager?.close()
    }
}