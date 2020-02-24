package com.project.memo.Activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.internal.ContextUtils.getActivity
import com.project.memo.Adapter.*
import com.project.memo.R
import com.project.memo.VO.MemoImageVO
import com.project.memo.VO.MemoVO
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_update_memo.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.*

class UpdateMemoActivity : AppCompatActivity() {

    val TAG : String ="UpdateMemoActivity"
    val imageList : MutableList<String> = mutableListOf()
    val updateImageList : MutableList<String> = mutableListOf()
    var dbImageList : MutableList<MemoImageVO> = mutableListOf()
    var list:MutableList<MemoVO> = mutableListOf()
    var id:Int? = null
    var title =""
    var content =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_memo)
        imageList.clear()

        id = intent.extras.getInt("id") // 메모장(tb_memo) id 값

        list = selectMemo(id!!,this) // id 값에 맞는 제목, 내용 가져오기

        title = list.get(0).title
        content = list.get(0).content

        update_title.setText(title)     // 제목 editText에 보내기
        update_content.setText(content) // 내용 editText에 보내기

        viewPagerRefresh()  // 이미지 가져와서 viewPager에 출력

        // 이미지 추가 버튼
        update_imageBtn.setOnClickListener {
            permission()    // 이미지 추가를 위한 권한설정
        }

        // 수정 버튼
        updateBtn.setOnClickListener {
            memoUpdate()    // 수정한 내용 DB에 저장
        }

    }

    // 퍼미션 권한 설정
    fun permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("TAG", "권한 설정 완료");
                addDialog()
            } else {
                Log.d("TAG", "권한 설정 요청");
                ActivityCompat.requestPermissions( this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1 );
            }
        }
    }
    // 이미지 추가 다이얼로그
    fun addDialog(){
        var dialog = AlertDialog.Builder(this)
        dialog.setTitle("이미지 추가")

        fun cameraDiolog(){ addDialogURL() } // URL 이미지 추가
        fun photoDialog(){
            // 갤러리 이동 ( tedImagePicker 라이브러리 이용 )
            TedImagePicker.with(this).startMultiImage { uriList -> getImageList(uriList) }
        }
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> cameraDiolog()
                    DialogInterface.BUTTON_NEGATIVE -> photoDialog()
                }
            }
        }

        dialog.setPositiveButton("URL 이미지",dialog_listener)
        dialog.setNegativeButton("갤러리/카메라",dialog_listener)
        dialog.setNeutralButton("취소",dialog_listener)
        dialog.show()

    }

    // image : tedImagePicker에서 선택한 이미지 리스트
    // 선택한 이미지 DB에 추가
    fun getImageList(image: List<Uri>){
        updateImageList.clear()
        update_viewPager.clipToPadding = false // 패딩공간 제거
        for(Uri in image){
            if(Uri!=null) {
               /* var bit = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri) // URI를 비트맵으로 변환*/
                var degree = getExifOrientation(Uri.toString().substring(7,Uri.toString().length))                    // 바뀐 회전각 구하기
                var bitRotate = imgRotate(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri), degree)  // 바뀐 각만큼 다시 바꾸기

                lateinit var downImage:String
                if(degree == 0){
                    downImage = saveBitmaptoJpeg(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri),"/Pictures/MemoApp")  // 이미지 다운로드
                }else{
                    downImage = saveBitmaptoJpeg(bitRotate,"/Pictures/MemoApp")      // 이미지 다운로드
                }
                imageList.add(downImage)  // 추가한 이미지를 viewPager imageList에 추가
                updateImageList.add(downImage) // 추가된 이미지 리스트 ( DB 넣기 위한 List )

            }
        }

        // DB에 추가된 이미지 넣기
        for(updateImage in updateImageList){

            /*  id          : memo pk id값 ( tb_memo id(pk) )
             *  updateImage : 추가할 이미지
             */
            imageInsert(id!!,updateImage, this)
        }
        viewPagerRefresh()  // 수정된 이미지리스트로 viewpager수정
        update_viewPager.setCurrentItem(imageList.size-1)  // viewPager 위치 끝으로
    }

    /* 이미지 다운로드
         * return : 다운로드한 파일 경로 String
         */
    fun saveBitmaptoJpeg(bitmap: Bitmap, folder:String):String{
        var ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath() // 외부 스토리지 최상위 주소
        val imageFileName = "MEMO_" + UUID.randomUUID().toString()  // 'MEMO_' + UUID로 안겹치는 이름을 만든다

        var foler_path = "/"+folder+"/"             // 이미지 경로를 /Pictures/MemoApp 로 설정
        var file_name = imageFileName+".jpg"        // 앞에서 정한 파일이름에 파일형식을 붙여준다
        var string_path = ex_storage+foler_path      // 최상위 주소와 폴더경로를 합쳐서 주소를 만든다.

        var file_path : File
        try{
            file_path = File(string_path);  // 폴더 생성
            if(!file_path.isDirectory()){   // 생성하려는 이름의 폴더가 없으면 생성한다
                file_path.mkdirs();
            }
            var out = FileOutputStream(string_path+file_name)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)   // 폴더에 50% 압축 이미지 저장
            out.close();

        }catch(exception: FileNotFoundException){
            Log.e("FileNotFoundException", exception.message);
        }catch(exception: IOException){
            Log.e("IOException", exception.message);
        }
        return string_path+file_name
    }

    /* imageList 의 이미지가 제거되어 크기가 0일때(= 이미지 없음)
     *  viewPager를 안보이게 Visible을 GONE으로 설정
     */
    fun imageViewPager() {
        Log.d(TAG," resume "+imageList.size)
        if(imageList.size == 0){
            update_viewPager.setVisibility(View.GONE)
            update_background_view.setVisibility(View.GONE)
        }else{
            update_viewPager.setVisibility(View.VISIBLE)
            update_background_view.setVisibility(View.VISIBLE)
        }
    }

    /*  position : ViewPagerUpdateAdapter에서 클릭된 이미지 position
        position으로 삭제된 이미지의 객체정보를 가져와서 DB에서 삭제한다
     */
    fun imageDelete(position:Int){
        // dbImageList : image id (pk)
        // tb_image 테이블에서 이미지 제거
        deleteImage(dbImageList.get(position).id,this)
        imageFileDelete(position) // 이미지 파일 삭제
        viewPagerRefresh() // viewPager 새로고침
        if( position == dbImageList.size){
            update_viewPager.setCurrentItem(position-1)  // viewPager 위치를 삭제 position 바로 앞으로
        }else {
            update_viewPager.setCurrentItem(position)  // viewPager 위치를 삭제 position으로
        }

    }

    // 뒤로가기시 수정하기
    override fun onBackPressed() {
        super.onBackPressed()
        memoUpdate() // 수정한 데이터 업데이트
        Toast.makeText(this, " 메모가 자동수정되었습니다.",Toast.LENGTH_SHORT).show()
    }

    // 수정된 데이터 업데이트
    fun memoUpdate(){
        val title = update_title.getText().toString()
        val content = update_content.getText().toString()
        id = intent.extras.getInt("id")

        /*  id      : 메모장 id
         *  title   : 메모장 제목
         *  content : 메모장 내용
         *  수정된 정보 DB에 저장
         */
        updateMemo(id!!,title, content, this)

        finish()
    }


    // URL 이미지 입력 다이얼로그
    fun addDialogURL(){
        var dialog = AlertDialog.Builder(this)
        dialog.setTitle("이미지URL 입력")
        var  et: EditText = EditText(this) // 입력 EditText
        fun urlImage(){
            var value:String  = et.getText().toString();
            Thread() {
                run() {
                    try{
                        var u = URL(value)                          // 입력된 URL 주소
                        var con: URLConnection = u.openConnection() // URL주소 데이터 읽어오기
                        var exitCode = con as HttpURLConnection     // 출력된 데이터
                        // if( 이미지 있으면 200 )
                        // else( 없으면 404 )
                        if ("200".equals(exitCode.getResponseCode().toString() )) {
                            imageList.add(value)                    // 추가된 URL 이미지 출력리스트에 추가
                            imageInsert(id!!,value, this)   // URL이미지 DB에 입력
                            runOnUiThread {                           // 입력된 URL 이미지 viewPager에 출력
                                viewPagerRefresh()
                                update_viewPager.setCurrentItem(imageList.size-1)  // viewPager 위치 끝으로
                            }
                        } else {    // 이미지가 없을경우, 실패했을경우
                            Looper.prepare()
                            Toast.makeText(this,"URL 이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }
                    }catch (e:Exception){ // 에러: https:// 을 안붙이거나 했을경우
                        try{
                            var u = URL("https://"+value);       // 입력된 URL에 https:// 붙이기
                            var con: URLConnection = u.openConnection()  // URL주소 데이터 읽어오기
                            var exitCode = con as HttpURLConnection      // 출력된 데이터
                            // if( 이미지 있으면 200 )
                            // else( 없으면 404 )
                            if ("200".equals(exitCode.getResponseCode().toString() + "")) {
                                value="https://"+value                   // 입력된 URL에 https:// 붙이기
                                imageList.add(value)                     // 추가된 URL 이미지 출력리스트에 추가
                                imageInsert(id!!,value, this)    // URL이미지 DB에 입력
                                runOnUiThread {                            // 입력된 URL 이미지 viewPager에 출력
                                    viewPagerRefresh()
                                    update_viewPager.setCurrentItem(imageList.size-1)  // viewPager 위치 끝으로
                                }
                            } else {        // 이미지가 없을경우, 실패했을경우
                                Looper.prepare()
                                Toast.makeText(this,"URL 이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                                Looper.loop()
                            }
                        }catch (e:java.lang.Exception){
                            Looper.prepare()
                            Toast.makeText(this,"URL 이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }
                    }
                }
            }.start();  // 쓰레드 실행
        }
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> urlImage()
                }
            }
        }
        dialog.setView(et)
        dialog.setPositiveButton("확인",dialog_listener)
        dialog.setNegativeButton("취소",dialog_listener)
        dialog.show()
    }

    // viewPager 출력
    fun viewPagerRefresh(){
        dbImageList.clear()
        imageList.clear()

        dbImageList = selectMemoImageList(id!!,this) // id값 이미지 리스트 가져오기

        // dbImageList : DB에서 뽑아온 이미지 리스트
        // DB에서 가져온 이미지를 viewPager list에 넣기
        for(img in dbImageList){
            imageList.add(img.images)
        }

        // DB에서 가져온 이미지가 0개      : GONE
        // DB에서 가져온 이미지가 1개 이상 : VISIBLE
        if (imageList.size != 0){
            update_viewPager.setVisibility(View.VISIBLE)
            update_background_view.setVisibility(View.VISIBLE)
        }else{
            update_viewPager.setVisibility(View.GONE)
            update_background_view.setVisibility(View.GONE)
        }
        update_viewPager.adapter = ViewPagerUpdateAdapter(this, imageList)
    }

    // 이미지 파일 삭제
    fun imageFileDelete(position:Int){
        var file = File(dbImageList.get(position).images)
        if(file.exists()){
            file.delete()
        }else{
            Log.d(TAG, " 삭제 실패 ")
        }
    }

    // exif 이미지 회전각 구하기
    fun getExifOrientation(filePath:String): Int {
        var exif:ExifInterface? = null
        try {
            exif = ExifInterface(filePath);
        } catch (e:IOException) {
            e.printStackTrace();
        }
        Log.d("!@#!!@#", exif.toString())
        if (exif != null) {
            var orientation:Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
            Log.d("!@#!!@# 1", orientation.toString())
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> return 270
                }
            }
        }

        return 0;
    }

    // 이미지 회전하기
    fun imgRotate(bitmap:Bitmap, angle: Int):Bitmap{
        var width = bitmap.getWidth();
        var height = bitmap.getHeight();

        var matrix = Matrix()
        matrix.postRotate(angle.toFloat());

        var resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();

        return resizedBitmap;
    }


}
