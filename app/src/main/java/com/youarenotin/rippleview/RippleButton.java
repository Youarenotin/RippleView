package com.youarenotin.rippleview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.nineoldandroids.view.ViewHelper;

/**
 * 作者：lubo on 7/1 0001 10:59
 * 邮箱：lubo_wen@126.com
 */
public class RippleButton extends Button {

    /**
     * 按下x坐标
     */
    private float mDownX;
    /**
     * 按下y坐标
     */
    private float mDownY;
    /**
     * 透明度因子
     */
    private float mAlphaFactor;
    /**
     * dp与像素的比例
     */
    private float mDensity;
    /**
     * 波纹半径
     */
    private float mRadius;
    /**
     * 波纹最大半径
     */
    private float mMaxRadius;
    /**
     * 波纹颜色
     */
    private int mRippleColor;
    /**
     * 波纹
     */
    private boolean mIsAnimating = false;
    private boolean mHover = true;
    private RadialGradient mRadialGradient;
    private Paint mPaint;
    private ObjectAnimator mRadiusAnimator;

    private int dpToPix(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    public RippleButton(Context context) {
        this(context, null);
    }

    public RippleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RippleButton);
        mRippleColor = a.getColor(R.styleable.RippleButton_rippleColor,
                mRippleColor);
        mAlphaFactor = a.getFloat(R.styleable.RippleButton_alphaFactor,
                mAlphaFactor);
        mHover = a.getBoolean(R.styleable.RippleButton_hover, mHover);
        a.recycle();
    }

    public void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(100);
        setRippleColor(Color.BLACK, 0.2f);

    }

    public void setRippleColor(int rippleColor, float alphaFactor) {
        mRippleColor = rippleColor;
        mAlphaFactor = alphaFactor;
    }

    public void setHover(boolean enabled) {
        mHover = enabled;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = (float) Math.sqrt(w * w + h * h);
    }

    private boolean mAnimationIsCancel;
    private Rect mRect;

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        Log.d("TouchEvent", String.valueOf(event.getActionMasked()));
        Log.d("mIsAnimating", String.valueOf(mIsAnimating));
        Log.d("mAnimationIsCancel", String.valueOf(mAnimationIsCancel));
        boolean superResult = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancel = false;
            mDownX = event.getX();
            mDownY = event.getY();

            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", 0, dpToPix(50))
                    .setDuration(400);
            mRadiusAnimator
                    .setInterpolator(new LinearInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleButton.this, 1);
                    mIsAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {
            mDownX = event.getX();
            mDownY = event.getY();

            // Cancel the ripple animation when moved outside
            if (mAnimationIsCancel =!mRect.contains(
                    getLeft() + (int) event.getX(),
                    getTop() + (int) event.getY())) {
                setRadius(0);
            } else {
                setRadius(dpToPix(40));
            }
            if (!superResult) {
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancel && this.isEnabled()) {
            mDownX = event.getX();
            mDownY = event.getY();

            final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                    * mDownY);
            float targetRadius = Math.max(tempRadius, mMaxRadius);

            if (mIsAnimating) {
                mRadiusAnimator.cancel();
            }
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", dpToPix(40),
                    targetRadius);
            mRadiusAnimator.setDuration(500);
            mRadiusAnimator
                    .setInterpolator(new AccelerateDecelerateInterpolator());
            mRadiusAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    setRadius(0);
                    ViewHelper.setAlpha(RippleButton.this, 1);
                    mIsAnimating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mRadiusAnimator.start();
            if (!superResult) {
                return true;
            }
        }
        return superResult;
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public void setRadius(final float radius) {
        mRadius = radius;
        if (mRadius > 0) {
            mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius,
                    adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor,
                    Shader.TileMode.MIRROR);
            mPaint.setShader(mRadialGradient);
        }
        invalidate();
    }

    private Path mPath = new Path();

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        canvas.save(Canvas.CLIP_SAVE_FLAG);

        mPath.reset();
        mPath.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);

        canvas.clipPath(mPath);
        canvas.restore();

        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
    }

}