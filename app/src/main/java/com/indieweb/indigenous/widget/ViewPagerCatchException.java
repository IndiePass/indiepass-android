package com.indieweb.indigenous.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

// See https://github.com/chrisbanes/PhotoView/issues/31#issuecomment-19803926
public class ViewPagerCatchException extends android.support.v4.view.ViewPager {

    public ViewPagerCatchException(Context context) {
        super(context);
    }

    public ViewPagerCatchException(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ignored) { }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ignored) { }
        return false;
    }
}