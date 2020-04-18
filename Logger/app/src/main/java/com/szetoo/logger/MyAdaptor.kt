package com.szetoo.logger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Original code created by abhijet on 4/30/19 and adapted by David
 */
class MyAdapter(private val myDataset: ArrayList<LoggingData>, mEntryListener: OnEntryListener) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    var selected_position = 0 // You have to set this globally in the Adapter class
    private val dataValues: ArrayList<LoggingData> = ArrayList()
    val entryListener = mEntryListener
    /**
     * We are creating our view holder on this function
     * @return
     * MyViewHolder object
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        /**
         * Lets firts create a layout for a  single item
         */
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layout, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(linearLayout, entryListener)  // return the view holder
    }


    /**
     *This function is invoked by layout manager when an item is visible or binded
     * Use this function to make changes to the item specific views
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.itemTextDate.text = ReformatDate(myDataset[position].date)
        holder.itemTextTime.text = ReformatTime(myDataset[position].date)
        holder.itemTextValue.text = myDataset[position].value.toString()
        holder.itemTextId.text = myDataset[position].id.toString()
    }

    /**
     * This function returns the size of the list holding by our adapter
     * It is very important that we return the actual size of the list here
     * IF zero is returned by this function no item will be displayed in the recyclerview
     */
    override fun getItemCount() = myDataset.size


    /**
     * MyViewHolder
     * ------------
     * MyViewHolder is our view holder
     * Every Layout of each item will be defined here
     *
     */
    class MyViewHolder(val view: View, entryListener: OnEntryListener) : RecyclerView.ViewHolder(view){
        lateinit var itemTextDate: TextView
        lateinit var itemTextTime: TextView
        lateinit var itemTextValue: TextView
        lateinit var itemTextId: TextView

        init {
            val thisEntryListener = entryListener
            itemTextDate = view.findViewById(R.id.card_text_date) as TextView
            itemTextTime = view.findViewById(R.id.card_text_time) as TextView
            itemTextValue = view.findViewById(R.id.card_text_datavalue) as TextView
            itemTextId = view.findViewById(R.id.card_text_id) as TextView
            view.setOnClickListener{
                thisEntryListener.onEntryClick(adapterPosition)
            }
        }

    }

    public interface OnEntryListener{
        fun onEntryClick(position: Int)
    }


}

public fun ReformatTime(strTime:String):String{
    val hour = strTime.subSequence(9,11).toString()
    val mins = strTime.subSequence(12,14).toString()
    val secs = strTime.subSequence(15,17).toString()
    return "$hour:$mins"
}

public fun ReformatDate(strTime:String):String{
    val year = strTime.subSequence(0,4).toString()
    val month = strTime.subSequence(4,6).toString()
    val day = strTime.subSequence(6,8).toString()
    return "$day-$month-$year"
}