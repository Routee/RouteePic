package com.routee.pic.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import com.routee.pic.R
import com.routee.pic.adpter.RouteePicAdapter
import com.routee.pic.adpter.SelectedCountListener
import com.routee.pic.camera.CameraView
import com.routee.pic.camera.CameraView.*
import com.routee.pic.dialog.LoadingDialog
import com.routee.pic.entity.ImageFolder
import com.routee.pic.entity.RouteePicEntity
import com.routee.pic.tools.BitmapUtil
import com.routee.pic.tools.DisplayUtils
import kotlinx.android.synthetic.main.activity_routee_pic.*
import kotlinx.android.synthetic.main.viewstub_pic.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class RouteePicActivity : AppCompatActivity(), SelectedCountListener {
    //所有图片的图片地址
    var mAllPicPicpath: ArrayList<String> = arrayListOf()
    //所有相册地址
    var mDirPaths: ArrayList<String> = arrayListOf()
    //所有相册信息
    var mImageFolders: ArrayList<ImageFolder> = arrayListOf()
    //拍照区域
    var mRect: Rect = Rect()
    var mBackgroundHandler: Handler? = null
    var mLoading: LoadingDialog? = null
    //相机获取的bitmap
    var mNewBm: Bitmap? = null
    //闪光灯状态
    val mFlashStateArray: Array<Int> = arrayOf(FLASH_OFF, FLASH_ON, FLASH_AUTO)
    //闪光灯icon
    val mFlashSrcArray: Array<Int> = arrayOf(R.mipmap.icon_flash_off, R.mipmap.icon_flash_on, R.mipmap.icon_flash_auto)
    var mFlashState: Int = FLASH_AUTO
    //参数集
    var entity: RouteePicEntity = RouteePicEntity()

    private val mCallback = object : CameraView.Callback() {

        override fun onCameraOpened(cameraView: CameraView) {}

        override fun onCameraClosed(cameraView: CameraView) {}

        override fun onPictureTaken(cameraView: CameraView, data: ByteArray) {
            getBackgroundHandler().post(Runnable {
                var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                if (bitmap.width > bitmap.height) {
                    val m = Matrix()
                    try {
                        if (cameraView.facing == FACING_FRONT) {
                            m.setRotate(270f, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())//90就是我们需要选择的90度
                        } else {
                            m.setRotate(90f, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())//90就是我们需要选择的90度
                        }
                        val bmp2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                        bitmap.recycle()
                        bitmap = bmp2
                    } catch (ex: Exception) {
                        print("创建图片失败！$ex")
                    }
                }
                if (cameraView.facing == FACING_FRONT) {
                    val m = Matrix()
                    m.setScale(-1.0f, 1.0f)
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                }
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()
                val widthScale: Float
                val heghtScale: Float
                if (cameraView.width < cameraView.height * bitmapWidth / bitmapHeight) {
                    //                setMeasuredDimension(height * bitmapWidth / bitmapHeight, height);
                    widthScale = bitmapHeight / cameraView.height
                    heghtScale = bitmapHeight / cameraView.height
                } else {
                    //                setMeasuredDimension(width, width * bitmapHeight / bitmapWidth);
                    widthScale = bitmapWidth / cameraView.width
                    heghtScale = bitmapWidth / cameraView.width
                }
                //            android.util.Log.e("xxxxxx", "mSensorOrientation = " + mSensorOrientation);
                if (widthScale < 1.0 || heghtScale < 1.0) {
                    mNewBm = bitmap
                } else {
                    mNewBm = Bitmap.createBitmap(bitmap, (mRect.left * widthScale).toInt(), (mRect.top * heghtScale).toInt(),
                            (mRect.width() * widthScale).toInt(), (mRect.height() * heghtScale).toInt())
                }
                val matrix = Matrix()
                matrix.setScale(0.5f, 0.5f)
                while (mNewBm?.height ?: 0 > 2000) {
                    mNewBm = Bitmap.createBitmap(mNewBm, 0, 0, mNewBm?.width
                            ?: 0, mNewBm?.height ?: 0, matrix, false)
                }
                mNewBm = BitmapUtil.compressImage(mNewBm)
                runOnUiThread {
                    cameraView.stop()
                    showLoading(false)
                    ivCameraResult.visibility = View.VISIBLE
                    ivCameraResult.setImageBitmap(mNewBm)
                    cameraFinishAnim()
                }
            })
        }
    }

    private fun cameraFinishAnim() {
        ivPic.visibility = View.GONE
        ivRepic.animate().translationX(DisplayUtils.dp2px(this, 100).toFloat()).setDuration(200).start()
        ivDone.animate().translationX(-DisplayUtils.dp2px(this, 100).toFloat()).setDuration(200).start()
    }

    private fun cameraRestartAnim() {
        ivCameraResult.visibility = View.INVISIBLE
        cameraView.start()
        ivPic.postDelayed({ ivPic.visibility = View.VISIBLE }, 200)
        ivRepic.animate().translationX(DisplayUtils.dp2px(this, 0).toFloat()).setDuration(200).start()
        ivDone.animate().translationX(-DisplayUtils.dp2px(this, 0).toFloat()).setDuration(200).start()
    }

    private fun getBackgroundHandler(): Handler {
        if (mBackgroundHandler == null) {
            val thread = HandlerThread("background")
            thread.start()
            mBackgroundHandler = Handler(thread.looper)
        }
        return mBackgroundHandler!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routee_pic)
        initConfig()
        initView()
        initAlbum()
    }

    private fun initConfig() {
        try {
            entity = intent.extras.get(RouteePicEntity.ENTITY_NAME) as RouteePicEntity
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler?.looper?.quitSafely()
            } else {
                mBackgroundHandler?.looper?.quit()
            }
            mBackgroundHandler = null
        }
    }

    private fun initView() {
        cbCam.isChecked = true
        initCamera()

        //绑定拍摄照片页的控件
        ivCloseCam.setOnClickListener { finish() }
        ivFlashSwitch.setOnClickListener {
            switchFlash()
        }
        ivCameraSwitch.setOnClickListener {
            switchCamera()
        }

        cbPic.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (viewstub == null) {
                    mLlPic.visibility = View.VISIBLE
                } else {
                    //绑定选择照片页面的控件
                    viewstub.inflate()
                    ivClosePic.setOnClickListener { finish() }
                    tvFinishPic.setOnClickListener {
                        setResultPic()
                    }
                    tvAlbumSwitch.setOnClickListener { toast("点击了相册切换") }
                    rvPic.layoutManager = GridLayoutManager(this@RouteePicActivity, 4)
                    var picAdapter = RouteePicAdapter(this@RouteePicActivity, mAllPicPicpath, entity)
                    picAdapter.mListener = this
                    rvPic.adapter = picAdapter
                    showPics()
                    countChanged(entity.SELECTED_PICS.size)
                }
            } else {
                if (viewstub != null) {
                    return@OnCheckedChangeListener
                } else {
                    mLlPic.visibility = View.GONE
                }
            }
        })
    }

    private fun switchCamera() {
        if (cameraView.facing == FACING_FRONT) {
            cameraView.facing = FACING_BACK
            ivFlashSwitch.visibility = View.VISIBLE
        } else {
            cameraView.facing = FACING_FRONT
            ivFlashSwitch.visibility = View.GONE
        }
    }

    //初始化相机
    private fun initCamera() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mRect = Rect((resources.displayMetrics.widthPixels - DisplayUtils.dp2px(this, 220)) / 2
                , (resources.displayMetrics.heightPixels - DisplayUtils.dp2px(this, 400)) / 2
                , (resources.displayMetrics.widthPixels + DisplayUtils.dp2px(this, 220)) / 2
                , (resources.displayMetrics.heightPixels + DisplayUtils.dp2px(this, 40)) / 2)
        rectView.setRect(mRect)
        if (cameraView != null) {
            cameraView.addCallback(mCallback)
        }
        ivPic.setOnClickListener {
            showLoading(true)
            cameraView.takePicture()
        }
        ivRepic.setOnClickListener {
            cameraRestartAnim()
        }
        ivDone.setOnClickListener {
            if (mNewBm == null) {
                return@setOnClickListener
            }
            savePic()
        }
    }

    private fun switchFlash() {
        var position = mFlashStateArray.indexOf(mFlashState)
        mFlashState = mFlashStateArray[++position % mFlashStateArray.size]
        ivFlashSwitch.setImageResource(mFlashSrcArray[position % mFlashStateArray.size])
        cameraView.flash = mFlashState
    }

    private fun showLoading(b: Boolean) {
        if (mLoading == null) {
            mLoading = LoadingDialog()
        }
        if (b) {
            mLoading!!.show(fragmentManager, "loading")
        } else {
            mLoading!!.dismissAllowingStateLoss()
            Handler(Looper.getMainLooper()).sendEmptyMessage(0)
            mLoading = null
        }
    }

    private fun showPics() {
        rvPic.adapter.notifyDataSetChanged()
    }

    //初始化相册资源
    private fun initAlbum() {
        mAllPicPicpath.clear()
        mDirPaths.clear()
        mImageFolders.clear()
        var executorService = Executors.newCachedThreadPool()
        var mimeType = MediaStore.Images.Media.MIME_TYPE
        executorService.run {
            var imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            var mCursor = contentResolver.query(imgUri, null, mimeType + "=? or " + mimeType + "=? or " + mimeType + "=?"
                    , arrayOf("image/jpg", "image/jpeg", "image/png"), MediaStore.Images.Media.DATE_MODIFIED + " DESC")
            while (mCursor.moveToNext()) {
                // 获取图片的路径
                val path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA))
                mAllPicPicpath.add(path)
                var parentFile = File(path).getParentFile();
                if (parentFile == null) {
                    continue//不获取sd卡根目录下的图片
                }
                var parentPath = parentFile.getAbsolutePath();//2.获取图片的文件夹信息
                var parentName = parentFile.getName();
                var imageFloder: ImageFolder

                //这个操作，可以提高查询的效率，将查询的每一个图片的文件夹的路径保存到集合中，
                //如果存在，就直接查询下一个，避免对每一个文件夹进行查询操作
                if (mDirPaths.contains(parentPath)) {
                    var position = mDirPaths.indexOf(parentPath)
                    mImageFolders[position].picList.add(path)
                    continue
                } else {
                    mDirPaths.add(parentPath) //将父路径添加到集合中
                    imageFloder = ImageFolder()
                    imageFloder.firstImagePath = path
                    imageFloder.dirPath = parentPath
                    imageFloder.dirName = parentName
                    mImageFolders.add(imageFloder)  //添加每一个相册
                }
            }
            mCursor.close()
        }
    }

    //保存拍摄的照片
    private fun savePic() {
        if (mNewBm == null || entity.IMAGES_COUNT < 1) {
            return
        }
        if (entity.IMAGES_COUNT <= entity.SELECTED_PICS.size) {
            toast("超出图片数量限制")
            return
        }
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        showLoading(true)
        Executors.newCachedThreadPool().run {
            var mCamResultPath = BitmapUtil.saveMyBitmap(entity.PIC_SAVE_PATH, "routee" + sdf.format(Date()) + ".png", mNewBm)
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(mCamResultPath))))
            initAlbum()
            entity.SELECTED_PICS.add(mCamResultPath)
            runOnUiThread {
                showLoading(false)
                setResultPic()
            }
        }
    }

    private fun setResultPic() {
        var b = Bundle()
        b.putSerializable(RouteePicEntity.ENTITY_NAME, entity)
        var intent = Intent()
        intent.putExtras(b)
        setResult(RouteePicEntity.RESULT_CODE, intent)
        finish()
    }

    override fun countChanged(count: Int) {
        if (count == 0) {
            tvFinishPic.text = "完成"
        } else {
            tvFinishPic.text = "选择(" + count + ")"
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this@RouteePicActivity, msg, Toast.LENGTH_LONG).show()
    }
}
