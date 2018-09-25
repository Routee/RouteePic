package com.routee.pic.adpter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.routee.pic.R
import com.routee.pic.entity.RouteePicEntity
import com.routee.pic.loader.ImageEngine

/**
 * @author: Routee
 * @date 2018/9/12
 * @mail chao.wang@xiangwushuo.com
 * ------------1.本类由Routee开发,阅读、修改时请勿随意修改代码排版格式后提交到git。
 * ------------2.阅读本类时，发现不合理请及时指正.
 * ------------3.如需在本类内部进行修改,请先联系Routee,若未经同意修改此类后造成损失本人概不负责。
 */
class RouteePicAdapter(var context: Context, var list: ArrayList<String>, var entity: RouteePicEntity) : RecyclerView.Adapter<VH>() {
    var mListener: SelectedCountListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        var view = LayoutInflater.from(context).inflate(R.layout.item_pic, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(vh: VH, position: Int) {
        if (ImageEngine.imageEngine == null) {
            throw RuntimeException("you must setImageLoader in ImageEngine first")
        }
        ImageEngine.imageEngine?.loadImage(list[position], vh.mIv!!)
        if (entity.SELECTED_PICS.contains(list[position])) {
            vh.mFl?.visibility = View.VISIBLE
            vh.mTv?.text = "".plus(entity.SELECTED_PICS.indexOf(list[position]) + 1)
        } else {
            vh.mFl?.visibility = View.GONE
        }
        vh.itemView.setOnClickListener {
            selectPic(list[position], position)
        }
    }

    private fun selectPic(s: String, position: Int) {
        if (entity.SELECTED_PICS.contains(s)) {
            entity.SELECTED_PICS.remove(s)
        } else {
            if (entity.SELECTED_PICS.size >= entity.IMAGES_COUNT) {
                return
            }
            entity.SELECTED_PICS.add(s)
        }
        if (mListener != null) {
            mListener?.countChanged(entity.SELECTED_PICS.size)
        }
        notifyItemChanged(position)
        notifyDataSetChanged()
    }

    fun setCountChangeListener(listener: SelectedCountListener) {
        mListener = listener
    }
}

interface SelectedCountListener {
    fun countChanged(count: Int)
}

class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var mIv: ImageView? = null
    var mFl: FrameLayout? = null
    var mTv: TextView? = null

    init {
        mIv = itemView.findViewById<ImageView>(R.id.iv)
        mFl = itemView.findViewById<FrameLayout>(R.id.fl)
        mTv = itemView.findViewById<TextView>(R.id.tv)
    }
}