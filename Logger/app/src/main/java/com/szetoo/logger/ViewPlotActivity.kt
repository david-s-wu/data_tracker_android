package com.szetoo.logger

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_main.btnAddNew
import kotlinx.android.synthetic.main.activity_view_plot.*
import kotlinx.android.synthetic.main.activity_view_plot.titleTV
import kotlinx.android.synthetic.main.dialog_value_adder.*
import kotlinx.android.synthetic.main.dialog_value_adder.view.*
import java.lang.Double
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*


class ViewPlotActivity : AppCompatActivity() {


    private val mDataArray: ArrayList<LoggingData> = ArrayList()

    var NavigateAwayFlag = false    // used for refreshing plot


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_plot)

        NavigateAwayFlag = false  // reset flag

        val currentDataSet = (this.application as MyApplication).selectedTable  // grab global variable
        val mDatabase = DatabaseHandler(this, currentDataSet)   // setup database handler for current dataset table

        title = currentDataSet   // set activity title at top of screen
        titleTV.text = "$currentDataSet Data"  // set title text view in layout

        mDataArray.clear()
        mDataArray.addAll(mDatabase.allDataValues)
        mDataArray.sortByDescending { it.date }

        // Check if database is empty and plot data using MPAndroid Chart (https://github.com/PhilJay/MPAndroidChart) if not
        if (mDataArray.isEmpty() != true){
            val lineChartView = findViewById<LineChart>(R.id.dataGraph)

            val refTime = dateStr2UnixTime(mDataArray.last().date).toFloat()  // set first data point as reference time t = 0

            val dataEntries = mDataArray.mapIndexed { index, _ ->
                val reducedTime = dateStr2UnixTime(mDataArray[index].date).toFloat() - refTime   // subtract first entry unix time to reduce length of numbers
                Entry(reducedTime , mDataArray[index].value.toFloat())
            }

            val lineDataSet = LineDataSet(dataEntries.sortedBy { it.x }, "")  // Have to add entires in ascending x order!!!
            lineDataSet.color = Color.parseColor("#eb895e")
            lineDataSet.setCircleColor(Color.parseColor("#3e5862"))
            lineDataSet.setDrawValues(false)
            lineDataSet.lineWidth = 3.0f
            lineDataSet.circleHoleRadius = 6.0f
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT)

            val lineData = LineData(lineDataSet)
            lineChartView.data = lineData

            // Format x axis ad simple dates
            class MyXAxisFormatter : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return unixTime2SimpleDate(refTime+value)
                }
            }
            lineChartView.xAxis.valueFormatter = MyXAxisFormatter()

            lineChartView.xAxis.textSize = 12f
            lineChartView.axisLeft.textSize = 12f
            lineChartView.axisRight.isEnabled = false
            lineChartView.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM

            // Remove legend and description text
            lineChartView.legend.isEnabled = false
            val description = Description()
            description.setText("")
            lineChartView.setDescription(description)


            // Set initial x limits if dataset too large
            val maxInitialXlimDays = 40f  // set max limit in days
            val maxInitialXlimSeconds = maxInitialXlimDays*24*60*60
            val maxT = dateStr2UnixTime(mDataArray.first().date).toFloat() - refTime
            val minT = maxT - maxInitialXlimSeconds
            val zoomYCentre = lineChartView.getViewPortHandler().getTransY().toFloat()    //get current y centre value
            val zoomXCentre= maxT-(maxInitialXlimSeconds/2)
            if (dateStr2UnixTime(mDataArray.first().date).toFloat() - refTime > maxInitialXlimSeconds) {
                lineChartView.zoom(maxT/(maxT-minT), 1.0f, -1*10f, zoomYCentre)
                lineChartView.moveViewToX(zoomXCentre) // Setting x centre in zoom doesnt work. Use seperate moveview to X as workaround.
            }


        }

        // Delete button set up
        btnDeleteSet.setOnClickListener {
            // Alert Dialog code adapted from https://devofandroid.blogspot.com/2018/03/create-alert-dialog-kotlin-android.html
            val mDelDialog = AlertDialog.Builder(this)
            mDelDialog.setIcon(R.mipmap.ic_launcher_round) //set alertdialog icon
            mDelDialog.setTitle("Confirm Delete:") //set alertdialog title
            mDelDialog.setMessage("Are you sure you want to remove the $currentDataSet Dataset?") //set alertdialog message
            mDelDialog.setPositiveButton("Yes") { dialog, id ->

                val mDatasetNames = (this.application as MyApplication).globalDatasetNames
                mDatabase.deleteAllEntries()
                mDatasetNames.remove(currentDataSet)
                // Need to set default string if no more datasets.
                if (mDatasetNames.isEmpty()){
                    mDatasetNames.add("NONE")
                }

                var nameStringBuilder = StringBuilder()
                for (name in mDatasetNames) {
                    nameStringBuilder.append("$name;")
                }
                val settingsShared = getSharedPreferences("STORED_NAMES", 0)
                val editor = settingsShared.edit()
                editor.putString("dataNames", nameStringBuilder.toString().dropLast(1))
                editor.commit()
                (this.application as MyApplication).homeRefreshFlag = true
                Toast.makeText(this, "$currentDataSet Dataset deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            mDelDialog.setNegativeButton("Cancel") { dialog, id ->
                //perform some tasks here
            }
            mDelDialog.show()
        }

        btnAddNew.setOnClickListener {
            // get current date and time
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val mTimeFormat = "HH:mm"
            val mDateFormat = "dd.MM.yyyy"
            val mBlack = "#000000"

            // Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_value_adder, null)
            //AlertDialog builder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Add value to database")
            val mAlertDialog = mBuilder.show()

            // Add custom response from buttons and fields

            //Date
            mAlertDialog.addDateTV.text = SimpleDateFormat(mDateFormat).format(c.time)
            mAlertDialog.dialogBtnChangeDate.setOnClickListener{
                val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view: DatePicker?, mYear: Int, mMonth: Int, mDay: Int ->
                    c.set(Calendar.YEAR, mYear)
                    c.set(Calendar.MONTH, mMonth)
                    c.set(Calendar.DAY_OF_MONTH, mDay)
                    mAlertDialog.addDateTV.text = SimpleDateFormat("dd.MM.yyyy").format(c.time)
                    mAlertDialog.addDateTV.setTextColor(Color.parseColor(mBlack))
                },
                    year, month, day
                )
                dpd.show()
            }


            // Time
            mAlertDialog.addTimeTV.text = SimpleDateFormat(mTimeFormat).format(c.time)

            mAlertDialog.dialogBtnChangeTime.setOnClickListener{
                val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    c.set(Calendar.HOUR_OF_DAY, hour)
                    c.set(Calendar.MINUTE, minute)
                    mAlertDialog.addTimeTV.text = SimpleDateFormat("HH:mm").format(c.time)
                    mAlertDialog.addTimeTV.setTextColor(Color.parseColor(mBlack))
                }
                TimePickerDialog(this, timeSetListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
            }

            // Cancel Button
            mDialogView.dialogBtnCancel.setOnClickListener{
                mAlertDialog.dismiss()
            }

            // Add new value Button
            mDialogView.dialogBtnAdd.setOnClickListener{

                val grabedNumberTxt = mAlertDialog.addValueET.text.toString()
                if (grabedNumberTxt.isBlank()) {   //check if someting was actually entered
                    Toast.makeText(this, "No value entered", Toast.LENGTH_SHORT).show()
                } else {
                    val newNumber = Double.parseDouble(grabedNumberTxt)
                    val newEntry = LoggingData()
                    newEntry.value = newNumber
                    newEntry.date =  date2str(c.time)   // convert into common date string format
                    mDatabase.addValue(newEntry)
                    Toast.makeText(this, "Entry added to database", Toast.LENGTH_SHORT).show()
                    mAlertDialog.dismiss()
                    recreate()
                }

            }

        }

        btnViewDb.setOnClickListener{
            NavigateAwayFlag = true
            val intentViewDB = Intent(applicationContext, ViewDatabaseActivity::class.java)
            startActivity(intentViewDB)
        }



    }

    override fun onResume() {
        super.onResume()
        // Refresh data array and plot when resuming
        if (NavigateAwayFlag){
            recreate()
        }
        if ((this.application as MyApplication).homeRefreshFlag){
            finish()
        }


    }

}
