package com.weixingyu.a202305100208.qimoa.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.weixingyu.a202305100208.qimoa.databinding.FragmentHomeBinding
import com.weixingyu.a202305100208.qimoa.db.AppDatabase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 适配器
    private val adapter = RecordAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化 RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 2. 点击+号跳转
        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireActivity(), AddRecordActivity::class.java)
            startActivity(intent)
        }

        // 3. 首次加载数据
        loadData()
    }

    // 当页面重新可见时（比如从记账页回来），刷新数据
    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        // 从数据库查出所有数据
        val records = AppDatabase.getDatabase(requireContext()).recordDao().getAllRecords()
        // 塞给适配器显示
        adapter.submitList(records)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}