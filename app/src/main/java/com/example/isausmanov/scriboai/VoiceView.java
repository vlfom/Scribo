package com.example.isausmanov.scriboai;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/** credit goes to:
 *  From https://github.com/kyze8439690/AndroidVoiceAnimation
 */
public class VoiceView extends View {

    private static final String TAG = VoiceView.class.getName();

    private static final int STATE_NORMAL = 0;
    private static final int STATE_PRESSED = 1;
    private static final int STATE_RECORDING = 2;

    private Bitmap mNormalBitmap;
    private Bitmap mPressedBitmap;
    private Bitmap mRecordingBitmap;
    private Paint mPaint;
    private AnimatorSet mAnimatorSet = new AnimatorSet();
    private OnIClickedListener mOnClickListener;

    private int mState = STATE_NORMAL;
    private boolean mIsRecording = false;
    private float mMinRadius;
    private float mMaxRadius;
    private float mCurrentRadius;

    public VoiceView(Context context) {
        super(context);
        init();
    }

    public VoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void init(){
        Drawable ic_mic_nrm = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_btn_wrk, null);
        Drawable ic_mic_prsd = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_btn_wrk_prs, null);
        Drawable ic_mic_on = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_btn_wrk_rec, null);

        //mRecordingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_microphone);
        mNormalBitmap = drawableToBitmap(ic_mic_nrm);
        mPressedBitmap = drawableToBitmap(ic_mic_prsd);
        mRecordingBitmap = drawableToBitmap(ic_mic_on);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.argb(255, 19, 165, 255));

        mMinRadius = ScreenUtils.dp2px(getContext(), 68) / 2;
        mCurrentRadius = mMinRadius;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = Math.min(w, h) / 2;
        Log.d(TAG, "MaxRadius: " + mMaxRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if(mCurrentRadius > mMinRadius){
            canvas.drawCircle(width / 2, height / 2, mCurrentRadius, mPaint);
        }

        switch (mState){
            case STATE_NORMAL:
                canvas.drawBitmap(mNormalBitmap, width / 2 - mMinRadius * 2,  height / 2 - mMinRadius * 2, mPaint);
                break;
            case STATE_PRESSED:
                canvas.drawBitmap(mPressedBitmap, width / 2 - mMinRadius * 2,  height / 2 - mMinRadius * 2, mPaint);
                break;
            case STATE_RECORDING:
                canvas.drawBitmap(mRecordingBitmap, width / 2 - mMinRadius * 2,  height / 2 - mMinRadius * 2, mPaint);
                break;
        }
    }

    public void animateRadius(float radius){
        if(radius <= mCurrentRadius){
            return;
        }
        if(radius > mMaxRadius){
            radius = mMaxRadius;
        }else if(radius < mMinRadius){
            radius = mMinRadius;
        }
        if(radius == mCurrentRadius){
            return;
        }
        if(mAnimatorSet.isRunning()){
            mAnimatorSet.cancel();
        }
        mAnimatorSet.playSequentially(
                ObjectAnimator.ofFloat(this, "CurrentRadius", getCurrentRadius(), radius).setDuration(50),
                ObjectAnimator.ofFloat(this, "CurrentRadius", radius, mMinRadius).setDuration(600)
        );
        mAnimatorSet.start();
    }

    public float getCurrentRadius() {
        return mCurrentRadius;
    }

    public void setCurrentRadius(float currentRadius) {
        mCurrentRadius = currentRadius;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Do the main recoding in MainActivity
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN");
                mState = STATE_PRESSED;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                mOnClickListener.onHandleRecording();
                Log.d(TAG, "ACTION_UP");
                if(mIsRecording){
                    mState = STATE_NORMAL;
                    if(mOnClickListener != null){
                        mOnClickListener.onAnimationFinish();
                    }
                }else{
                    mState = STATE_RECORDING;
                    if(mOnClickListener != null){
                        mOnClickListener.onAnimationStart();
                    }
                }
                mIsRecording = !mIsRecording;
                invalidate();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void setOnClickListener(OnIClickedListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public static interface OnIClickedListener {
        public void onHandleRecording();
        public void onAnimationStart();
        public void onAnimationFinish();
    }
}