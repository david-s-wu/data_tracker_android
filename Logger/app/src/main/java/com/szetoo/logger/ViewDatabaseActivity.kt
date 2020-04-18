package com.szetoo.logger

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_view_database.*
import java.io.File
import java.io.FileOutputStream


const val EXTRA_MESSAGE_ID = "com.szetoo.logger.MESSAGE_ID"
val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE : Int = 99

class ViewDatabaseActivity : AppCompatActivity(),  MyAdapter.OnEntryListener  {

    private lateinit var recyclerView: RecyclerView // The recycler view UI component
    private lateinit var viewAdapter: RecyclerView.Adapter<*> // Adapter for the recyclerview
    private lateinit var viewManager: RecyclerView.LayoutManager // the layout manager

    private val mDataArray: ArrayList<LoggingData> = ArrayList()

    var NavigateAwayFlag = false    // used for refreshing plot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_database)

        NavigateAwayFlag = false

        val currentDataSet = (this.application as MyApplication).selectedTable
        title = currentDataSet
        val mDatabase = DatabaseHandler(this,currentDataSet)

        mDataArray.clear()
        mDataArray.addAll(mDatabase.allDataValues)
        mDataArray.sortByDescending { it.date }

        // RecyclerView
        // we are creating the LinearLayoutManager here

        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(mDataArray,this)

        recyclerView = findViewById<RecyclerView>(R.id.rVDatabase).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

       btnExport.setOnClickListener{

           // Here, thisActivity is the current activity
           if (ContextCompat.checkSelfPermission(this,
                   android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
               // Permission is not granted
               // Should we show an explanation?
               Toast.makeText(this, "Data export requires write permission", Toast.LENGTH_SHORT).show()
               ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                   MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
               Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
           }

           if (ContextCompat.checkSelfPermission(this,
                   android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
               // Permission is not granted
               // Should we show an explanation?
               Toast.makeText(this, "Data not exported. Write to external storage permission required.", Toast.LENGTH_SHORT).show()
           } else {
               // use https://www.youtube.com/watch?v=VDAwbgHoYEA
               // generate string containing export data
               val exportData = StringBuilder()
               exportData.append("Date, Value")
               for (datapoint in mDataArray) {
                   exportData.append("\n${datapoint.date.toString()},${datapoint.value.toString()}")
               }

               try {
                   //saving the file into device
                   val out: FileOutputStream = openFileOutput("${currentDataSet}_data.csv", Context.MODE_PRIVATE)
                   out.write(exportData.toString().toByteArray())
                   out.close()

                   //saving the file into device
                   val dir = File(this.getExternalFilesDir(null), "my_data_tracker_data")
                   if (!dir.exists()){
                       dir.mkdirs()
                   }
                   val file = File(dir, "${currentDataSet}_data.csv")
                   if (file.exists()) {
                       file.delete()
                       file.createNewFile()
                   } else {
                       file.createNewFile()
                   }
                   val out2: FileOutputStream = FileOutputStream(file, true)
                   out2.write(exportData.toString().toByteArray())
                   out2.close()
                   Toast.makeText(this, "Local file saved to ${file.path}", Toast.LENGTH_SHORT).show()

                   //exporting
                   val context: Context = applicationContext
                   val filelocation = File(filesDir, "${currentDataSet}_data.csv")
                   val path: Uri = FileProvider.getUriForFile(
                       context,
                       "com.szetoo.logger.fileprovider",
                       filelocation
                   )

                   val fileIntent = Intent(Intent.ACTION_SEND)
                   fileIntent.type = "text/csv"
                   fileIntent.putExtra(Intent.EXTRA_SUBJECT, "My Data Tracker export")
                   fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                   fileIntent.putExtra(Intent.EXTRA_STREAM, path)
                   startActivity(Intent.createChooser(fileIntent, "Send mail"))


               } catch (e: Exception) {
                   e.printStackTrace()
                   Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
               }


           }

       }

    }

    override fun onResume() {
        super.onResume()
        // Refresh values array when resuming
        if (NavigateAwayFlag) {
            recreate()
        }
    }

    override fun onEntryClick(position: Int) {
        var selectedID = mDataArray[position].id
        val intentViewEntry = Intent(this, EditEntryActivity::class.java).apply { putExtra(EXTRA_MESSAGE_ID, selectedID.toString())
        }
        NavigateAwayFlag = true
        startActivity(intentViewEntry)
      }

}
