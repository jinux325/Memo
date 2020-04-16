package com.project.memo.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.project.memo.R
import kotlinx.android.synthetic.main.activity_memo_detail_viewpager.view.*


class ViewPagerDetailAdapter(private val mContext : Context, private val imageList : MutableList<String> ) : PagerAdapter() {

    val TAG : String = "ViewPagerDetailAdapter"
    @SuppressLint("InflateParams", "SetTextI18n")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.activity_memo_detail_viewpager, null)

        Log.d(TAG, " "+imageList[position])

        // 이미지 현재 이미지 / 총 이미지 수
        view.detail_imagesCount.text = (position+1).toString()+"/"+imageList.size.toString()

        // 이미지 출력
        Glide.with(mContext).load(imageList[position]).into(view.detail_viewPager_imageView)
        container.addView(view)

        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}