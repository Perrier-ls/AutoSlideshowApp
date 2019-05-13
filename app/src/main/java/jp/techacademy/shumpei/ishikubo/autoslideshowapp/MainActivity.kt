package jp.techacademy.shumpei.ishikubo.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    var slideFlag = true
    var imageUriList = ArrayList<Uri>()
    var imageUriListIndex = 0
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        prevBtn.setOnClickListener(this)
        nextBtn.setOnClickListener(this)
        slideBtn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.prevBtn -> displayPrevImage()
            R.id.nextBtn -> displayNextImage()
            R.id.slideBtn -> slideToggle()
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageUriList.add(imageUri)
            } while (cursor.moveToNext())
            imageDisplay.setImageURI(imageUriList.get(0))
        }
        cursor.close()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    fun slideToggle() {
        if (slideFlag) {
            slideFlag = !slideFlag
            slideBtn.text = "stop"
            nextBtn.setEnabled(false)
            prevBtn.setEnabled(false)
            slideShowStart()
        }  else {
            slideFlag = !slideFlag
            slideBtn.text = "start"
            nextBtn.setEnabled(true)
            prevBtn.setEnabled(true)
            slideShowStop()
        }
    }

    fun displayNextImage() {
        if (0 < imageUriList.size){
            imageUriListIndex++
            if (imageUriListIndex >= imageUriList.size){
                imageUriListIndex = 0
            }
            imageDisplay.setImageURI(imageUriList.get(imageUriListIndex))
        } else {
            var toast = Toast.makeText(this, "画像ファイルへのアクセスを許可してください", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun displayPrevImage() {
        if (0 < imageUriList.size) {
            imageUriListIndex--
            if (imageUriListIndex < 0) {
                imageUriListIndex = imageUriList.size - 1
            }
            imageDisplay.setImageURI(imageUriList.get(imageUriListIndex))
        } else {
            var toast = Toast.makeText(this, "画像ファイルへのアクセスを許可してください", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun slideShowStart() {
        if (mTimer == null && 0 < imageUriList.size) {
            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post {
                        imageUriListIndex++
                        if (imageUriListIndex >= imageUriList.size){
                            imageUriListIndex = 0
                        }
                        imageDisplay.setImageURI(imageUriList.get(imageUriListIndex))
                    }
                }
            }, 2000, 2000)
        }
    }

    fun slideShowStop() {
        if (mTimer != null) {
            mTimer!!.cancel()
            mTimer = null
        }
    }

}
