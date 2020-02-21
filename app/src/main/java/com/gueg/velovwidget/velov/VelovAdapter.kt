package com.gueg.velovwidget.velov

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gueg.velovwidget.R

class VelovAdapter(private val velovs: List<Velov>) : RecyclerView.Adapter<VelovAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var rating: TextView = v.findViewById(R.id.velov_rating)
        var standNb: TextView = v.findViewById(R.id.velov_stand_nb)
        var pic: ImageView = v.findViewById(R.id.velov_bikes_pic)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val velov = velovs[pos]

        if(velov.ratingNone)
            holder.rating.text = "--"
        else
            holder.rating.text = velov.rating.toString()

        if(velov.type == "ELECTRICAL")
            holder.pic.setImageResource(R.drawable.ic_velo_elec)
        else // type == "MECHANICAL"
            holder.pic.setImageResource(R.drawable.ic_velo)

        if(velov.status != "AVAILABLE")
            holder.rating.setTextColor(Color.RED)
        else
            holder.rating.setTextColor(Color.BLACK)

        holder.standNb.text = velov.standNumber.toString()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) =ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.row_velov, p0, false))

    override fun getItemCount(): Int = velovs.size

}