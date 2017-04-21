package com.liuwb.demofloatheartview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.liuwb.demofloatheartview.view.FloatHeartBubbleSurfaceView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FloatHeartBubbleSurfaceView floatHeartBubbleSurfaceView;
    private Button btnStart;
    private Button btnSpeedAdd;
    private Button btnSpeedDown;

    private AutoSendFloatHeart autoSendFloatHeart;
    private int speedCount = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatHeartBubbleSurfaceView = (FloatHeartBubbleSurfaceView) findViewById(R.id.float_heart_view);
        btnStart = (Button) findViewById(R.id.start);
        btnSpeedAdd = (Button) findViewById(R.id.add_speed);
        btnSpeedDown = (Button) findViewById(R.id.down_speed);

        btnStart.setOnClickListener(this);
        btnSpeedAdd.setOnClickListener(this);
        btnSpeedDown.setOnClickListener(this);

        autoSendFloatHeart = new AutoSendFloatHeart(floatHeartBubbleSurfaceView);
    }

    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            if (!autoSendFloatHeart.isStarted()) {
                autoSendFloatHeart.start(speedCount);
            }
        } else if (v == btnSpeedAdd) {
            speedCount += 10;
            autoSendFloatHeart.reset(speedCount);
        } else if (v == btnSpeedDown) {
            speedCount -= 10;
            if (speedCount < 10) {
                speedCount = 10;
            }
            autoSendFloatHeart.reset(speedCount);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(autoSendFloatHeart != null) {
            autoSendFloatHeart.stop();
            autoSendFloatHeart.destory();
        }
    }
}
