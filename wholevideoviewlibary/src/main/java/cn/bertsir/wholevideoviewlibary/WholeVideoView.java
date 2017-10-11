package cn.bertsir.wholevideoviewlibary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

/**
 * Created by Bert on 2017/10/10.
 */


/**
 * 如果启用全屏  请在使用WholeVideoview的Activity的manifests加 android:configChanges="orientation|screenSize"
 */

public class WholeVideoView extends FrameLayout implements View.OnClickListener {

    private MyVideoView vv;
    private Button bt_play_video;
    private TextView tv_video_current_video;
    private VideoViewProgress vp_video;
    private TextView tv_video_all_video;
    private ImageView iv_full_video;
    private LinearLayout ll_control_video;
    private FrameLayout fl_vv_click;
    private SeekBar sb_video;

    //视频相关设置
    public static final int MODE_STRETCH = 1;//拉伸（可能会变形全屏）
    public static final int MODE_CENTER = 2;//居中（保持比例）
    public int CURRENT_MODE = MODE_CENTER;
    public boolean is_looper = false;

    private boolean is_pre = false;
    private boolean is_Auto_play = false;

    private Context mContext;
    private String video_path;

    private int ProgressColor = Color.parseColor("#3ccd88");
    private int ProgressBackGroundColor = Color.parseColor("#cf000000");


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    ll_control_video.setVisibility(GONE);
                    ll_control_video.setAnimation(AnimationUtil.moveToViewBottom());
                    break;
            }
        }
    };



    public WholeVideoView(@NonNull Context context) {
        super(context);
    }

    public WholeVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView(context);
    }

    private void initView(Context mContext) {
        View rootView = View.inflate(mContext, R.layout.view_whole_video, this);
        vv = (MyVideoView) rootView.findViewById(R.id.vv);
        bt_play_video = (Button) rootView.findViewById(R.id.bt_play_video);
        tv_video_all_video = (TextView) rootView.findViewById(R.id.tv_video_all_video);
        tv_video_current_video = (TextView) rootView.findViewById(R.id.tv_video_current_video);
        vp_video = (VideoViewProgress) rootView.findViewById(R.id.vp_video);
        iv_full_video = (ImageView) rootView.findViewById(R.id.iv_full_video);
        ll_control_video = (LinearLayout) rootView.findViewById(R.id.ll_control_video);
        fl_vv_click = (FrameLayout) rootView.findViewById(R.id.fl_vv_click);
        sb_video = (SeekBar) rootView.findViewById(R.id.sb_video);

        bt_play_video.setOnClickListener(this);
        fl_vv_click.setOnClickListener(this);
        iv_full_video.setOnClickListener(this);

        vp_video.setVideoViewProgress(new VideoViewProgress.VideoViewProgressListener() {
            @Override
            public void onStartCount() {
            }

            @Override
            public void onFinishCount() {
            }

            @Override
            public void onProgress(float progress) {
                sb_video.setProgress((int) (progress*100));
            }

            @Override
            public void onTime(long time) {
                tv_video_current_video.setText(VideoUtils.getInstance().milliseconds2hour((int) time));
            }
        });
    }

    /**
     * 设置视频地址（可以是本地或者网络,最后调用）
     * @param url
     */
    public void setVideoPath(String url){
        vv.setVideoPath(url);
        video_path = url;
        prepareVideo();
    }

    /**
     * 拉升还是平铺(需要在设置地址前调用)
     * @param modle
     */
    public void setModle(int modle){
        CURRENT_MODE = modle;
    }

    /**
     * 设置循环模式
     * @param looper
     */
    public void setLooper(boolean looper){
        is_looper = looper;
    }


    /**
     * 是否启用全屏
     * @param is_Full
     */
    public void setFullScreen(boolean is_Full){
        if(!is_Full){
            iv_full_video.setVisibility(GONE);
        }
    }

    /**
     * 释放资源防止内存泄漏
     */
    public void release(){
        vv.release();
        vp_video.realse();
        mHandler.removeMessages(1);
    }

    /**
     * 准备视频
     */
    private void prepareVideo(){
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //todo progressbar or seekbar
                vp_video.setVideoView(vv);
                vv.setLooping(is_looper);
                is_pre = true;
                if(is_Auto_play){
                    vv.start();
                    is_Auto_play = false;
                }
                tv_video_all_video.setText(VideoUtils.getInstance().milliseconds2hour(vv.getDuration()));
                tv_video_current_video.setText("00:00");

                if(CURRENT_MODE == MODE_STRETCH){
                    //拉伸不处理
                }else {
                    //处理居中
                    final ViewTreeObserver viewTreeObserver = vv.getViewTreeObserver();
                    viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                        @Override
                        public boolean onPreDraw() {
                            //获得视频播放控件宽高
                            int measuredHeight = vv.getMeasuredHeight();
                            int measuredWidth = vv.getMeasuredWidth();
                            //获得视频宽高
                            int videoWidth = vv.getVideoWidth();
                            int videoHeight = vv.getVideoHeight();
                            if(videoHeight > measuredHeight || videoWidth > measuredWidth){
                                float ratio = 0.f;
                                //计算高的比例
                                float heightRatio = (float) measuredHeight / videoHeight;
                                //计算宽的比例
                                float widthRatio = (float)measuredWidth / videoWidth;
                                //获取最小的
                                ratio = Math.min(heightRatio,widthRatio);
                                //计算真实宽
                                int resultWidth = (int) (videoWidth * ratio);
                                int resultHeight = (int)(videoHeight * ratio);
                                //设置视频播放控件宽
                                FrameLayout.LayoutParams vv_edit_video_layoutParams = (FrameLayout.LayoutParams) vv
                                        .getLayoutParams();
                                vv_edit_video_layoutParams.width = resultWidth;
                                vv_edit_video_layoutParams.height = resultHeight;
                                vv.setLayoutParams(vv_edit_video_layoutParams);
                            }
                            viewTreeObserver.removeOnPreDrawListener(this);
                            return true;
                        }
                    });
                }

            }
        });
        vv.setOnPlayStateListener(new MyVideoView.OnPlayStateListener() {
            @Override
            public void onStateChanged(boolean isPlaying) {
                if (isPlaying) {
                    bt_play_video.setBackgroundResource(R.drawable.video_bottom_pause);
                    mHandler.sendEmptyMessageDelayed(1,2000);
                }else {
                    bt_play_video.setBackgroundResource(R.drawable.video_bottom_play);
                }
            }
        });

    }

    /**
     * 播放视频
     */
    private void playVideo(){
        if(vv.isPlaying()){
            vv.pause();
        }else {
            if (is_pre) {
                vv.start();
            }else {
                Toast.makeText(getContext(),"缓冲中，请稍后！",Toast.LENGTH_SHORT).show();
                is_Auto_play = true;
            }
        }

        if(!is_looper){
            vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    vv.seekTo(0);
                }
            });
        }

    }

    /**
     * 设置是否启用滑动
     */
    public void setSeek(boolean seek){
        if(seek){
            vp_video.setVisibility(INVISIBLE);
            seek();
        }else {
            vp_video.setVisibility(VISIBLE);
            sb_video.setVisibility(GONE);
        }
    }


    /**
     * 修改SeekBar样式  进度条颜色po_seekbar   滑块替换seekbar_thumb
     */

    /**
     * 调整进度
     */
    private void seek(){
        sb_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(1);
                vv.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                BigDecimal divide = new BigDecimal(progress).divide(new BigDecimal(100));
                double v1 = divide.doubleValue();
                double v = vv.getDuration() * v1;
                vv.seekTo((int) v);
                vv.start();
                mHandler.sendEmptyMessageDelayed(1,2000);

            }
        });
    }




    /**
     * 获取视频当前播放时间
     * @return
     */
    public int getVideoCurren(){
        return  vv.getCurrentPosition();
    }


    /**
     * 设置进度条颜色
     * @param color
     */
    public void setProgressColor(int color){
        ProgressColor = color;
        vp_video.setProgressColor(color);
    }


    /**
     * 设置进度条背景色
     * @param color
     */
    public void setProgressBackgroudColor(int color){
        ProgressBackGroundColor = color;
        vp_video.setProgressBackgroudColor(color);
    }


    @Override
    public void onClick(View v) {
      if(v.getId() == R.id.bt_play_video){
          playVideo();
      }else if(v.getId() == R.id.fl_vv_click){
          mHandler.removeMessages(1);
          if(ll_control_video.getVisibility() == VISIBLE){
              ll_control_video.setVisibility(GONE);
              ll_control_video.setAnimation(AnimationUtil.moveToViewBottom());
          }else {
              ll_control_video.setAnimation(AnimationUtil.moveToViewLocation());
              ll_control_video.setVisibility(VISIBLE);
              mHandler.sendEmptyMessageDelayed(1,2000);
          }
      }else if(v.getId() == R.id.iv_full_video){
          vv.pause();
          boolean screen = vv.getVideoHeight() > vv.getVideoWidth();
            mContext.startActivity(new Intent(mContext,FullScreenVideoActivity.class).putExtra("video_path",
                    video_path).putExtra("progress",vv.getCurrentPosition()).putExtra("screen",screen).putExtra
                    ("progresscolor",ProgressColor).putExtra("progressBacnColor",ProgressBackGroundColor));
      }
    }

}
