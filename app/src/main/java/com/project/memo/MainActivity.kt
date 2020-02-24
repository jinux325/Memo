package com.project.memo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.project.memo.Activity.AddMemoActivity
import com.project.memo.Activity.UpdateMemoActivity
import com.project.memo.Adapter.Adapter
import com.project.memo.Adapter.selectALL
import com.project.memo.Adapter.selectImage
import com.project.memo.Adapter.selectMemo
import com.project.memo.VO.MemoImageVO
import com.project.memo.VO.MemoVO

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // 메모추가 버튼 ( addMemoActivity 이동 )
        add_memo.setOnClickListener { view ->
            val addMemoIntent = Intent(this, AddMemoActivity::class.java)
            startActivity(addMemoIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // actionbar >> toolbar
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onResume() {
        super.onResume()

        var mainList = selectALL(this)  // 모든 메모의 정보와 메모에 첨부된 첫번째 이미지 가져오기

        val sgManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = sgManager
        recyclerView.adapter = Adapter(this, mainList )    // adapter에 리스트보내고 출력하기

    }
}
