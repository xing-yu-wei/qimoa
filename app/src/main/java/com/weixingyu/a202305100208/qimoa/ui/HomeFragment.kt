package com.weixingyu.a202305100208.qimoa.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.weixingyu.a202305100208.qimoa.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    // 使用 ViewBinding (加分项：代码规范)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 后面我们会在这里写逻辑，比如显示列表
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}