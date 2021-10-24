package com.example.bletest

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bletest.ScannerResultAdapter.ViewHolder
import com.example.bletest.databinding.ScanResultItemBinding

class ScannerResultAdapter(private val onClickListener: (device: BluetoothDevice) -> Unit) : RecyclerView.Adapter<ViewHolder>() {

    private var items: List<BluetoothDevice> = emptyList()

    fun submitItems(list: List<BluetoothDevice>) {
        val oldSize = items.size
        items = list
        if (list.isEmpty()) {
            notifyItemRangeRemoved(0, oldSize)
        } else {
            notifyItemRangeInserted(oldSize, list.size - oldSize)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ScanResultItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ScanResultItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onClickListener(items[adapterPosition])
                }
            }
        }

        fun bind(item: BluetoothDevice) {
            binding.nameTextView.text = item.name
            binding.addressTextView.text = item.address
        }
    }
}