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
import com.bumptech.glide.Glide
import com.project.memo.Adapter.ViewPagerAdapter
import com.project.memo.Adapter.imageInsert
import com.project.memo.Adapter.memoInsert
import com.project.memo.Adapter.selectMemoId
import com.project.memo.R
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_add_memo.*
import kotlinx.android.synthetic.main.activity_add_memo_viewpager.view.*
import kotlinx.android.synthetic.main.activity_memo_detail_viewpager.view.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.*

class AddMemoActivity : AppCompatActivity() {

    private val TAG :String = "AddMemoActivity"
    // 추가할 이미지 리스트
    val imageList : MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_memo)

        addBtn.setOnClickListener {
            memoInsert()    // 메모추가
            finish()
        }

        // 이미지 추가 버튼
        addImageBtn.setOnClickListener {
            // 퍼미션 ( 권한 설정 )
            permission()
        }

    }

    // 뒤로가기시 추가하기
    override fun onBackPressed() {
        super.onBackPressed()
        memoInsert() // 메모 추가
        Toast.makeText(this, " 메모가 자동추가되었습니다.",Toast.LENGTH_SHORT).show()
    }

    // 메모추가
    fun memoInsert(){
        var title = addTitle.getText().toString()
        var content = addContent.getText().toString()

        // 제목이 없을경우 '제목없음'으로 입력
        if(title.replace(" ","").equals("")){ title = "제목없음" }
        // 내용이 없을경우 '내용없음'으로 입력
        if(content.replace(" ","").equals("") ){ content = "내용없음" }

        memoInsert(title,content,this)  // 제목, 내용으로 메모정보 DB에 저장함

        var list = selectMemoId(this)   // 가장 최근에 저장한 값의 id를 가져옴
        val memoId = list.get(0).id              // 가져온 id

        // imageList : 입력할 이미지 리스트
        // 이미지리스트에 있는 이미지를 전부 DB에 저장한다
        for(image in imageList){
            /* memoId : 이미지를 가지고있는 메모의 id
             * image : 이미지
             * 이미지를 입력
             */
            imageInsert(memoId,image, this)
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
                ActivityCompat.requestPermissions( this, arrayOf(
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET), 1 )
            }
        }else{
            addDialog()
        }

    }

    // 이미지 추가 다이얼로그
    fun addDialog(){
        var dialog = AlertDialog.Builder(this)
        dialog.setTitle("이미지 추가")

        fun urlDiolog(){ addDialogURL() }   // URL 이미지 입력 다이얼로그
        fun photoDialog(){                  // 카메라/갤러리 이미지 추가
            // tedImagePicker 라이브러리
            TedImagePicker.with(this)
                .startMultiImage { uriList -> getImageList(uriList) }
        }
        var dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> urlDiolog()
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
        viewPager.clipToPadding = false // 패딩공간 제거
        //imageList.clear()
        for(Uri in image){
            if(Uri!=null) {
                //var bit = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri) // URI를 비트맵으로 변환

                var degree = getExifOrientation(Uri.toString().substring(7,Uri.toString().length))                  // 바뀐 회전각 구하기
                var bitRotate = imgRotate(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri), degree)// 바뀐 각만큼 다시 바꾸기

                lateinit var downImage:String
                if(degree == 0){
                    downImage = saveBitmaptoJpeg(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri),"/Pictures/MemoApp")  // 이미지 다운로드
                }else{
                    downImage = saveBitmaptoJpeg(bitRotate,"/Pictures/MemoApp")      // 이미지 다운로드
                }
                imageList.add(downImage)  // 추가한 이미지를 viewPager imageList에 추가

                // viewPager에 추가한 이미지 출력하기
                add_background_view.setVisibility(View.VISIBLE)
                viewPager.setVisibility(View.VISIBLE)
                viewPager.adapter = ViewPagerAdapter(this, imageList)
                viewPager.setCurrentItem(imageList.size-1)  // viewPager 위치 끝으로

            }
        }
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


    // URL 이미지 입력 다이얼로그
    fun addDialogURL(){
        var dialog = AlertDialog.Builder(this)
        dialog.setTitle("이미지URL 입력")
        var  et: EditText = EditText(this);
        fun urlImage(){
            var value:String  = et.getText().toString();
            Thread() {
                run() {
                    try{
                        var u = URL(value)                          // 입력된 URL 주소
                        var con: URLConnection = u.openConnection() // URL주소 데이터 읽어오기
                        var exitCode = con as HttpURLConnection     // 출력된 데이터
                        // true(이미지 있음) : 200 >> if
                        // false(이미지 없음) : 404 >> else
                        if ("200".equals(exitCode.getResponseCode().toString())) {
                            imageList.add(value)                     // 추가된 URL 이미지 출력리스트에 추가
                            runOnUiThread {                            // 입력된 URL 이미지 viewPager에 출력
                                viewPager.setVisibility(View.VISIBLE)
                                viewPager.adapter = ViewPagerAdapter(this, imageList)
                            }
                        } else {        // 이미지가 없을경우, 실패했을경우
                            Looper.prepare()
                            Toast.makeText(this,"URL 이미지를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }

                    }catch (e:Exception){
                        try{
                            var u = URL("https://"+value)       // 입력된 URL에 https:// 붙이기
                            var con: URLConnection = u.openConnection() // URL주소 데이터 읽어오기
                            var exitCode = con as HttpURLConnection     // 출력된 데이터
                            // true(이미지 있음) : 200 >> if
                            // false(이미지 없음) : 404 >> else
                            if ("200".equals(exitCode.getResponseCode().toString())) {
                                value="http://"+value                   // 입력된 URL에 https:// 붙이기
                                imageList.add(value)                    // 추가된 URL 이미지 출력리스트에 추가
                                runOnUiThread {                           // 입력된 URL 이미지 viewPager에 출력
                                    viewPager.setVisibility(View.VISIBLE)
                                    viewPager.adapter = ViewPagerAdapter(this, imageList)
                                }

                            } else {
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
            }.start();          // 쓰레드 실행
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


    /* imageList 의 이미지가 제거되어 크기가 0일때(= 이미지 없음)
     *  viewPager를 안보이게 Visible을 GONE으로 설정
     */
    fun imageViewPager() {
        Log.d(TAG," imageViewPager "+imageList.size)
        if(imageList.size == 0){
            viewPager.setVisibility(View.GONE)
            add_background_view.setVisibility(View.GONE)
        }else{
            viewPager.setVisibility(View.VISIBLE)
            add_background_view.setVisibility(View.VISIBLE)
        }
    }

    // 이미지 파일 삭제
    fun imageFileDelete(imagePath:String){
        var file = File(imagePath)
        if(file.exists()){
            file.delete()
        }else{
            Log.d(TAG, " 삭제 실패 ")
        }
    }
    // 회전각 구하기
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
        matrix.postRotate(angle.toFloat()); // 받은값 만큼 이미지 회전

        var resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();

        return resizedBitmap;
    }


}
