package com.routee.pic.tools;

import android.content.Context;

public class DisplayUtils {
    public static int dp2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
