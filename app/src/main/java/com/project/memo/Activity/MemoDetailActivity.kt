package com.project.memo.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.project.memo.Adapter.*
import com.project.memo.R
import com.project.memo.VO.MemoImageVO
import com.project.memo.VO.MemoVO
import kotlinx.android.synthetic.main.activity_add_memo.*
import kotlinx.android.synthetic.main.activity_memo_detail.*
import java.nio.file.Files.delete

class MemoDetailActivity : AppCompatActivity() {

    val TAG:String = "MemoDetailActivity"
    var imageList : MutableList<String> = mutableListOf()          // viewPagerAdapter에 보낼 imageList
    var dbImageList : MutableList<MemoImageVO> = mutableListOf()   // DB에서 select해온 image list
    var list:MutableList<MemoVO> = mutableListOf()                  // 메모 (제목, 내용) list
    var id:Int? = null
    var title =""
    var content =""
    var image =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_detail)

        // 내용 textview 스크롤
        detail_content.setMovementMethod(ScrollingMovementMethod());

        // 메모 삭제버튼
        detail_delete.setOnClickListener {
            deleteMemo() // 삭제 확인/취소 다이얼로그
        }

        // 메모 수정하기 버튼
        detail_update.setOnClickListener {
            val updateIntent = Intent(this, UpdateMemoActivity::class.java)
            updateIntent.putExtra("id", id!!)
            updateIntent.putExtra("title", title)
            updateIntent.putExtra("content", content)
            updateIntent.putExtra("image",image)
            startActivity(updateIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        // DB에서 메모 데이터 ( 제목, 내용, 이미지) 가져오기
        detailSelect()
    }

    fun detailSelect(){
        imageList.clear()
        id = intent.extras.getInt("id")
        dbImageList = selectMemoImageList(id!!,this) // 클릭한 메모 image list 가져오기

        list = selectMemo(id!!,this) // 클릭한 제목, 내용 가져오기

        title = list.get(0).title
        content = list.get(0).content

        // 가져온 리스트를 경로만 빼서 List에 넣기
        for(img in dbImageList){
            imageList.add(img.images)
        }

        // 이미지가 0개 viewPager GONE
        // 이미지 1개 이상 viewPager VISIBLE
        if (imageList.size != 0){
            detail_viewPager.setVisibility(View.VISIBLE)
            detail_background_view.setVisibility(View.VISIBLE)
        }else{
            detail_viewPager.setVisibility(View.GONE)
            detail_background_view.setVisibility(View.GONE)
        }

        detail_title.setText(title)
        detail_content.setText(content)

        // adapter에 list 보내서 출력
        detail_viewPager.adapter = ViewPagerDetailAdapter(this, imageList)

    }

    // 삭제 확인/취소 다이얼로그
    fun deleteMemo(){
        var dialog = AlertDialog.Builder(this)
        dialog.setTitle("메모를 삭제하시겠습니까?")

        fun memoDelete(){
            deleteMemo(id!!,this)       // 메모 테이블에서 메모삭제
            deleteImageAll(id!!,this)   // 메모이미지 테이블에 이미지 전부 삭제
            finish()
        }
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> memoDelete()
                }
            }
        }
        dialog.setPositiveButton("확인",dialog_listener)
        dialog.setNegativeButton("취소",dialog_listener)
        dialog.show()

    }



}
