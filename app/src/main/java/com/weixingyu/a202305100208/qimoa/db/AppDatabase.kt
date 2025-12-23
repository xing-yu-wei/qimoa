package com.weixingyu.a202305100208.qimoa.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.weixingyu.a202305100208.qimoa.bean.Record

// 声明包含哪些表，版本号是1
@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordDao(): RecordDao

    companion object {
        // 单例模式：保证整个App只有一个数据库连接，防止冲突
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qimoa_database" // 数据库文件的名字
                )
                    .allowMainThreadQueries() // 【重要】允许在主线程读写，为了作业简单不报错，这里开了后门
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}