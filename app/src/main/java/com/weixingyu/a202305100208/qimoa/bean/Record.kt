package com.weixingyu.a202305100208.qimoa.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity 表示这就这是一张数据库表，表名叫 "record"
@Entity(tableName = "record")
data class Record(
    // 主键，自动增长（不用我们管id，它自己会变 1,2,3...）
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 金额 (比如 25.5)
    val amount: Double,

    // 类型：1 代表收入，2 代表支出 (简单吧？)
    val type: Int,

    // 分类名称：比如 "餐饮", "交通", "兼职"
    val category: String,

    // 备注：比如 "吃黄焖鸡米饭"
    val note: String,

    // 时间：存时间戳 (比如 1703456789000)，方便排序
    val time: Long
)