package com.ShayaanNofil.i210450

import Mentors
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class homerecycle_adapter(private val items: MutableList<Mentors>): RecyclerView.Adapter<homerecycle_adapter.ViewHolder>() {

    private lateinit var onClickListener: homerecycle_adapter.OnClickListener

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val mentorimg: ImageView = itemView.findViewById(R.id.mentor_img)
        val mentorname: TextView = itemView.findViewById(R.id.mentor_name)
        val mentorrate: TextView = itemView.findViewById(R.id.mentor_rate)
        val mentorjob: TextView = itemView.findViewById(R.id.mentor_job)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var onClickListener: OnClickListener? = null

        val view = LayoutInflater.from(parent.context).inflate(R.layout.homepage_mentor_recycle, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mentor = items[position]

        holder.mentorname.text = mentor.name
        holder.mentorrate.text = mentor.rate.toString()
        holder.mentorjob.text = mentor.job

        var rqoptions: RequestOptions = RequestOptions()
        rqoptions= rqoptions.transform(CenterCrop(), RoundedCorners(40))
        Glide.with(holder.itemView)
            .load(mentor.profilepic)
            .apply(rqoptions)
            .into(holder.mentorimg)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(position, mentor)
        }
    }

    // A function to bind the onclickListener.
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    // onClickListener Interface
    interface OnClickListener {
        fun onClick(position: Int, model: Mentors)
    }

}