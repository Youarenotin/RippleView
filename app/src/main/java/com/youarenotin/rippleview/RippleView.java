package com.youarenotin.rippleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 作者：lubo on 7/1 0001 10:59
 * 邮箱：lubo_wen@126.com
 */
public class RippleView extends RelativeLayout {
    public RippleView(Context context) {
        super(context);
        init(context,null);
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);

    }


}
