package com.project.memo.VO

/* 메모장 제목, 내용
* id      : 메모장 ID (PK)
* title   : 메모장 제목
* content : 메모장 내용
* */
data class MemoVO (
    val id:Int,
    val title:String,
    val content:String
)