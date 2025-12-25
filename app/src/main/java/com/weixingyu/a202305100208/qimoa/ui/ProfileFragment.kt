package com.weixingyu.a202305100208.qimoa.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.weixingyu.a202305100208.qimoa.databinding.FragmentProfileBinding
import com.weixingyu.a202305100208.qimoa.db.AppDatabase
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // 注册一个“创建文件”的启动器，用于导出 Excel/CSV
    private val createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                writeCsvToUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 设置预算按钮
        binding.btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        // 2. 导出 Excel 按钮
        binding.btnExportExcel.setOnClickListener {
            exportData()
        }

        // 3. 关于应用 - 点击弹出版本信息
        binding.btnAbout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("关于应用")
                .setMessage("个人财务管家 v1.0\n\n开发者：魏星宇\n学号：202305100208\n\n本应用用于期末作业答辩，严禁商用。")
                .setPositiveButton("知道了", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回来都刷新一下数据
        refreshBudgetView()
    }

    // === 预算管理逻辑 ===
    private fun refreshBudgetView() {
        // 1. 获取用户设置的预算 (默认2000)
        val sp = requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
        val budgetLimit = sp.getFloat("budget", 2000f)

        // 2. 计算本月总支出
        val dao = AppDatabase.getDatabase(requireContext()).recordDao()
        val allRecords = dao.getAllRecords()

        // 筛选本月支出
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentTotalExpense = allRecords.filter {
            val c = Calendar.getInstance()
            c.timeInMillis = it.time
            it.type == 2 && c.get(Calendar.MONTH) == currentMonth
        }.sumOf { it.amount }

        // 3. 更新UI
        binding.tvBudgetLimit.text = "总预算: ${budgetLimit.toInt()}"
        binding.tvExpenseStatus.text = "${String.format("%.2f", currentTotalExpense)} / ${budgetLimit.toInt()}"

        val progress = (currentTotalExpense / budgetLimit * 100).toInt()
        binding.progressBudget.progress = progress

        // 超支提醒 (变红)
        if (currentTotalExpense > budgetLimit) {
            binding.progressBudget.progressDrawable.setTint(android.graphics.Color.RED)
            binding.tvExpenseStatus.setTextColor(android.graphics.Color.RED)
        } else {
            binding.progressBudget.progressDrawable.setTint(android.graphics.Color.parseColor("#2196F3"))
            binding.tvExpenseStatus.setTextColor(android.graphics.Color.BLACK)
        }
    }

    private fun showSetBudgetDialog() {
        val editText = EditText(requireContext())
        editText.hint = "请输入预算金额"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(requireContext())
            .setTitle("设置本月预算")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val input = editText.text.toString()
                if (input.isNotEmpty()) {
                    val newBudget = input.toFloat()
                    // 保存到 SharedPreferences
                    val sp = requireContext().getSharedPreferences("config", Context.MODE_PRIVATE)
                    sp.edit().putFloat("budget", newBudget).apply()
                    refreshBudgetView()
                    Toast.makeText(requireContext(), "预算已更新", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // === 导出逻辑 (CSV格式，Excel能打开) ===
    private fun exportData() {
        // 弹出一个系统文件保存框，让用户选位置
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv" // 文件类型
            putExtra(Intent.EXTRA_TITLE, "我的账单_${SimpleDateFormat("yyyyMMdd").format(Date())}.csv")
        }
        createFileLauncher.launch(intent)
    }

    private fun writeCsvToUri(uri: Uri) {
        try {
            val outputStream = requireContext().contentResolver.openOutputStream(uri)
            val writer = BufferedWriter(OutputStreamWriter(outputStream, "GBK")) // 用GBK编码，防止Excel打开乱码

            // 1. 写表头
            writer.write("时间,收支类型,金额,分类,备注\n")

            // 2. 写数据
            val records = AppDatabase.getDatabase(requireContext()).recordDao().getAllRecords()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")

            for (record in records) {
                val timeStr = sdf.format(Date(record.time))
                val typeStr = if (record.type == 1) "收入" else "支出"
                // 写入一行：时间,支出,25.5,餐饮,好吃
                writer.write("$timeStr,$typeStr,${record.amount},${record.category},${record.note}\n")
            }

            writer.flush()
            writer.close()
            Toast.makeText(requireContext(), "导出成功！请在文件管理中查看", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}