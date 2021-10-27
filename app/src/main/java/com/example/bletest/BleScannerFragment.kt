package com.example.bletest

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.example.bletest.databinding.BleScannerFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BleScannerFragment : Fragment() {

    private var binding: BleScannerFragmentBinding by viewLifecycle()
    private lateinit var viewModel: BleScannerViewModel
    private var resultAdapter: ScannerResultAdapter? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = ContextCompat.getSystemService(requireContext(), BluetoothManager::class.java) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }
    private var isScanning = false

    private val bluetoothResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "Bluetooth enabling rejected")
            findNavController().popBackStack()
        } else {
            Log.d(TAG, "Bluetooth enabling accepted")
            startLeScanner()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
        if (isGranted) {
            startLeScanner()
        }
    }

    private val leScanCallback = object: ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            viewModel.handleScannedDevice(result.device)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false

            val message = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> R.string.scan_failed_already_started
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> R.string.scan_failed_application_registration_failed
                SCAN_FAILED_FEATURE_UNSUPPORTED -> R.string.scan_failed_feature_unsupported
                SCAN_FAILED_INTERNAL_ERROR -> R.string.scan_failed_internal_error
                else -> R.string.scan_failed_internal_error
            }

            Log.d(TAG, getString(message))
            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BleScannerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(BleScannerViewModel::class.java)

        binding.grantLocationPermissionButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.devices.collect {
                    resultAdapter?.submitItems(it)
                }
            }
        }

        resultAdapter = ScannerResultAdapter {
            val directions = BleScannerFragmentDirections.toDeviceDetailsFragment(it.address)
            findNavController().navigate(directions)
        }
        binding.resultsRecyclerView.adapter = resultAdapter
    }

    override fun onResume() {
        super.onResume()

        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothResultLauncher.launch(enableBluetoothIntent)
        } else {
            startLeScanner()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLeScanner()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resultAdapter = null
    }

    private fun startLeScanner() {
        if (isScanning) return
        when {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                isScanning = true
                bluetoothLeScanner.startScan(leScanCallback)
                showDiscoveredDevices()
                lifecycleScope.launch {
                    delay(15_000)
                    stopLeScanner()
                }
            }
            else -> showLocationPermissionInfo()
        }
    }

    private fun stopLeScanner() {
        if (isScanning) {
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private fun showLocationPermissionInfo() {
        /*val transition = Slide(Gravity.END).apply {
            duration = R.integer.anim_duration_short.toLong()
            addTarget(binding.locationPermissionInfoGroup)
        }
        TransitionManager.beginDelayedTransition(binding.root, transition)*/
        binding.resultsRecyclerView.visibility = View.GONE
        binding.locationPermissionInfoGroup.visibility = View.VISIBLE
    }

    private fun showDiscoveredDevices() {
        binding.locationPermissionInfoGroup.visibility = View.GONE
        binding.resultsRecyclerView.visibility = View.VISIBLE
    }

    companion object {
        const val TAG = "BleScanner"
    }
}