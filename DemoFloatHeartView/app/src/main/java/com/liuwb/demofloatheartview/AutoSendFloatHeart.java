package com.liuwb.demofloatheartview;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.liuwb.demofloatheartview.view.FloatHeartBubbleSurfaceView;

/**
 * Created by liuwb on 2016/12/8.
 */
public class AutoSendFloatHeart {

    private FloatHeartBubbleSurfaceView heartView;

    public static final int MAX_NUM = 3;//一次最多发10个
    public static final long MIN_DELAY_TIME = 400;//最小的时间间隔
    public static final long MAX_DELAY_TIME = 1000;//最大的时间间隔

    public static final float TIME_CHANGE_DUR = 100f;//从100人到3000人之间有变化了
    public static final int TIME_MAX_NUM = 10;//从100人的时候开始变化有变化了

    public static final float NUM_K = 0.01f;

    private HandlerThread workHandlerThread;

    private WorkHandler workHandler;

    private Object syncObject = new Object();

    private boolean isStarted;

    public AutoSendFloatHeart(FloatHeartBubbleSurfaceView heartView) {
        this.heartView = heartView;
        workHandlerThread = new HandlerThread("AutoSendFloatHeart,workHandlerThread");
        workHandlerThread.start();
        workHandler = new WorkHandler(heartView, workHandlerThread.getLooper());
    }

    /**
     * 以 x为值计算开始
     *
     * @param x
     */
    public void start(int x) {
        synchronized (syncObject) {
            isStarted = true;
            workHandler.setStart(getNum(x), getTime(x));
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    private int getNum(int x) {
        int num = (int) (NUM_K * x);
        num = Math.max(1, num);
        num = Math.min(num, MAX_NUM);
        return num;
    }

    private long getTime(int x) {
        float k = (MIN_DELAY_TIME - MAX_DELAY_TIME) / TIME_CHANGE_DUR;
        float b = MAX_DELAY_TIME - TIME_MAX_NUM * k;
        long t = (long) (k * x + b);
        t = Math.max(t, MIN_DELAY_TIME);
        t = Math.min(t, MAX_DELAY_TIME);
        return t;
    }


    public void reset(int x) {
        synchronized (syncObject) {
            workHandler.reset(getNum(x), getTime(x));
        }
    }

    public void stop() {
        synchronized (syncObject) {
            isStarted = false;
            workHandler.setStop();
        }
    }

    public void destory() {
        synchronized (syncObject) {
            isStarted = false;
            workHandler.removeCallbacksAndMessages(null);
            workHandlerThread.quit();
        }
    }

    static class WorkHandler extends Handler {

        private FloatHeartBubbleSurfaceView heartView;
        private static final int MSG_WHAT_SEND = 10;

        private int num;
        private long delayTime;

        private static int[] resArr = new int[]{
                R.drawable.flaot_heart, R.drawable.flaot_star,
                R.drawable.float_flower, R.drawable.float_bone,
                R.drawable.float_fish
        };

        public WorkHandler(FloatHeartBubbleSurfaceView heartView, Looper looper) {
            super(looper);
            this.heartView = heartView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_SEND) {
                int num = msg.arg1;
                for (int i = 0; i < num; i++) {
                    int res = getResIdRandom();
                    heartView.addAnHeart(res);
                }

                sendHeart();
            }
        }

        public void setStart(int num, long delayTime) {
            this.num = num;
            this.delayTime = delayTime;
            sendHeart();
        }

        public void reset(int num, long delayTime) {
            this.num = num;
            this.delayTime = delayTime;
        }

        private void sendHeart() {
            Message msg = obtainMessage(MSG_WHAT_SEND, num, 0);
            sendMessageDelayed(msg, delayTime);
        }

        public void setStop() {
            removeMessages(MSG_WHAT_SEND);
        }

        private int getResIdRandom() {
            int count = (int) (Math.random() * 5);
            return resArr[count];
        }
    }
}
