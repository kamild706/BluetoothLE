package com.example.bletest

sealed class BleEvent {
    object Connected : BleEvent()
    object Disconnected : BleEvent()
    object ServicesDiscovered: BleEvent()
    object ConnectionChangeFailed : BleEvent()
    object CharacteristicRead : BleEvent()
    object CharacteristicWritten : BleEvent()
    object CharacteristicChanged : BleEvent()
}
