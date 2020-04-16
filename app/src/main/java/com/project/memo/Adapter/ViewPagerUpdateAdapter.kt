package com.project.memo.Adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.project.memo.Activity.UpdateMemoActivity
import com.project.memo.R.layout.activity_add_memo_viewpager
import kotlinx.android.synthetic.main.activity_add_memo_viewpager.view.*


class ViewPagerUpdateAdapter(private val mContext : Context, private val imageList : MutableList<String> ) : PagerAdapter() {

    private val TAG : String = "ViewPagerAdapter"
    @SuppressLint("SetTextI18n", "InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(activity_add_memo_viewpager, null)

        Log.d(TAG, " "+ imageList[position])

        // 수정 페이지 viewPager 클릭 이벤트
        view.viewPager_imageView.setOnClickListener {
            imageDeleteDialog(position) // 클릭시 삭제 이벤트
        }

        // 이미지 현재 이미지 / 총 이미지 수
        view.imagesCount.text = " ${(position+1)}/${imageList.size}"

        // 수정 이미지 출력
        Glide.with(mContext).load(imageList[position]).into(view.viewPager_imageView)
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


    // 클릭시 삭제 이벤트
    private fun imageDeleteDialog( position: Int){
        val dialog = AlertDialog.Builder(mContext)
        dialog.setTitle("이미지를 삭제하시겠습니까?")

        fun imageDelete(){
            imageList.removeAt(position)                              // 제거된 index를 리스트에서 제거
            (mContext as UpdateMemoActivity).imageDelete(position)    // 제거된 이미지 index를 보냄( 삭제처리를 위해 )
            notifyDataSetChanged()                                      // 제거되어 바뀐 리스트 다시 셋팅

            /*  imageList : 제거된 이미지 리스트
             *  imageList 의 이미지가 제거되어 크기가 0일때(= 이미지 없음)
             */
            if(imageList.size == 0){
                mContext.imageViewPager()
            }
        }
        val dialogListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> imageDelete()
                }
            }
        }
        dialog.setPositiveButton("확인",dialogListener)
        dialog.setNegativeButton("취소",dialogListener)
        dialog.show()

    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}