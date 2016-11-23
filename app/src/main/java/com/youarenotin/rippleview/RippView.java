package com.youarenotin.rippleview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

/**
 * 作者：lubo on 7/1 0001 12:07
 * 邮箱：lubo_wen@126.com
 */
public class RippView extends RelativeLayout {
    /**
     * 这个rippleview的宽度
     */
    private int WIDTH;
    /**
     * 这个rippleview的高度
     */
    private int HEIGHT;
    /**
     * 每次时间增加的变量 10ms
     */
    private int FRAME_RATE = 10;
    /**
     * 波纹动画总时间
     */
    private int DURATION = 400;
    /**
     *
     */
    private int PAINT_ALPHA = 90;
    /**
     * 延迟绘制handler
     */
    private Handler mCanvasHandler;
    /**
     * 波纹圆形的最大半径
     */
    private float radiusMax = 0;
    /**
     * 波纹动画是否在进行中
     */
    private boolean isStartAnimationRunning = false;
    /**
     * 波纹由最小半径到最大期间 计数器的次数 (timer++)*FRAME_RATE <= Duration
     */
    private int timer = 0;
    private int timerEmpty = 0;
    private int durationEmpty = -1;
    private float x = -1;
    private float y = -1;
    /**
     * 放大或缩小的变化时间
     */
    private int zoomDuration;
    /**
     * 缩放比例
     */
    private float zoomScale;
    /**
     * 缩放动画 一个补间动画
     */
    private ScaleAnimation scaleAnimation;
    private Boolean hasToZoom;
    private Boolean isCentered;
    /**
     * 波纹类型
     */
    private Integer rippleType;
    /**
     * 波纹画笔
     */
    private Paint paint;
    private Bitmap originBitmap;
    /**
     * 波纹颜色
     */
    private int rippleColor;
    /**
     * 该rippleview中的子view
     */
    private View childView;
    /**
     * 基于该rippleview的weight和height算出的radius在进行padding
     */
    private int ripplePadding;

    private GestureDetector gestureDetector;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
    public RippView(Context context) {
        super(context);
    }
    public RippView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public RippView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippView);
        rippleColor = typedArray.getColor(R.styleable.RippView_rv_color, getResources().getColor(R.color.colorPrimaryDark));
        rippleType = typedArray.getInt(R.styleable.RippView_rv_type, 0);
        hasToZoom = typedArray.getBoolean(R.styleable.RippView_rv_zoom, false);
        isCentered = typedArray.getBoolean(R.styleable.RippView_rv_centered, false);
        DURATION = typedArray.getInteger(R.styleable.RippView_rv_rippleDuration, DURATION);
        FRAME_RATE = typedArray.getInteger(R.styleable.RippView_rv_framerate, FRAME_RATE);
        PAINT_ALPHA = typedArray.getInteger(R.styleable.RippView_rv_alpha, PAINT_ALPHA);
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippView_rv_ripplePadding, 0);
        zoomScale = typedArray.getFloat(R.styleable.RippView_rv_zoomScale, 1.0f);
        zoomDuration = typedArray.getInt(R.styleable.RippView_rv_zoomDuration, 200);
        mCanvasHandler = new Handler();
        paint = new Paint();
        paint.setAntiAlias(true);//抗锯齿
        paint.setStyle(Paint.Style.FILL);//填充绘制
        paint.setColor(rippleColor);//设置颜色
        paint.setAlpha(PAINT_ALPHA);//设置透明度
        this.setWillNotDraw(false);//基类属于viewgroup默认不做绘制只充当容器,但该view要绘制故清除这个标志.
        this.setDrawingCacheEnabled(true);
        //初始化点击,双击,长按等手势识别
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        this.setDrawingCacheEnabled(true);
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        childView = child;
        super.addView(child, index, params);
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isStartAnimationRunning) {//如果onTouch事件出发了绘制波纹 ---正在波纹动画正在运行
            if (DURATION <= timer * FRAME_RATE) {//将绘制分成duration/FRAME_RATE这么多次 如果大于duration表示绘制结束
                isStartAnimationRunning = false;
                timer = 0;
                durationEmpty = -1;
                timerEmpty = 0;
                canvas.restore();
                invalidate();
                return;
            } else
                mCanvasHandler.postDelayed(runnable, FRAME_RATE);//刷新view
            if (timer == 0)
                canvas.save();
            canvas.drawCircle(x, y, (radiusMax * (((float) timer * FRAME_RATE) / DURATION)), paint);
//            paint.setColor(getResources().getColor(android.R.color.holo_red_light));
            if (rippleType == 1 && originBitmap != null && (((float) timer * FRAME_RATE) / DURATION) > 0.4f) {
                if (durationEmpty == -1)
                    durationEmpty = DURATION - timer * FRAME_RATE;
                timerEmpty++;
                final Bitmap tmpBitmap = getCircleBitmap((int) ((radiusMax) * (((float) timerEmpty * FRAME_RATE) / (durationEmpty))));
                canvas.drawBitmap(tmpBitmap, 0, 0, paint);
                tmpBitmap.recycle();
            }
            paint.setColor(rippleColor);
            if (rippleType == 1) {
                if ((((float) timer * FRAME_RATE) / DURATION) > 0.6f)
                    paint.setAlpha((int) (PAINT_ALPHA - ((PAINT_ALPHA) * (((float) timerEmpty * FRAME_RATE) / (durationEmpty)))));
                else
                    paint.setAlpha(PAINT_ALPHA);
            } else
                paint.setAlpha((int) (PAINT_ALPHA - ((PAINT_ALPHA) * (((float) timer * FRAME_RATE) / DURATION))));
            timer++;
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        WIDTH = w;
        HEIGHT = h;
        scaleAnimation = new ScaleAnimation(1.0f, zoomScale, 1.0f, zoomScale, w / 2, h / 2);
        scaleAnimation.setDuration(zoomDuration);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event) && !isStartAnimationRunning) {
            if (hasToZoom)
                this.startAnimation(scaleAnimation);//开始缩放的补间动画
            radiusMax = Math.max(WIDTH, HEIGHT);
            if (rippleType != 2)
                radiusMax /= 2;
            radiusMax -= getResources().getDisplayMetrics().density*ripplePadding;
            if (isCentered || rippleType == 1) {//波纹出发点
                this.x = getWidth() / 2;
                this.y = getHeight() / 2;
            } else {
                this.x = event.getX();//取相对于这个view左上角的坐标
                this.y = event.getY();
            }
            isStartAnimationRunning = true;//设置开启波纹动画标志
            if (rippleType == 1 && originBitmap == null)
                originBitmap = getDrawingCache(true);//取出这个view转化成的bitmap
            invalidate();//导致ondraw被调用
        }
        childView.onTouchEvent(event);//这里可以直接在onInterceptTouchEvent里返回true 通过这句将触摸事件传递到子view,也可以
        return true;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }
    private Bitmap getCircleBitmap(final int radius) {
        final Bitmap output = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect((int) (x - radius), (int) (y - radius), (int) (x + radius), (int) (y + radius));
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(x, y, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originBitmap, rect, rect, paint);
        return output;
    }
}
