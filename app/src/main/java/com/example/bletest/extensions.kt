package com.example.bletest

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> Fragment.viewLifecycle(): ReadWriteProperty<Fragment, T> =
    object : ReadWriteProperty<Fragment, T>, DefaultLifecycleObserver {
        private var binding: T? = null
        init {
            // Observe the view lifecycle of the Fragment.
            // The view lifecycle owner is null before onCreateView and after onDestroyView.
            // The observer is automatically removed after the onDestroy event.
            this@viewLifecycle
                .viewLifecycleOwnerLiveData
                .observe(this@viewLifecycle, Observer { owner: LifecycleOwner? ->
                    owner?.lifecycle?.addObserver(this)
                })
        }

        override fun onDestroy(owner: LifecycleOwner) {
            binding = null
        }
        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>
        ): T {
            return this.binding ?: error("Called before onCreateView or after onDestroyView.")
        }
        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: T
        ) {
            this.binding = value
        }
    }

fun Fragment.hasSystemFeature(featureName: String): Boolean = requireActivity().packageManager.hasSystemFeature(featureName)

fun Fragment.checkSelfPermission(permissionName: String): Boolean {
    return when (ContextCompat.checkSelfPermission(requireContext(), permissionName)) {
        PackageManager.PERMISSION_GRANTED -> true
        else -> false
    }
}

infix fun <T> List<T>.add(item: T): List<T> {
    return this + listOf(item)
}
