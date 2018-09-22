package com.routee.picdemo

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.routee.pic.loader.ImageLoader

/**
 * @author: Routee
 * @date 2018/9/19
 * @mail chao.wang@xiangwushuo.com
 * ------------1.本类由Routee开发,阅读、修改时请勿随意修改代码排版格式后提交到git。
 * ------------2.阅读本类时，发现不合理请及时指正.
 * ------------3.如需在本类内部进行修改,请先联系Routee,若未经同意修改此类后造成损失本人概不负责。
 */
class GlideImageLoader(val context: Context) : ImageLoader {

    override fun loadImage(path: String, view: ImageView) {
        Glide.with(context).load(path).into(view)
    }
}