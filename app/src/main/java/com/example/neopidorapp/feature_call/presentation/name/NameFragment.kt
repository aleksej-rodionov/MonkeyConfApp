package com.example.neopidorapp.feature_call.presentation.name

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.neopidorapp.CallActivity
import com.example.neopidorapp.R
import com.example.neopidorapp.databinding.FragmentNameBinding
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.flow.collectLatest

class NameFragment: Fragment(R.layout.fragment_name) {

    private val vm: NameViewModel by viewModels()

    private var _binding: FragmentNameBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNameBinding.bind(view)

        initObservers()
        initListeners()
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.nameEvent.collectLatest { event ->
                when (event) {
                    is NameEvent.EnterClick -> {
                        findNavController().navigate(NameFragmentDirections.actionNameFragmentToCallFragment(event.username))
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun initListeners() {
        binding.etUsername.addTextChangedListener {
            vm.updateName(it.toString())
        }

        binding.btnEnter.setOnClickListener {

            PermissionX.init(this)
                .permissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                ).request { allGranted, _, _ ->
                    if (allGranted) {
                        vm.onEnterClick()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "You shoulg accept all permissions",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.etUsername.setText(vm.username)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}