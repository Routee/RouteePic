package com.routee.picdemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.routee.pic.R
import com.routee.pic.activity.RouteePicActivity
import com.routee.pic.entity.RouteePicEntity
import com.routee.pic.loader.ImageEngine
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE: Int = 0x200
    private var mEntity: RouteePicEntity = RouteePicEntity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        mBt.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkStoragePermission()
            } else {
                toRouteePic()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkStoragePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0x100)
        } else {
            checkCameraPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 0x101)
        } else {
            toRouteePic()
        }
    }

    private fun toRouteePic() {
        var intent = Intent(this, RouteePicActivity::class.java)
        var b = Bundle()
        b.putSerializable(RouteePicEntity.ENTITY_NAME, mEntity)
        intent.putExtras(b)
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun initView() {
        mRvPic.layoutManager = GridLayoutManager(this, 4)
        mRvPic.adapter = ImageAdapter(this, mEntity.SELECTED_PICS)
        ImageEngine.imageEngine = GlideImageLoader(application)
    }

    class ImageAdapter(var context: Context, var list: ArrayList<String>) : RecyclerView.Adapter<VH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            var view = LayoutInflater.from(context).inflate(R.layout.item_image_rv_main, parent, false)
            return VH(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            Glide.with(context).load(list[position]).into(holder.mIv)
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mIv = itemView.findViewById<ImageView>(R.id.iv)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    checkCameraPermission()
                } else {
                    Toast.makeText(this, "无文件读取权限", Toast.LENGTH_LONG).show()
                }
            }
        } else if (requestCode == 0x101) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    toRouteePic()
                } else {
                    Toast.makeText(this, "无相机权限", Toast.LENGTH_LONG).show()
                }
            }
        } else if (requestCode == REQUEST_CODE) {
            if (resultCode == RouteePicEntity.RESULT_CODE) {
                var b: Bundle = data?.extras ?: Bundle()
                var entity = b.getSerializable(RouteePicEntity.ENTITY_NAME) as RouteePicEntity
                mEntity.SELECTED_PICS.clear()
                mEntity.SELECTED_PICS.addAll(entity.SELECTED_PICS)
                mRvPic.adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ImageEngine.imageEngine = null
    }
}
