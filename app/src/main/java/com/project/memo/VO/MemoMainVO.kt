package com.project.memo.VO

/* 메모장 Main List
* id      : 메모장 tb_memo ID
* title   : 메모장 제목
* content : 메모장 내용
* image   : 메모장 이미지
* */
data class MemoMainVO (
    val id:Int,
    val title:String,
    val content:String,
    val image:String?
)