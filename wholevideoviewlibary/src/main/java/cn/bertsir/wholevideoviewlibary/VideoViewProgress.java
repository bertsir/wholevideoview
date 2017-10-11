package cn.bertsir.wholevideoviewlibary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by Bert on 2017/5/27.
 */

public class VideoViewProgress extends View {


    private static final String TAG = VideoViewProgress.class.getSimpleName();
    private static final int BACKGROUND_COLOR = 0xFFFFFF;
    private static final int BORDER_COLOR = 0xFF0000;

    private int backgroundColor;
    private int progressColor;
    private boolean is_rect;

    private Paint bgPaint;
    private Paint pgPaint;

    private float progress = 0;

    private MyVideoView myVideoView;


    private VideoViewProgressListener listener;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    getProgress();
                    mHandler.sendEmptyMessage(1);
                    break;
            }
        }
    };

    public VideoViewProgress(Context context) {
        this(context, null);
    }

    public VideoViewProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoViewProgress);
        backgroundColor = ta.getColor(R.styleable.VideoViewProgress_vvp_background_color, BACKGROUND_COLOR);
        progressColor = ta.getColor(R.styleable.VideoViewProgress_vvp_progress_color, BORDER_COLOR);
        is_rect = ta.getBoolean(R.styleable.VideoViewProgress_vvp_rect, false);
        ta.recycle();
        init();
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setDither(true);
        bgPaint.setColor(backgroundColor);
        bgPaint.setStyle(Paint.Style.FILL);

        pgPaint = new Paint();
        pgPaint.setAntiAlias(true);
        pgPaint.setDither(true);
        pgPaint.setColor(progressColor);

        pgPaint.setStyle(Paint.Style.FILL);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(is_rect){
            canvas.drawRect(0f,0f,(float)width,(float)height,bgPaint);//背景
            canvas.drawRect(0f,0f,progress,(float)height,pgPaint);//进度
        }else {
            canvas.drawRoundRect(0f,0f,(float)width,(float)height,90,90,bgPaint);//背景
            canvas.drawRoundRect(0f,0f,progress,(float)height,90,90,pgPaint);//进度
        }

    }


    /**
     * 设置进度颜色
     * @param color
     */
    public void setProgressColor(int color){
        progressColor = color;
    }

    /**
     * 设置进度条背景色
     * @param color
     */
    public void setProgressBackgroudColor(int color){
        backgroundColor = color;
    }

    /**
     * 获得进度
     * @return
     */
    public void getProgress(){
        if(myVideoView != null){
            int duration = myVideoView.getDuration();
            int currentPosition = myVideoView.getCurrentPosition();
            int measuredWidth = getMeasuredWidth();
            progress = measuredWidth*(currentPosition / Float.valueOf(duration));
            if(listener != null){
                listener.onStartCount();
                listener.onProgress(currentPosition / Float.valueOf(duration));
                listener.onTime(currentPosition);
            }
            invalidate();
        }
    }


    /**
     * 设置播放控件（自行修改）
     * @param myVideoView
     */
    public void setVideoView(MyVideoView myVideoView){
        this.myVideoView = myVideoView;
        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(listener != null){
                    listener.onFinishCount();
                }
            }
        });
        mHandler.sendEmptyMessage(1);
    }

    /**
     * 释放进度条（防止内存泄漏）
     */
    public void realse(){
        mHandler.removeMessages(1);
    }



    /**
     * 设置监听
     * @param listener
     */
    public void setVideoViewProgress(VideoViewProgressListener listener) {
        this.listener = listener;
    }

    public interface VideoViewProgressListener {

        void onStartCount();

        void onFinishCount();

        void onProgress(float progress);

        void onTime(long time);
    }


}

