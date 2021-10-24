package com.example.bletest

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleScannerViewModel : ViewModel() {
    private val _devices = MutableStateFlow(emptyList<BluetoothDevice>())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    fun handleScannedDevice(device: BluetoothDevice) {
        val devices = _devices.value
        if (!devices.contains(device)) {
            _devices.value = devices add device
        }
    }
}