package com.subefu.aquateka.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentOrderInfoBinding

class OrderInfoFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("Title") ?: ""
        binding.tvCustomerInfo.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "Title"
        private const val ARG_DESC = "arg_desc"

        fun newInstance(title: String, description: String): OrderInfoFragment {
            val fragment = OrderInfoFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_DESC, description)
            }
            fragment.arguments = args
            return fragment
        }
    }
}