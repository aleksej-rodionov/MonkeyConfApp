package com.example.neopidorapp.feature_call.presentation.name

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentNameBinding

class NameFragment: Fragment(R.layout.fragment_name) {

    private val vm: NameViewModel by viewModels()

    private var _binding: FragmentNameBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNameBinding.bind(view)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}