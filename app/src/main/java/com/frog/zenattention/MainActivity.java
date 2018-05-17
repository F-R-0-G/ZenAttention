package com.frog.zenattention;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frog.zenattention.utils.ActivityCollector;
import com.frog.zenattention.utils.AlarmClock;
import com.frog.zenattention.utils.ToastUtil;
import com.shawnlin.numberpicker.NumberPicker;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener{

    private Chronometer chronometer;
    private ProgressBar progressBar;
    private ProgressBar cancel_bar;
    private NumberPicker numberPicker;
    private Button stopButton;
    private Button cancelButton;
    private Button resumeButton;
    private Button startAttachAttention;
    private AlarmClock alarm_clock;
    private boolean isCancel = true;
    private ValueAnimator animator;

    Button start_music;
    Button pause_music;
    Button open_drawer;
    Button start_attention;

    ImageView background;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView quote1;
    TextView quote2;

    MediaPlayer mediaPlayer;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);

        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //定义全屏参数
        window.setFlags(flag, flag);
        //设置当前窗体为全屏显示

        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        start_music = (Button) findViewById(R.id.start_music);
        pause_music = (Button) findViewById(R.id.pause_music);
        open_drawer = (Button) findViewById(R.id.open_drawer);
        start_music.setOnClickListener(this);
        pause_music.setOnClickListener(this);
        open_drawer.setOnClickListener(this);

        start_music.setVisibility(View.VISIBLE);
        pause_music.setVisibility(View.INVISIBLE);

        initMediaPlayer();

        navigationView.setCheckedItem(R.id.nav_statistics);
        Resources resource=(Resources)getBaseContext().getResources();
        ColorStateList csl=(ColorStateList)resource.getColorStateList(R.color.navigation_menu_item_color);
        navigationView.setItemTextColor(csl);
        //设置颜色
        View headView = navigationView.getHeaderView(0);

        quote1=(TextView)headView.findViewById(R.id.quote_1);
        quote2=(TextView)headView.findViewById(R.id.quote_2);
        AssetManager mgr=getAssets();
        //根据路径得到Typeface
        Typeface tf=Typeface.createFromAsset(mgr, "fonts/Extralight.ttf");
        //设置字体
        quote1.setTypeface(tf);
        quote2.setTypeface(tf);
        //
        chronometer = findViewById(R.id.Clock_chronometer);
        chronometer.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.Clock_ProgressBar);
        cancel_bar = findViewById(R.id.cancel_progressBar);
        cancel_bar.setVisibility(View.INVISIBLE);
        // 创建计时器和进度条, 将cancel_bar设为不可见
        numberPicker = findViewById(R.id.number_picker);
        String[] displayNumber = new String[13];
        displayNumber[0] = "1:00";
        for (int i = 1; i < 13; i++){
            displayNumber[i] = Integer.toString(i*5) + ":" + "00";
        }
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(displayNumber.length);
        numberPicker.setDisplayedValues(displayNumber);
        numberPicker.setValue(6);
        // 创建时间选择器
        startAttachAttention = findViewById(R.id.start_attach_attention);
        startAttachAttention.setOnClickListener(this);
        //开始专注按钮
        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);
        stopButton.setVisibility(View.INVISIBLE);
        // 暂停按钮，设为不可见
        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnTouchListener(this);
        cancelButton.setVisibility(View.INVISIBLE);
        // 取消按钮，设为不可见
        resumeButton = findViewById(R.id.resume_button);
        resumeButton.setOnClickListener(this);
        resumeButton.setVisibility(View.INVISIBLE);
        //继续按钮，设为不可见
        alarm_clock = new AlarmClock(chronometer, progressBar,
                MainActivity.this, numberPicker);
        // 计时器实例
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(false);
                switch (item.getItemId()) {
                    case R.id.nav_statistics:
                        //统计
                        Intent intent = new Intent(MainActivity.this, checkStatistic.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_target:
                        //目标
                        break;
                    case R.id.nav_theme:
                        ToastUtil.showToast(MainActivity.this,"theme",Toast.LENGTH_SHORT);
                        //主题
                        break;
                    case R.id.nav_about:
                        //关于
                        break;
                    default:
                        break;
                }
                item.setChecked(false);
                return true;
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_music:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start(); // 开始播放
                }
                start_music.setVisibility(View.INVISIBLE);
                pause_music.setVisibility(View.VISIBLE);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        mediaPlayer.start();
                        mediaPlayer.setLooping(true);
                    }
                });
                break;
            case R.id.pause_music:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause(); // 暂停播放
                }
                start_music.setVisibility(View.VISIBLE);
                pause_music.setVisibility(View.INVISIBLE);
                break;
            case R.id.open_drawer:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.start_attach_attention:
                int num = numberPicker.getValue();
                alarm_clock.startCounting(num, startAttachAttention, stopButton);
                startAttachAttention.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                break;
            case R.id.resume_button:
                stopButton.setVisibility(View.VISIBLE);
                resumeButton.setVisibility(View.INVISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                alarm_clock.resumeAlarm();
                break;
            case R.id.stop_button:
                resumeButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.INVISIBLE);
                alarm_clock.pauseAlarm();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.cancel_button:
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    cancel_bar.setVisibility(View.VISIBLE);
                    animator = ValueAnimator.ofInt(0, 100);
                    animator.setDuration(2000);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int value = (int) animator.getAnimatedValue();
                            Log.e(TAG, Integer.toString(value));
                            cancel_bar.setProgress(value);
                            if (value == 100){
                                cancel_bar.setProgress(0);
                                if (!isCancel){
                                    isCancel = true;
                                    return;
                                }
                                resumeButton.setVisibility(View.INVISIBLE);
                                cancelButton.setVisibility(View.INVISIBLE);
                                alarm_clock.cancelAlarm();
                                ToastUtil.showToast(MainActivity.this, "计时器已被取消");
                            }
                        }
                    });
                    animator.start();
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    isCancel = false;
                    animator.end();
                    cancel_bar.setVisibility(View.INVISIBLE);
                }
            default:
                break;
        }
        return false;
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.heavy_rain);
            mediaPlayer.prepare(); // 让MediaPlayer进入到准备状态
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long mExitTime = 0;

    //计时器，虽然放在这里很丑，但放在实例区明显不合适，就凑合一下（可理解）
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 1000) {
                ToastUtil.showToast(this, "再按一次退出程序", Toast.LENGTH_SHORT);
                mExitTime = System.currentTimeMillis();
            } else {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //实现再按一次退出，退出时说骚话并以home形式存储

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}