package com.example.bletest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.bletest.databinding.DeviceDetailsFragmentBinding

class DeviceDetailsFragment : Fragment() {

    private var binding: DeviceDetailsFragmentBinding by viewLifecycle()
    private val args: DeviceDetailsFragmentArgs by navArgs()
    private lateinit var viewModel: DeviceDetailsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DeviceDetailsFragmentBinding.inflate(inflater, container ,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[DeviceDetailsViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        viewModel.connect(args.deviceAddress)
    }

    override fun onPause() {
        super.onPause()
        viewModel.close()
    }

}