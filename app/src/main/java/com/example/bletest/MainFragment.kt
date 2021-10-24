package com.example.bletest

import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bletest.databinding.MainFragmentBinding
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {

    private var binding: MainFragmentBinding by viewLifecycle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bleScanButton.setOnClickListener {
            checkBluetoothAvailability(
                onSuccess = {
                    val directions = MainFragmentDirections.toBleScannerFragment()
                    findNavController().navigate(directions)
                },
                onFailure = {
                    Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
                    Log.d(TAG, getString(it))
                }
            )
        }
    }

    private fun checkBluetoothAvailability(onSuccess: () -> Unit, onFailure: (failureResId: Int) -> Unit) {
        if (!hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            onFailure(R.string.no_ble_support)
            return
        }
        val bluetoothManager = ContextCompat.getSystemService(requireContext(), BluetoothManager::class.java) as BluetoothManager
        if (bluetoothManager.adapter == null) {
            onFailure(R.string.no_bl_adapter)
            return
        }
        onSuccess()
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}