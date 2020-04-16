package com.project.memo.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.project.memo.VO.MemoMainVO
import com.project.memo.VO.MemoImageVO
import com.project.memo.VO.MemoVO

// 디비 버전
private const val DATABASE_VERSION = 1

// 데이터베이스 이름
private const val DATABASE_NAME = "memodb10"

// 테이블 이름
private const val DB_TABLE = "tb_memo"
private const val DB_IMAGE = "tb_image"

// 컬럼 이름
private const val TITLE = "title"
private const val CONTENT = "content"
private const val IMAGE = "image"
private const val MEMOID = "memoId"
private const val TAG : String ="DBHelper"


class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null,DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        /* create
        *  tb_memo : table이름
        *  _id     : id (PK)
        *  title   : 제목
        *  content : 내용
        * */
        val memo = "create table $DB_TABLE"+
                "(_id integer primary key autoincrement,"+
                "$TITLE TEXT,"+
                "$CONTENT TEXT)"
        /* create
        *  tb_image : table이름
        *  _id      : id (PK)
        *  memoId   : tb_memo id
        *  image    : 이미지
        * */
        val memoImage = "create table $DB_IMAGE"+
                "(_id integer primary key autoincrement,"+
                "$MEMOID integer,"+
                "$IMAGE TEXT)"
        db.execSQL(memoImage)
        db.execSQL(memo)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e(TAG," UPDATE ")
        db.execSQL("drop table $DB_TABLE")
        onCreate(db)
    }

}

/* insert
*  메모 제목, 내용 입력
*  title   : 제목
*  content : 내용
*  context : context
* */
@SuppressLint("Recycle")
fun memoInsert(title:String, content:String, context:Context){
    val db = DBHelper(context).writableDatabase
    db.execSQL("insert into $DB_TABLE ($TITLE, $CONTENT) values (?,?)", arrayOf<Any?>(title,content))
    db.close()
}

/* insert
*  메모 이미지 입력
*  id      : tb_memo id
*  images  : 이미지 경로
*  context : context
* */
@SuppressLint("Recycle")
fun imageInsert(id:Int,images:String, context:Context){
    val db = DBHelper(context).writableDatabase
    db.execSQL("insert into $DB_IMAGE ($MEMOID, $IMAGE) values (?,?)", arrayOf<Any?>(id, images))
    db.close()
}

/* delete
*  메모 삭제
*  id      : tb_memo id
*  context : context
* */
@SuppressLint("Recycle")
fun deleteMemo(id:Int, context:Context){
    val db = DBHelper(context).writableDatabase
    db.execSQL("delete from $DB_TABLE where _id=?", arrayOf<Any?>(id))
    db.close()
}
/* delete
*  메모 이미지 전체삭제
*  id      : tb_memo id
*  context : context
* */
@SuppressLint("Recycle")
fun deleteImageAll(id:Int, context:Context){
    val db = DBHelper(context).writableDatabase
    db.execSQL("delete from $DB_IMAGE where $MEMOID=?", arrayOf<Any?>(id))
    db.close()
}

/* delete
*  메모 이미지 삭제
*  수정할 id   : tb_image id
*  context     : context
* */
@SuppressLint("Recycle")
fun deleteImage(id:Int, context:Context){
    val db = DBHelper(context).writableDatabase
    db.execSQL("delete from $DB_IMAGE where _id=?", arrayOf<Any?>(id))
    db.close()
}

/* update
*  메모 수정
*  tb_memo id  : 수정할 id
*  title       : 수정 제목
*  content     : 수정 내용
*  context     : context
* */
@SuppressLint("Recycle")
fun updateMemo(id:Int, title:String, content:String, context:Context){
    val db = DBHelper(context).writableDatabase
    db.execSQL("update $DB_TABLE set $TITLE=?, $CONTENT=? where _id=?", arrayOf<Any?>(title,content,id))
    db.close()
}

