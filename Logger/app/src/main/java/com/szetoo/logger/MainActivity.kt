package com.szetoo.logger


import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_new_database.*
import kotlinx.android.synthetic.main.dialog_value_adder.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.lang.StringBuilder

// initiate global variables
public class MyApplication : Application() {
    var selectedTable: String = "NONE"
    var globalDatasetNames: ArrayList<String> = ArrayList()
    var homeRefreshFlag: Boolean = false
}

class MainActivity : AppCompatActivity(), MyHomeAdapter.OnHomeEntryListener  {

    private lateinit var recyclerView: RecyclerView // The recycler view UI component
    private lateinit var viewAdapter: RecyclerView.Adapter<*> // Adapter for the recyclerview
    private lateinit var viewManager: RecyclerView.LayoutManager // the layout manager

    var mDatasetNames: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // load saved dataset names, saved in shared preferences --> STORE_NAMES --> dataNames
        val settingsShared = getSharedPreferences("STORED_NAMES", 0)
        val mDatasetNamesString =
            settingsShared.getString("dataNames", "NONE").toString().split(";")

        if (mDatasetNamesString[0] != "NONE") {
            mDatasetNames.addAll(mDatasetNamesString)
        }

        (this.application as MyApplication).globalDatasetNames = mDatasetNames
        (this.application as MyApplication).homeRefreshFlag = false

        // RecyclerView
        // Clickable items created using guide https://www.youtube.com/watch?v=69C1ljfDvl0
        // Don't forget to add implements to the main activity class MainActivity:AppCompatActivity(),***MyHomeAdapter.OnHomeEntryListener**

        viewManager = GridLayoutManager(this, 2)
        viewAdapter = MyHomeAdapter(mDatasetNames, this)

        recyclerView = findViewById<RecyclerView>(R.id.homeRV).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        // Set up buttons
        btnAddNew.setOnClickListener {
            // Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_database, null)
            //AlertDialog builder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Track new category")
            val mAlertDialog = mBuilder.show()


            mDialogView.dialogBtnCancel.setOnClickListener {
                mAlertDialog.dismiss()
            }

            mDialogView.dialogBtnAdd.setOnClickListener {
                var mDatasetNamesUnique = true

                val grabbedName = mAlertDialog.addNewNameET.text.toString().trim()

                if (grabbedName.isBlank()) {
                    mDatasetNamesUnique = false
                    mAlertDialog.dismiss()
                }

                for (name in mDatasetNames) {
                    if (grabbedName.toUpperCase() == name.toUpperCase()) {
                        mDatasetNamesUnique = false
                    }
                }

                if (mDatasetNamesUnique) {

                    var nameStringBuilder = StringBuilder()
                    for (name in mDatasetNames) {
                        nameStringBuilder.append("$name;")
                    }
                    nameStringBuilder.append(grabbedName)
                    val editor = settingsShared.edit()
                    editor.putString("dataNames", nameStringBuilder.toString())
                    editor.commit()
                    Toast.makeText(this, "CREATED", Toast.LENGTH_SHORT).show()
                    mAlertDialog.dismiss()
                    recreate()
                } else{
                    Toast.makeText(this, "Name is not unique", Toast.LENGTH_SHORT).show()
                }
                mAlertDialog.dismiss()
            }

        }

    }


    override fun onResume() {
        super.onResume()
        if ((this.application as MyApplication).homeRefreshFlag){
            recreate()  //restart home screen if dataset was deleted
        }
    }

    override fun onHomeEntryClick(position: Int) {
        (this.application as MyApplication).selectedTable = mDatasetNames[position]
        val intentViewDataSet = Intent(this, ViewPlotActivity::class.java)
        startActivity(intentViewDataSet)
    }

}


fun dateStr2UnixTime(dateStr: String): String {
    val formatter: DateFormat = SimpleDateFormat("yyyyMMdd_HH:mm:ss")
    val date = formatter.parse(dateStr) as Date
    val output = date.time / 1000L
    val unixTimeStr = output.toString()
    return unixTimeStr
}

fun unixTime2SimpleDate(unixT: Float): String {
    val unixTL = unixT.toLong() * 1000
    val date = java.util.Date(unixTL)
    return SimpleDateFormat("dd/MM").format(date)

}
