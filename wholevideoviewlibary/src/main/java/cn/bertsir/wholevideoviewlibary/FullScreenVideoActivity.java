package cn.bertsir.wholevideoviewlibary;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class FullScreenVideoActivity extends Activity {

    private String video_path;
    private int progress;
    private MyVideoView vv_full_screen;
    private ImageView iv_full_screen;
    private boolean is_pre = false;
    private Button bt_video_full;
    private TextView tv_video_all;
    private TextView tv_video_current;
    private VideoViewProgress vp_video_full;
    private LinearLayout ll_video_full_control;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    tv_video_current.setText(VideoUtils.getInstance().milliseconds2hour(vv_full_screen.getCurrentPosition()));
                    mHandler.sendEmptyMessage(1);
                    break;
            }
        }
    };
    private boolean screenOr;
    private int progresscolors;
    private int progressBacnColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_full_screen_video);
        initVariables();
        initView();
    }


    public void initVariables() {
        video_path = getIntent().getStringExtra("video_path");
        progress = getIntent().getIntExtra("progress", 0);
        screenOr = getIntent().getBooleanExtra("screen", true);
        progresscolors = getIntent().getIntExtra("progresscolor", Color.parseColor("#3ccd88"));
        progressBacnColor = getIntent().getIntExtra("progressBacnColor", Color.parseColor("#cf000000"));
        if(screenOr){
            //竖屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
        }else {
            //横屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        }
    }


    public void initView() {

        vv_full_screen = (MyVideoView) findViewById(R.id.vv_full_screen);
        View v_click = findViewById(R.id.v_click);
        bt_video_full = (Button) findViewById(R.id.bt_video_full);
        tv_video_all = (TextView) findViewById(R.id.tv_video_all);
        tv_video_current = (TextView) findViewById(R.id.tv_video_current);
        vp_video_full = (VideoViewProgress) findViewById(R.id.vp_video_full);
        ll_video_full_control = (LinearLayout) findViewById(R.id.ll_video_full_control);
        ll_video_full_control.setVisibility(View.GONE);

        iv_full_screen = (ImageView) findViewById(R.id.iv_full_screen);

        vp_video_full.setProgressColor(progresscolors);
        vp_video_full.setProgressBackgroudColor(progressBacnColor);
        iv_full_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent videoPath = new Intent().putExtra("progress", vv_full_screen.getCurrentPosition());
//                setResult(RESULT_OK, videoPath);
                finish();
            }
        });
        Toast.makeText(getApplicationContext(),"请稍后...",Toast.LENGTH_SHORT).show();
        playVideo();

        v_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ll_video_full_control.getVisibility() == View.GONE){
                    ll_video_full_control.setVisibility(View.VISIBLE);
                    ll_video_full_control.setAnimation(AnimationUtil.moveToViewLocation());
                }else {
                    ll_video_full_control.setVisibility(View.GONE);
                    ll_video_full_control.setAnimation(AnimationUtil.moveToViewBottom());
                }
            }
        });

        bt_video_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vv_full_screen.isPlaying()){
                    vv_full_screen.pause();
                    bt_video_full.setBackgroundResource(R.drawable.video_bottom_play);
                }else {
                    vv_full_screen.start();
                    bt_video_full.setBackgroundResource(R.drawable.video_bottom_pause);
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vv_full_screen.pause();
        vv_full_screen.release();
        vp_video_full.realse();
        mHandler.removeMessages(1);
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        vv_full_screen.setVideoPath(video_path);
        vv_full_screen.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoReady();
                vp_video_full.setVideoView(vv_full_screen);
            }
        });


        vv_full_screen.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                Intent videoPath = new Intent().putExtra("progress", vv_full_screen.getCurrentPosition());
//                setResult(RESULT_OK, videoPath);
                finish();
            }
        });

    }

    private void videoReady(){
        vv_full_screen.setLooping(false);
        vv_full_screen.seekTo(progress);
        is_pre = true;
        vv_full_screen.start();
        bt_video_full.setBackgroundResource(R.drawable.video_bottom_pause);
        tv_video_all.setText(VideoUtils.getInstance().milliseconds2hour(vv_full_screen.getDuration()));
        mHandler.sendEmptyMessage(1);
        final ViewTreeObserver viewTreeObserver = vv_full_screen.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {


            @Override
            public boolean onPreDraw() {
                //获得视频播放控件宽高
                int measuredHeight = vv_full_screen.getMeasuredHeight();
                //获得视频宽高
                int videoWidth = vv_full_screen.getVideoWidth();
                int videoHeight = vv_full_screen.getVideoHeight();
                //计算视频高于控件高的比例
                float heightRatio = (float) measuredHeight / videoHeight;
                //计算真实宽
                int resultWidth = (int) (videoWidth * heightRatio);
                //设置视频播放控件宽
                FrameLayout.LayoutParams vv_edit_video_layoutParams = (FrameLayout.LayoutParams) vv_full_screen.getLayoutParams();
                vv_edit_video_layoutParams.width = resultWidth;
                vv_full_screen.setLayoutParams(vv_edit_video_layoutParams);
                viewTreeObserver.removeOnPreDrawListener(this);
                return true;
            }
        });


    }


}
