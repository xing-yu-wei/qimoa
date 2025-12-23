package com.weixingyu.a202305100208.qimoa.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.weixingyu.a202305100208.qimoa.bean.Record
import com.weixingyu.a202305100208.qimoa.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordAdapter : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    // 数据源：一开始是空的
    private var recordList = listOf<Record>()

    // 更新数据的方法
    fun submitList(list: List<Record>) {
        recordList = list
        notifyDataSetChanged() // 刷新列表
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = recordList[position]
        holder.bind(record)
    }

    override fun getItemCount() = recordList.size

    // ViewHolder 内部类
    class RecordViewHolder(private val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: Record) {
            binding.tvCategory.text = record.category
            binding.tvNote.text = if (record.note.isEmpty()) "无备注" else record.note

            // 格式化时间
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            binding.tvTime.text = sdf.format(Date(record.time))

            // 处理金额显示
            if (record.type == 1) { // 收入
                binding.tvAmount.text = "+${record.amount}"
                binding.tvAmount.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // 绿色
                binding.tvCategoryIcon.text = "收"
                binding.tvCategoryIcon.background.setTint(android.graphics.Color.parseColor("#4CAF50"))
            } else { // 支出
                binding.tvAmount.text = "-${record.amount}"
                binding.tvAmount.setTextColor(android.graphics.Color.parseColor("#F44336")) // 红色
                binding.tvCategoryIcon.text = record.category.first().toString() // 取首字
                binding.tvCategoryIcon.background.setTint(android.graphics.Color.parseColor("#2196F3"))
            }
        }
    }
}