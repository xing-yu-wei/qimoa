package com.weixingyu.a202305100208.qimoa.ui

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.weixingyu.a202305100208.qimoa.bean.Record
import com.weixingyu.a202305100208.qimoa.databinding.ActivityAddRecordBinding
import com.weixingyu.a202305100208.qimoa.db.AppDatabase
import com.weixingyu.a202305100208.qimoa.utils.OcrManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private val categories = listOf("餐饮", "交通", "购物", "娱乐", "医疗", "工资", "兼职", "其他")

    // 1. 定义相册选择器：选完图后自动调用 OCR
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            binding.etAmount.hint = "正在识别中..."
            // 调用我们写的工具类进行识别
            OcrManager.recognize(this, uri) { amount ->
                if (amount != null) {
                    binding.etAmount.setText(amount.toString())
                    Toast.makeText(this, "识别成功！", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "未找到金额，请手动输入", Toast.LENGTH_SHORT).show()
                    binding.etAmount.setText("")
                }
                binding.etAmount.hint = "金额 (元)"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化分类下拉框
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        // 2. 给相机按钮设置点击事件
        binding.btnOcr.setOnClickListener {
            // 打开相册，只显示图片
            pickImageLauncher.launch("image/*")
        }

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun saveRecord() {
        val amountStr = binding.etAmount.text.toString()
        val note = binding.etNote.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()
        val type = if (binding.rbExpense.isChecked) 2 else 1

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()

        val record = Record(
            amount = amount,
            type = type,
            category = category,
            note = note,
            time = System.currentTimeMillis()
        )

        GlobalScope.launch {
            AppDatabase.getDatabase(this@AddRecordActivity).recordDao().insertRecord(record)
            runOnUiThread {
                Toast.makeText(this@AddRecordActivity, "记账成功！", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}