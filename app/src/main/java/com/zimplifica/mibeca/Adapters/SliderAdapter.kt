package com.zimplifica.mibeca.Adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.zimplifica.mibeca.R

class SliderAdapter(val context: Context) : PagerAdapter() {

    val array = arrayListOf("Bienvenido a IMFOBN", "Se el primero en enterarte si ya depositaron","Agrega beneficiarios")
    val arrayimgs = arrayListOf(R.drawable.logo_base, R.drawable.school_bell, R.drawable.family)
    val arraydescriptions = arrayListOf(R.string.WelcomeDescription, R.string.DepositDescription, R.string.CitizenDescription)

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1 as RelativeLayout
    }

    override fun getCount(): Int {
        return array.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(R.layout.slide_layout, container, false)
        val header : TextView = view.findViewById(R.id.header_slide)
        val description : TextView = view.findViewById(R.id.description_slide)
        val img : ImageView = view.findViewById(R.id.slide_image)
        header.text = array[position]
        description.setText(arraydescriptions[position])
        img.setImageResource(arrayimgs[position])
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }
}