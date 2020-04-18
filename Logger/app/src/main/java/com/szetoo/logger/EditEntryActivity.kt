package com.szetoo.logger

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_edit_entry.*
import java.lang.Double
import java.text.SimpleDateFormat
import java.util.*

class EditEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)


        val orangy = "#e76c36"
        val currentDataSet = (this.application as MyApplication).selectedTable
        setTitle(currentDataSet)


        val mDatabase = DatabaseHandler(this,currentDataSet)
        val currentID = intent.getStringExtra(EXTRA_MESSAGE_ID).toInt()

        val currentDataEntry = mDatabase.getValue(currentID)

        entryIdTV.text = currentDataEntry.id.toString()
        entryNewValueTV.hint= currentDataEntry.value.toString()
        entryDateTV.text = ReformatDate(currentDataEntry.date)
        entryTimeTV.text = ReformatTime(currentDataEntry.date)

        val date = currentDataEntry.date.substring(0,8)
        val time = currentDataEntry.date.substring(9,17)
        var dataValue = currentDataEntry.value.toString()

        val mYear = date.substring(0,4).toInt()
        val mMonth = date.substring(4,6).toInt() -1
        val mDay = date.substring(6,8).toInt()
        val mHour = time.substring(0,2).toInt()
        val mMinute = time.substring(3,5).toInt()


        val c = Calendar.getInstance()

        c.set(Calendar.HOUR_OF_DAY, mHour)
        c.set(Calendar.MINUTE, mMinute)
        c.set(Calendar.SECOND, time.substring(6,8).toInt())

        c.set(Calendar.DAY_OF_MONTH, mDay)
        c.set(Calendar.MONTH, mMonth)
        c.set(Calendar.YEAR, mYear)

        // Clock
        btnEditTime.setOnClickListener{
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                entryTimeTV.text = SimpleDateFormat("HH:mm").format(c.time)
                entryTimeTV.setTextColor(Color.parseColor(orangy))
            }
            TimePickerDialog(this, timeSetListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }

        btnEditDate.setOnClickListener{
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view: DatePicker?, pYear: Int, pMonth: Int, pDay: Int ->
                c.set(Calendar.YEAR, pYear)
                c.set(Calendar.MONTH, pMonth)
                c.set(Calendar.DAY_OF_MONTH, pDay)
                entryDateTV.text = SimpleDateFormat("dd-MM-yyyy").format(c.time)
                entryDateTV.setTextColor(Color.parseColor(orangy))
            },
                mYear, mMonth, mDay
            )
            // show dialog
            dpd.show()
        }

        btnSaveEdit.setOnClickListener {
            val grabedNumberTxt = entryNewValueTV.text.toString()
            if (!grabedNumberTxt.isBlank()) {
                val newNumber = Double.parseDouble(grabedNumberTxt)
                dataValue = grabedNumberTxt
                currentDataEntry.value = dataValue.toDouble()
            }
            if (!grabedNumberTxt.isBlank() || date2str(c.time) !=  currentDataEntry.date ) {
            currentDataEntry.date =  date2str(c.time)
            mDatabase.updateEntry(currentDataEntry)
            Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show()
            finish()
            } else {
                finish()
            }
        }

        btnDeleteEntry.setOnClickListener {
            val mAlertDialog = AlertDialog.Builder(this)
            mAlertDialog.setTitle("Confirm")
            mAlertDialog.setMessage("Are you sure you want to delete this entry?")
            mAlertDialog.setPositiveButton("DELETE"){dialog, id ->
                mDatabase.deleteEntry(currentDataEntry.id)
                Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            mAlertDialog.setNegativeButton("Cancel"){_,_ ->
                            }
            mAlertDialog.show()
                }
    }

}

