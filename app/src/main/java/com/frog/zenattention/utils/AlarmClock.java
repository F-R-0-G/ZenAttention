package com.frog.zenattention.utils;

import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;

import com.frog.zenattention.MainActivity;
import com.shawnlin.numberpicker.NumberPicker;

public class AlarmClock {
    private Chronometer chronometer;
    private ProgressBar progressBar;
    private NumberPicker numberPicker;
    private ValueAnimator valueAnimator;
    private static final int MAX_PROGRESS = 10000;
    private long selectTime;
    private Context context;
    private long pauseTime;
    private boolean isCancel = false;

    private static final String TAG = "Alarm_Clock";

    public AlarmClock (Chronometer chronometer, ProgressBar progressBar, Context context,
                        NumberPicker numberPicker){
        this.chronometer = chronometer;
        this.progressBar = progressBar;
        this.context = context;
        this.numberPicker = numberPicker;
    }


    public long startCounting(final int numberChoosed, Button startButton, Button stopButton){
        numberPicker.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.VISIBLE);
        if (numberChoosed == 1) selectTime = 60 * 1000;
        else selectTime = (numberChoosed - 1) * 5 * 60 * 1000;
        ToastUtil.showToast(context, "设置成功");

        chronometer.setBase(SystemClock.elapsedRealtime() + selectTime);  // 设置倒计时
        chronometer.setCountDown(true);
        chronometer.start();
        initProgressBar(selectTime, startButton, stopButton);

        return selectTime;
    }

    // 初始化一个ProgressBar，并在结束时停止chronometer
    private void initProgressBar(long duration, final Button startButton,
                                 final Button stopButton) {
        progressBar.setMax(MAX_PROGRESS);
        progressBar.setProgress(0);

        valueAnimator = ValueAnimator.ofInt(0, MAX_PROGRESS);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) valueAnimator.getAnimatedValue();
                progressBar.setProgress(value);
                if (value == MAX_PROGRESS) {
                    chronometer.stop();
                    progressBar.setProgress(0);
                    chronometer.setVisibility(View.INVISIBLE);
                    numberPicker.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.VISIBLE);
                    stopButton.setVisibility(View.INVISIBLE);
                    if (isCancel){
                        isCancel = false;
                        return;
                    }
                    AttentionTimeData.storeTime(selectTime, context);      // 存储时间数据
                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationUtils notification = new NotificationUtils(context);
                    notification.sendNotification("时间","预设时间到", pi);
                }
            }
        });
        valueAnimator.start();
    }

    public void pauseAlarm(){
        valueAnimator.pause();
        chronometer.stop();
        pauseTime = SystemClock.elapsedRealtime();
    }

    public void resumeAlarm(){
        if (pauseTime != 0){
            chronometer.setBase(chronometer.getBase() + (SystemClock.elapsedRealtime() - pauseTime));
        }
        else {
            chronometer.setBase(SystemClock.elapsedRealtime());
        }
        valueAnimator.resume();
        chronometer.start();
    }

    public void cancelAlarm(){
        isCancel = true;
        valueAnimator.end();
    }
}
