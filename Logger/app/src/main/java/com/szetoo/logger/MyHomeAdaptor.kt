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
class MyHomeAdapter(private val myDataset: ArrayList<String>, mEntryListener: OnHomeEntryListener) :
    RecyclerView.Adapter<MyHomeAdapter.MyViewHolder>() {

    var selected_position = 0 // You have to set this globally in the Adapter class
    private val dataValues: ArrayList<LoggingData> = ArrayList()
    val HomeEntryListener = mEntryListener
    /**
     * We are creating our view holder on this function
     * @return
     * MyViewHolder object
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyHomeAdapter.MyViewHolder {
        /**
         * Lets firts create a layout for a  single item
         */
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_card_layout, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(linearLayout, HomeEntryListener)  // return the view holder
    }


    /**
     *This function is invoked by layout manager when an item is visible or binded
     * Use this function to make changes to the item specific views
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.homeCardTV.text = myDataset[position]
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
    class MyViewHolder(val view: View, entryListener: OnHomeEntryListener) : RecyclerView.ViewHolder(view){
        lateinit var homeCardTV: TextView

        init {
            val thisEntryListener = entryListener
            homeCardTV = view.findViewById(R.id.homeCardTV) as TextView
            view.setOnClickListener{
                thisEntryListener.onHomeEntryClick(adapterPosition)
            }
        }

    }

    public interface OnHomeEntryListener{
        fun onHomeEntryClick(position: Int)
    }


}