/* select
*  tb_memo 모든 데이터 가져오기
*  return 값 : MemoVO
*/
/*@SuppressLint("Recycle")
fun selectMemo(context:Context):MutableList<MemoVO>{
    val list:MutableList<MemoVO> = mutableListOf()
    val helper = DBHelper(context)
    val db = helper.readableDatabase
    val cursor = db.rawQuery("select * from $DB_TABLE order by _id desc",null)
    while (cursor.moveToNext()){
        Log.d(TAG,cursor.getInt(0).toString())
        list.add(MemoVO(cursor.getInt(0),cursor.getString(1),cursor.getString(2)))
    }
    return list
}*/

/* select
*  tb_memo 모든 데이터 가져오기
*  return 값 : MemoImageVO
*/
/*@SuppressLint("Recycle")
fun selectImage(context:Context):MutableList<MemoImageVO>{
    val list:MutableList<MemoImageVO> = mutableListOf()
    val helper = DBHelper(context)
    val db = helper.readableDatabase
    val cursor = db.rawQuery("select * from $DB_IMAGE order by _id desc",null)
    while (cursor.moveToNext()){
        Log.d(TAG,cursor.getInt(0).toString())
        list.add(MemoImageVO(cursor.getInt(0),cursor.getInt(1),cursor.getString(2)))
    }
    return list
}*/

/* select
*  가장 최근에 들어간 메모 가져오기
*  return 값 : MemoVO
*/
@SuppressLint("Recycle")
fun selectMemoId(context:Context):MutableList<MemoVO>{
    val list:MutableList<MemoVO> = mutableListOf()
    val helper = DBHelper(context)
    val db = helper.readableDatabase
    val cursor = db.rawQuery("select * from $DB_TABLE order by _id desc limit 1",null)
    while (cursor.moveToNext()){
        Log.d(TAG,cursor.getInt(0).toString())
            list.add(MemoVO(cursor.getInt(0),cursor.getString(1),cursor.getString(2)))
    }
    return list
}

/* select
*  특정 메모의 이미지리스트 가져오기
*  id        : memoVO id
*  return 값 : MemoImageVO
*/
@SuppressLint("Recycle")
fun selectMemoImageList(id:Int, context:Context):MutableList<MemoImageVO>{
    val list:MutableList<MemoImageVO> = mutableListOf()
    val helper = DBHelper(context)
    val db = helper.readableDatabase
    val cursor = db.rawQuery("select * from $DB_IMAGE where $MEMOID = ? order by _id asc ", arrayOf(id.toString()))
    while (cursor.moveToNext()){
        list.add(MemoImageVO(cursor.getInt(0),cursor.getInt(1),cursor.getString(2)))
    }
    return list
}

/* select
*  특정 메모 가져오기
*  id        : memoVO id
*  return 값 : MemoVO
*/
@SuppressLint("Recycle")
fun selectMemo(id:Int, context:Context):MutableList<MemoVO>{
    val list:MutableList<MemoVO> = mutableListOf()
    val helper = DBHelper(context)
    val db = helper.readableDatabase
    val cursor = db.rawQuery("select * from $DB_TABLE where _id = ? order by _id asc ", arrayOf(id.toString()))
    while (cursor.moveToNext()){
        list.add(MemoVO(cursor.getInt(0),cursor.getString(1),cursor.getString(2)))
    }
    return list
}

/* select
*  모든 메모의 정보와 메모에 첨부된 첫번째 이미지 가져오기
*  return 값 : MemoMainVO
*/
@SuppressLint("Recycle")
fun selectALL(context:Context):MutableList<MemoMainVO>{
    val list:MutableList<MemoMainVO> = mutableListOf()
    val helper = DBHelper(context)
    val db = helper.readableDatabase
    val cursor = db.rawQuery("select * from $DB_TABLE as a " +
                                "left join " +
                                "(select min(_id), $MEMOID, $IMAGE from $DB_IMAGE group by $MEMOID) as b " +
                                "on a._id = b.$MEMOID "+
                                "order by a._id desc", null)
    while (cursor.moveToNext()){
        list.add(MemoMainVO(cursor.getInt(0),cursor.getString(1),cursor.getString(2),cursor.getString(5)))
    }
    return list
}

