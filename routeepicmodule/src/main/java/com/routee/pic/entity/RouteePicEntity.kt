package com.routee.pic.entity

import android.os.Environment
import java.io.File
import java.io.Serializable

/**
 * @author: Routee
 * @date 2018/9/18
 * @mail chao.wang@xiangwushuo.com
 * ------------1.本类由Routee开发,阅读、修改时请勿随意修改代码排版格式后提交到git。
 * ------------2.阅读本类时，发现不合理请及时指正.
 * ------------3.如需在本类内部进行修改,请先联系Routee,若未经同意修改此类后造成损失本人概不负责。
 */
class RouteePicEntity : Serializable {
    companion object {
        val ENTITY_NAME = "ROUTEE_ENTITY"
        val RESULT_CODE: Int = 200
    }

    var PIC_SAVE_PATH = Environment.getExternalStorageDirectory().absolutePath + File.separator + "routeePic"
    var IMAGES_COUNT = 9
    var SELECTED_PICS: ArrayList<String> = arrayListOf()
}