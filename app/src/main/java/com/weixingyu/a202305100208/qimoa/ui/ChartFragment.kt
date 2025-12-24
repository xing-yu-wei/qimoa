package com.weixingyu.a202305100208.qimoa.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.weixingyu.a202305100208.qimoa.databinding.FragmentChartBinding
import com.weixingyu.a202305100208.qimoa.db.AppDatabase
import com.weixingyu.a202305100208.qimoa.bean.Record
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChartFragment : Fragment() {

    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    // 当前选中的类型：2=支出，1=收入
    private var currentType = 2
    // 当前选中的时间范围：0=本月，1=本周
    private var currentTimeScope = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPieChart()
        initLineChart()

        // 1. 监听收支切换
        binding.rgChartType.setOnCheckedChangeListener { _, checkedId ->
            currentType = if (checkedId == binding.rbExpenseChart.id) 2 else 1
            loadData()
        }

        // 2. 监听时间范围切换 (本月/本周)
        binding.rgTimeScope.setOnCheckedChangeListener { _, checkedId ->
            currentTimeScope = if (checkedId == binding.rbMonth.id) 0 else 1
            loadData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun initPieChart() {
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setCenterTextSize(16f)
        binding.pieChart.animateY(1000)
        // 禁止触摸旋转等 (可选，看你喜好)
        binding.pieChart.isRotationEnabled = true
    }

    private fun initLineChart() {
        binding.lineChart.description.isEnabled = false
        binding.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.lineChart.xAxis.setDrawGridLines(false)
        binding.lineChart.axisRight.isEnabled = false

        // 【解决你的问题1：禁止缩放和拖拽】
        binding.lineChart.setScaleEnabled(false) // 禁止缩放
        binding.lineChart.isDragEnabled = false // 禁止拖拽
        binding.lineChart.setPinchZoom(false) // 禁止双指缩放
        binding.lineChart.isDoubleTapToZoomEnabled = false // 禁止双击缩放

        binding.lineChart.animateX(1000)
    }

    private fun loadData() {
        val dao = AppDatabase.getDatabase(requireContext()).recordDao()
        val allRecords = dao.getAllRecords()

        // 筛选数据
        val filteredRecords = filterRecords(allRecords, currentType, currentTimeScope)

        if (filteredRecords.isEmpty()) {
            binding.pieChart.clear()
            binding.lineChart.clear()
            // 可以加个Toast提示无数据，或者不管它
            return
        }

        drawPieChart(filteredRecords)
        drawLineChart(filteredRecords)
    }

    // 【解决你的问题2：筛选本月或本周】
    private fun filterRecords(records: List<Record>, type: Int, timeScope: Int): List<Record> {
        val calendar = Calendar.getInstance()
        // 设置一周的第一天是周一 (中国习惯)
        calendar.firstDayOfWeek = Calendar.MONDAY

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        return records.filter { record ->
            val recordCal = Calendar.getInstance()
            recordCal.firstDayOfWeek = Calendar.MONDAY
            recordCal.timeInMillis = record.time

            val isSameType = (record.type == type)
            val isSameYear = (recordCal.get(Calendar.YEAR) == currentYear)

            if (timeScope == 0) {
                // 筛选本月
                isSameType && isSameYear && (recordCal.get(Calendar.MONTH) == currentMonth)
            } else {
                // 筛选本周
                isSameType && isSameYear && (recordCal.get(Calendar.WEEK_OF_YEAR) == currentWeek)
            }
        }
    }

    private fun drawPieChart(records: List<Record>) {
        val groupedMap = records.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = ArrayList<PieEntry>()
        for ((category, amount) in groupedMap) {
            entries.add(PieEntry(amount.toFloat(), category))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK

        binding.pieChart.data = PieData(dataSet)
        // 动态设置中间的文字
        val timeText = if(currentTimeScope == 0) "本月" else "本周"
        val typeText = if(currentType == 2) "支出" else "收入"
        binding.pieChart.centerText = "$timeText$typeText"

        binding.pieChart.invalidate()
    }

    private fun drawLineChart(records: List<Record>) {
        val sdf = SimpleDateFormat("d", Locale.getDefault())
        val sortedRecords = records.sortedBy { it.time }

        val dailyMap = sortedRecords.groupBy { sdf.format(Date(it.time)) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = ArrayList<Entry>()
        val xLabels = ArrayList<String>()

        var index = 0
        for ((dayStr, amount) in dailyMap) {
            entries.add(Entry(index.toFloat(), amount.toFloat()))
            xLabels.add("${dayStr}日")
            index++
        }

        val dataSet = LineDataSet(entries, "趋势") // 图例文字
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.valueTextSize = 12f
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(Color.parseColor("#2196F3"))
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(true) // 显示数值

        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        binding.lineChart.xAxis.labelCount = xLabels.size

        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}