package com.weixingyu.a202305100208.qimoa.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weixingyu.a202305100208.qimoa.bean.Record
import com.weixingyu.a202305100208.qimoa.databinding.ActivityAddRecordBinding
import com.weixingyu.a202305100208.qimoa.db.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding

    // 定义一些常见的分类
    private val categories = listOf("餐饮", "交通", "购物", "娱乐", "医疗", "工资", "兼职", "其他")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 设置分类下拉框 (Spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter

        // 2. 点击保存按钮
        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun saveRecord() {
        // 获取输入的数据
        val amountStr = binding.etAmount.text.toString()
        val note = binding.etNote.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()

        // 判断是支出(2) 还是 收入(1)
        val type = if (binding.rbExpense.isChecked) 2 else 1

        // 校验：金额不能为空
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()

        // 3. 创建一个 Record 对象
        val record = Record(
            amount = amount,
            type = type,
            category = category,
            note = note,
            time = System.currentTimeMillis() // 当前时间
        )

        // 4. 写入数据库 (使用协程在后台线程执行)
        GlobalScope.launch {
            AppDatabase.getDatabase(this@AddRecordActivity).recordDao().insertRecord(record)

            // 存完之后，切回主线程提示用户，并关闭页面
            runOnUiThread {
                Toast.makeText(this@AddRecordActivity, "记账成功！", Toast.LENGTH_SHORT).show()
                finish() // 关闭当前页面，返回上一页
            }
        }
    }
}