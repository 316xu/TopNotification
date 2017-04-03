package cn.xujifa.topnotification;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by xujifa on 17-3-22.
 */

class SwipeLayout extends FrameLayout implements GestureDetector.OnGestureListener{

    private static final int SHOW_DURATION = 500;
    private static final int LEAST_SWIPE_SIZE = 15;

    private int mHeight = 0;
    private int mWidth = 0;
    private int mTop = 0;
    private int mChildTop = 0;

    private OnHiddenListener mOnHiddenListener;
    private GestureDetector mGestureDetector;
    private OnSwipeListener mOnSwipeListener;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        mHeight = bottom - top;
        mWidth = right - left;
        mTop = top;
        getChildAt(0).layout(left, mChildTop, right, mChildTop + mHeight);
    }

    public void show() {
        setVisibility(VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(SHOW_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                mChildTop = (int)(mTop - mHeight + value * mHeight);
                getChildAt(0).layout(0, mChildTop, mWidth, (int) (mTop + value * mHeight));
            }
        });
        valueAnimator.start();
    }

    public void hide() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(SHOW_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                getChildAt(0).layout(0, (int)(mTop - value * mHeight), mWidth, (int) (mTop + mHeight - value * mHeight));
                if (value == 1 && mOnHiddenListener != null) {
                    mOnHiddenListener.onHidden();
                }
            }
        });
        valueAnimator.start();
    }

    public void setmOnHiddenListener(OnHiddenListener mOnHiddenListener) {
        mOnHiddenListener = mOnHiddenListener;
    }

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        mOnSwipeListener = onSwipeListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (distanceY > LEAST_SWIPE_SIZE && mOnSwipeListener != null) {

            mOnSwipeListener.onSwipe();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public interface OnSwipeListener {
        void onSwipe();
    }

    public interface OnHiddenListener {
        void onHidden();
    }
}
