package com.project.memo.VO

/* 메모장 이미지
* id     : 메모장 이미지 ID (PK)
* memoId : 메모장 tb_Memo id
* images : 이미지 경로
* */
data class MemoImageVO (
    val id:Int,
    val memoId:Int,
    val images:String
)
