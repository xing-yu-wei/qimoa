package com.weixingyu.a202305100208.qimoa.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.weixingyu.a202305100208.qimoa.bean.Record

@Dao
interface RecordDao {
    // 插入一笔新账单
    @Insert
    fun insertRecord(record: Record)

    // 删除一笔账单
    @Delete
    fun deleteRecord(record: Record)

    // 查询所有账单：按时间倒序排列 (最新的在最前面)
    @Query("SELECT * FROM record ORDER BY time DESC")
    fun getAllRecords(): List<Record>

    // 查询某一类账单的总金额 (比如查查这个月一共花了多少钱)
    // 这里的 SUM(amount) 就是求和
    @Query("SELECT SUM(amount) FROM record WHERE type = :type")
    fun getSumByType(type: Int): Double
}