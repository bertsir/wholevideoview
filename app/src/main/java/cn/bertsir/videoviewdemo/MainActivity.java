package cn.bertsir.videoviewdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.bertsir.wholevideoviewlibary.WholeVideoView;

public class MainActivity extends AppCompatActivity {

    private WholeVideoView wvv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        wvv = (WholeVideoView) findViewById(R.id.wvv);
        wvv.setLooper(false);//设置是否循环
        wvv.setModle(WholeVideoView.MODE_CENTER);//设置视频模式  拉伸或保持比例居中
        wvv.setFullScreen(true);//是否启用全屏选项
        wvv.setSeek(true);//是否启用拖动条
        wvv.setProgressBackgroudColor(Color.parseColor("#cf000000"));//设置进度条背景色
        wvv.setProgressColor(Color.parseColor("#3ccd88"));//设置进度条颜色
        wvv.setVideoPath("http://orewrc0vz.bkt.clouddn.com/testMovie.mp4");
    }
}
