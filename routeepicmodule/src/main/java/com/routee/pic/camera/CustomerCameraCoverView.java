package com.routee.pic.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.routee.pic.tools.DisplayUtils;

/**
 * @author: Routee
 * @date 2018/7/14
 * @mail wangc4@qianbaocard.com
 * ------------1.本类由Routee开发,阅读、修改时请勿随意修改代码排版格式后提交到git。
 * ------------2.阅读本类时，发现不合理请及时指正.
 * ------------3.如需在本类内部进行修改,请先联系Routee,若未经同意修改此类后造成损失本人概不负责。
 */
public class CustomerCameraCoverView extends View {
    private Rect mRect;
    private Paint mPaint;

    public CustomerCameraCoverView(Context context) {
        this(context, null);
    }

    public CustomerCameraCoverView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomerCameraCoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRect == null) {
            super.onDraw(canvas);
        } else {
            drawRect(canvas);
            drawLine(canvas);
        }
    }

    private void drawLine(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#ffffff"));
        canvas.drawRect(mRect.left, mRect.top, mRect.left + DisplayUtils.dp2px(getContext(), 2), mRect.top + DisplayUtils.dp2px(getContext(), 16), mPaint);
        canvas.drawRect(mRect.left, mRect.top, mRect.left + DisplayUtils.dp2px(getContext(), 16), mRect.top + DisplayUtils.dp2px(getContext(), 2), mPaint);
        canvas.drawRect(mRect.right - DisplayUtils.dp2px(getContext(), 16), mRect.top, mRect.right, mRect.top + DisplayUtils.dp2px(getContext(), 2), mPaint);
        canvas.drawRect(mRect.right - DisplayUtils.dp2px(getContext(), 2), mRect.top, mRect.right, mRect.top + DisplayUtils.dp2px(getContext(), 16), mPaint);
        canvas.drawRect(mRect.left, mRect.bottom - DisplayUtils.dp2px(getContext(), 16), mRect.left + DisplayUtils.dp2px(getContext(), 2), mRect.bottom, mPaint);
        canvas.drawRect(mRect.left, mRect.bottom - DisplayUtils.dp2px(getContext(), 2), mRect.left + DisplayUtils.dp2px(getContext(), 16), mRect.bottom, mPaint);
        canvas.drawRect(mRect.right - DisplayUtils.dp2px(getContext(), 16), mRect.bottom - DisplayUtils.dp2px(getContext(), 2), mRect.right, mRect.bottom, mPaint);
        canvas.drawRect(mRect.right - DisplayUtils.dp2px(getContext(), 2), mRect.bottom - DisplayUtils.dp2px(getContext(), 16), mRect.right, mRect.bottom, mPaint);
    }


    private void drawRect(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#33000000"));
        canvas.drawRect(0, 0, getWidth(), mRect.top, mPaint);
        canvas.drawRect(0, mRect.top, mRect.left, mRect.bottom, mPaint);
        canvas.drawRect(mRect.right, mRect.top, getWidth(), mRect.bottom, mPaint);
        canvas.drawRect(0, mRect.bottom, getWidth(), getHeight(), mPaint);
    }

    public void setRect(Rect rect) {
        mRect = rect;
        invalidate();
    }
}
