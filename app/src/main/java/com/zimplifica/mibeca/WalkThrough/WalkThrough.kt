package com.zimplifica.mibeca.WalkThrough

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.zimplifica.mibeca.Adapters.SliderAdapter
import com.zimplifica.mibeca.R
import com.zimplifica.mibeca.SignScreen

class WalkThrough : AppCompatActivity() {

    lateinit var mSlideViewPager: ViewPager
    lateinit var mDotLayout: LinearLayout
    lateinit var mDots: Array<TextView>
    var mcurrentPage: Int = 0
    lateinit var mNextBtn : Button
    lateinit var mBackBtn : Button
    lateinit var mFinishBtn: Button
    lateinit var closeBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk_through)
        mNextBtn = findViewById(R.id.nextBtn)
        mBackBtn = findViewById(R.id.backBtb)
        mFinishBtn = findViewById(R.id.finishBtn)
        closeBtn = findViewById(R.id.CloseBtnWalktr)
        mSlideViewPager = findViewById(R.id.slideViewPager)
        mDotLayout = findViewById(R.id.dotsLayout)
        addDotsIndicator(0)
        mSlideViewPager.addOnPageChangeListener(viewListener)
        mBackBtn.setOnClickListener{
            mSlideViewPager.currentItem = mcurrentPage -1
        }
        mNextBtn.setOnClickListener {
            mSlideViewPager.currentItem = mcurrentPage +1
        }
        mSlideViewPager.adapter = SliderAdapter(this)
        closeBtn.setOnClickListener {
            val intent = Intent(this,SignScreen::class.java)
            startActivity(intent)
            finish()
        }
        mFinishBtn.setOnClickListener {
            val intent = Intent(this,SignScreen::class.java)
            startActivity(intent)
            finish()
        }





    }

    fun addDotsIndicator(position: Int){
        mDots = Array<TextView>(3){ TextView(this) }
        mDotLayout.removeAllViews()
        var i = 0
        while (i<mDots.size){
            mDots[i].text = Html.fromHtml("&#8226;")
            mDots[i].textSize = 35F
            mDots[i].setTextColor(resources.getColor(R.color.custom_gray))
            mDotLayout.addView(mDots[i])
            i++
        }
        if (mDots.size>0){
            mDots[position].setTextColor(resources.getColor(R.color.colorAccent))
        }
    }

    private val viewListener = object : ViewPager.OnPageChangeListener{
        override fun onPageScrollStateChanged(p0: Int) {

        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

        }

        override fun onPageSelected(p0: Int) {
            addDotsIndicator(p0)
            mcurrentPage = p0
            when(p0){
                0 -> {
                    mNextBtn.isEnabled = true
                    mBackBtn.isEnabled = false
                    mFinishBtn.isEnabled = false
                    mBackBtn.visibility = View.INVISIBLE
                    mFinishBtn.visibility = View.INVISIBLE
                    mNextBtn.visibility =  View.VISIBLE
                }
                1 -> {
                    mBackBtn.isEnabled = true
                    mBackBtn.visibility = View.VISIBLE
                    mNextBtn.isEnabled = true
                    mNextBtn.visibility =  View.VISIBLE
                    mFinishBtn.visibility = View.INVISIBLE
                    mFinishBtn.isEnabled = false

                }
                else -> {
                    mBackBtn.isEnabled = true
                    mBackBtn.visibility = View.VISIBLE
                    mNextBtn.isEnabled = false
                    mNextBtn.visibility =  View.INVISIBLE
                    mFinishBtn.isEnabled = true
                    mFinishBtn.visibility = View.VISIBLE
                }
            }
        }

    }
}
