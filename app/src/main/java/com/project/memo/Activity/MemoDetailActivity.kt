package com.project.memo.Activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import com.project.memo.Adapter.*
import com.project.memo.R
import com.project.memo.VO.MemoImageVO
import com.project.memo.VO.MemoVO
import kotlinx.android.synthetic.main.activity_memo_detail.*
import java.io.File

class MemoDetailActivity : AppCompatActivity() {

    private val TAG:String = "MemoDetailActivity"
    private var imageList : MutableList<String> = mutableListOf()          // viewPagerAdapter에 보낼 imageList
    private var dbImageList : MutableList<MemoImageVO> = mutableListOf()   // DB에서 select해온 image list
    private var list:MutableList<MemoVO> = mutableListOf()                  // 메모 (제목, 내용) list
    private var id:Int? = null
    private var title =""
    private var content =""
    private var image =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo_detail)

        // 내용 textview 스크롤
        detail_content.movementMethod = ScrollingMovementMethod()

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

    private fun detailSelect(){
        imageList.clear()
        id = intent.extras.getInt("id")
        dbImageList = selectMemoImageList(id!!,this) // 클릭한 메모 image list 가져오기

        list = selectMemo(id!!,this) // 클릭한 제목, 내용 가져오기

        title = list[0].title
        content = list[0].content

        // 가져온 리스트를 경로만 빼서 List에 넣기
        for(img in dbImageList){
            imageList.add(img.images)
        }

        // 이미지가 0개 viewPager GONE
        // 이미지 1개 이상 viewPager VISIBLE
        if (imageList.size != 0){
            detail_viewPager.visibility = View.VISIBLE
            detail_background_view.visibility = View.VISIBLE
        }else{
            detail_viewPager.visibility = View.GONE
            detail_background_view.visibility = View.GONE
        }

        detail_title.text = title
        detail_content.text = content

        // adapter에 list 보내서 출력
        detail_viewPager.adapter = ViewPagerDetailAdapter(this, imageList)

    }

    // 삭제 확인/취소 다이얼로그
    private fun deleteMemo(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("메모를 삭제하시겠습니까?")

        fun memoDelete(){
            deleteMemo(id!!,this)       // 메모 테이블에서 메모삭제
            deleteImageAll(id!!,this)   // 메모이미지 테이블에 이미지 전부 삭제
            finish()
        }
        for(i in 0 until imageList.size){
            Log.d("@@@@ ","  $i")
            imageFileDelete(i)
        }
        val dialogListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> memoDelete()
                }
            }
        }
        dialog.setPositiveButton("확인",dialogListener)
        dialog.setNegativeButton("취소",dialogListener)
        dialog.show()

    }
    // 이미지 파일 삭제
    private fun imageFileDelete(position:Int){
        val file = File(imageList[position])
        if(file.exists()){
            file.delete()
        }else{
            Log.d(TAG, " 삭제 실패 ")
        }
    }


}
