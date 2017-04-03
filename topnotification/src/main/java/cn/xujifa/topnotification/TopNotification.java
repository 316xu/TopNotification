package cn.xujifa.topnotification;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by jfxu on 17-3-21.
 */

public class TopNotification {

    public static final int[] ICONS = new int[] {R.drawable.information, R.drawable.warning, R.drawable.error, R.drawable.correct};
    public static final int[] PROGRESS_DRAWABLES = new int[] {R.drawable.progressbar_info, R.drawable.progressbar_warning, R.drawable.progressbar_error, R.drawable.progressbar_correct};

    @IntDef({INFO, WARN, ERROR, CORRECT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Level {

    }

    @IntDef({UNCREATED, CREATED, VISIBLE, INVISIBLE, DESTROYING, DESTROYED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {

    }

    public static final int INFO = 0;
    public static final int WARN = 1;
    public static final int ERROR = 2;
    public static final int CORRECT = 3;

    private static final int UNCREATED = 0;
    private static final int CREATED = 1;
    private static final int VISIBLE = 2;
    private static final int INVISIBLE = 3;
    private static final int DESTROYING = 4;
    private static final int DESTROYED = 5;

    private Context mContext;

    @State
    private int mState = UNCREATED;

    private SwipeLayout mRootLayout;
    private View mMaskView;
    private ProgressBar mProgressBar;

    private CountDownTimer mCountDownTimer;

    private String mMessage;
    @DrawableRes
    private int mIconRes;
    @ColorInt
    private int mColorInt;
    @Level
    private int mLevel;

    private long mDuration;
    private boolean mCancelable;
    private boolean mIsProgressBarVisible;
    private boolean mOutsideTouchable = true;

    private OnClickListener mOnPositiveClickListener;
    private OnClickListener mOnNegativeClickListener;
    private OnDismissListener mOnDismissListener;


    private TopNotification(Context context) {

        mContext = context;

        mRootLayout = (SwipeLayout) View.inflate(mContext, R.layout.notification, null);
        mRootLayout.setVisibility(View.GONE);
        mProgressBar = ((ProgressBar) mRootLayout.findViewById(R.id.progress));
    }

    public void setMessage(String message) {

        mMessage = message;
        if (mState < DESTROYING) {

            ((TextView) mRootLayout.findViewById(R.id.info)).setText(message);
        }
    }

    public void setMessage(@StringRes int textId) {

        setMessage(mContext.getString(textId));
    }

    public void setDuration(long duration) {

        mDuration = duration;
        determineProgressBarVisibility();
    }

    public void setCancelable(boolean cancelable) {

        mCancelable = cancelable;
    }

    public void setIconRes(@DrawableRes int iconRes) {

        mIconRes = iconRes;
        ((ImageView) mRootLayout.findViewById(R.id.icon)).setImageResource(iconRes);
    }

    public void setBackgroundColor(@ColorInt int color) {

        mColorInt = color;
        mRootLayout.getChildAt(0).setBackgroundColor(color);
    }

    public void setLevel(@Level int level) {

        mLevel = level;
        if (mState < DESTROYING) {

            setBackgroundColor(mContext.getResources().getIntArray(R.array.notification_background_color)[level]);
            ((ImageView) mRootLayout.findViewById(R.id.icon)).setImageResource(ICONS[level]);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                mProgressBar.setProgressDrawable(mContext.getDrawable(PROGRESS_DRAWABLES[level]));
            } else {

                mProgressBar.setProgressDrawable(mContext.getResources().getDrawable(PROGRESS_DRAWABLES[level]));
            }
        }
    }

    public void setIsProgressBarVisible(boolean isProgressBarVisible) {
        mIsProgressBarVisible = isProgressBarVisible;
        if (mState < DESTROYING) {

            determineProgressBarVisibility();
        }
    }

    public void setOutSideTouchable(boolean outsideTouchable) {

        mOutsideTouchable = outsideTouchable;
    }

    public void setOnPositiveClickListener(final OnClickListener onPositiveClickListener) {

        mOnPositiveClickListener = onPositiveClickListener;
        if (mState < DESTROYING && mOnPositiveClickListener != null) {

            mRootLayout.findViewById(R.id.button_panel).setVisibility(View.VISIBLE);
            mRootLayout.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mState >= DESTROYING) {
                        return;
                    }

                    onPositiveClickListener.onClick(getInstance());
                }
            });
        }
    }

    public void setOnNegativeClickListener(final OnClickListener onNegativeClickListener) {

        this.mOnNegativeClickListener = onNegativeClickListener;
        if (mState < DESTROYING && mOnNegativeClickListener != null) {
            mRootLayout.findViewById(R.id.button_panel).setVisibility(View.VISIBLE);
            mRootLayout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mState >= DESTROYING) {
                        return;
                    }

                    onNegativeClickListener.onClick(getInstance());
                }
            });
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {

        mOnDismissListener = onDismissListener;
    }


    public void create() {
        if (mState != UNCREATED) {

            return;
        }

        mState = CREATED;
        setBackKeyListener();
        setSwipeListener();
    }

    public void show() {
        if (mState >= DESTROYING) {

            return;
        }

        if (mState == UNCREATED) {

            create();
        }

        if (!mOutsideTouchable) {

            addMask();
        }

        if (mState == CREATED) {

            WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

            windowParams.gravity = Gravity.TOP | Gravity.LEFT;
            windowParams.x = 0;
            windowParams.y = 0;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
            windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowParams.format = PixelFormat.TRANSLUCENT;
            windowParams.windowAnimations = 0;
            windowManager.addView(mRootLayout, windowParams);
        }
        mState = VISIBLE;
        mRootLayout.setVisibility(View.VISIBLE);
        mRootLayout.show();

        startCountDown(mDuration);
    }

    public void hide() {

        if (mState != VISIBLE) {

            return;
        }
        if (mCountDownTimer != null) {

            mCountDownTimer.cancel();
        }

        mState = INVISIBLE;
        mRootLayout.hide();
        if (mMaskView != null) {

            mMaskView.setVisibility(View.GONE);
        }
    }

    public void dismiss() {

        if (mState >= DESTROYING) {

            return;
        }

        if (mCountDownTimer != null) {

            mCountDownTimer.cancel();
        }

        if (mOnDismissListener != null) {

            mOnDismissListener.onDismiss();
        }

        if (mState == INVISIBLE) {

            destroy();
            return;
        }

        mState = DESTROYING;

        mRootLayout.setmOnHiddenListener(new SwipeLayout.OnHiddenListener() {

            @Override
            public void onHidden() {

                destroy();
            }
        });

        if (mMaskView != null) {

            mMaskView.setVisibility(View.GONE);
        }
        mRootLayout.hide();
    }

    private void determineProgressBarVisibility() {

        if (mIsProgressBarVisible && mDuration > 0) {

            mProgressBar.setVisibility(View.VISIBLE);
        } else {

            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void startCountDown(final long duration) {

        if (duration <= 0) {

            return;
        }
        if (mCountDownTimer != null) {

            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(duration, duration / 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                mProgressBar.setProgress(100 - (int) ((float) (duration - millisUntilFinished) / duration * 100));
            }

            @Override
            public void onFinish() {

                dismiss();
            }
        };

        mCountDownTimer.start();
    }

    private void destroy() {

        mState = DESTROYED;
        if (mCountDownTimer != null) {

            mCountDownTimer.cancel();
        }
        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);

        if (mMaskView != null) {

            windowManager.removeViewImmediate(mMaskView);
        }
        windowManager.removeViewImmediate(mRootLayout);
    }

    private void addMask() {

        mMaskView = new View(mContext);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mMaskView.setLayoutParams(layoutParams);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            mMaskView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent_gray));
        } else {

            mMaskView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.transparent_gray, null));
        }

        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowParams.x = 0;
        windowParams.y = 0;
        windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
        windowManager.addView(mMaskView, windowParams);
        mMaskView.setVisibility(View.VISIBLE);
    }

    private void setBackKeyListener() {

        if (mContext instanceof Activity) {

            final Window window = ((Activity) mContext).getWindow();
            final Window.Callback callback = window.getCallback();

            ((Activity) mContext).getWindow().setCallback(new AbstractWindowCallback(callback) {

                @Override
                public boolean dispatchKeyEvent(KeyEvent event) {

                    if (!mOutsideTouchable && mState == VISIBLE) {

                        if (mCancelable) {

                            return true;
                        } else {

                            return false;
                        }
                    }

                    return callback.dispatchKeyEvent(event);
                }
            });
        }
    }

    private void setSwipeListener() {

        mRootLayout.setOnSwipeListener(new SwipeLayout.OnSwipeListener() {

            @Override
            public void onSwipe() {

                if (!mCancelable) {

                    return;
                }
                dismiss();
            }
        });
    }

    private TopNotification getInstance() {

        return this;
    }


    public static class Builder {

        private Context context;

        private String message;
        @DrawableRes
        private int iconRes;
        @ColorInt
        private int color = -1;
        @Level
        private int level;

        private long duration;
        private boolean cancelable = true;
        private boolean isProgressBarVisible;
        private boolean outsideTouchable = true;

        private OnClickListener onPositiveClickListener;
        private OnClickListener onNegativeClickListener;
        private OnDismissListener onDismissListener;


        public Builder(Context context) {

            this.context = context;
        }

        /**
         * Set message of notification
         *
         * @param message
         * @return
         */
        public Builder setMessage(String message) {

            this.message = message;
            return this;
        }

        /**
         * Set string res id of message
         *
         * @param textId
         * @return
         */
        public Builder setMessage(@StringRes int textId) {

            setMessage(context.getString(textId));
            return this;
        }

        /**
         * Set icon res of message
         *
         * @param iconRes
         * @return
         */
        public Builder setIconRes(@DrawableRes int iconRes) {

            this.iconRes = iconRes;
            return this;
        }

        /**
         * set background color
         *
         * @param color
         * @return
         */
        public Builder setColor(@ColorInt int color) {

            this.color = color;
            return this;
        }

        /**
         * Set notification level, include info, warning, error and correct
         *
         * @param level
         * @return
         * @see Level
         */
        public Builder setLevel(@Level int level) {

            this.level = level;
            return this;
        }

        /**
         * set duration
         *
         * @param duration
         * @return
         */
        public Builder setDuration(long duration) {

            this.duration = duration;
            return this;
        }

        /**
         * If set true, swipe gesture will be disabled
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {

            this.cancelable = cancelable;
            return this;
        }

        /**
         * Set visibility of progress bar
         *
         * @param isProgressBarVisible
         * @return
         */
        public Builder setIsProgressBarVisible(boolean isProgressBarVisible) {

            this.isProgressBarVisible = isProgressBarVisible;
            return this;
        }

        /**
         * If set true, notification will show like a normal dialog
         *
         * @param outsideTouchable
         * @return
         */
        public Builder setOutsideTouchable(boolean outsideTouchable) {

            this.outsideTouchable = outsideTouchable;
            return this;
        }

        /**
         * set onClickListener of ok button
         *
         * @param onClickListener
         * @return
         */
        public Builder setPositiveListener(OnClickListener onClickListener) {

            this.onPositiveClickListener = onClickListener;
            return this;
        }

        /**
         * set onClickListener of cancel button
         *
         * @param onClickListener
         * @return
         */
        public Builder setNegativeListener(OnClickListener onClickListener) {

            this.onNegativeClickListener = onClickListener;
            return this;
        }

        /**
         * set onDismissListener
         *
         * @param onDismissListener
         * @return
         */
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {

            this.onDismissListener = onDismissListener;
            return this;
        }

        public TopNotification create() {

            TopNotification topNotification = new TopNotification(context);

            topNotification.setMessage(message);
            topNotification.setLevel(level);
            topNotification.setOnPositiveClickListener(onPositiveClickListener);
            topNotification.setOnNegativeClickListener(onNegativeClickListener);
            topNotification.setDuration(duration);
            topNotification.setOnDismissListener(onDismissListener);
            topNotification.setCancelable(cancelable);
            topNotification.setOutSideTouchable(outsideTouchable);
            topNotification.setIsProgressBarVisible(isProgressBarVisible);
            if (iconRes > 0) {

                topNotification.setIconRes(iconRes);
            }
            if (color > -1) {

                topNotification.setBackgroundColor(color);
            }

            return topNotification;
        }
    }


    public interface OnDismissListener {

        void onDismiss();
    }

    public interface OnClickListener {

        void onClick(TopNotification topnotification);
    }
}
