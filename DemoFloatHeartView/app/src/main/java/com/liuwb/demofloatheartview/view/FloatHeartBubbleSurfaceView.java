package com.liuwb.demofloatheartview.view;

import android.content.Context;
import android.graphics.*;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import com.liuwb.demofloatheartview.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * 采用surfaceView。防止大数据影响
 * Created by liuwb on 2016/7/18.
 */
public class FloatHeartBubbleSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "Heart";
    private Context context;

    public int BKG_COLORS = R.color.white;
    public int HEART_RESOURCE = R.drawable.icon_heart;
    /**
     * 心形背景之间的间距
     */
    public int DXDY = 4;

    /**
     * 控制向上移动的速度
     */
    private int speedY = 5;

    /**
     * 控制缩放总进度
     */
    private int scaleSize = 7;

    /**
     * 刷新的显示的时间间隔
     */
    private long timeGap = 16L;

    /**
     * 绘制的数据集
     */
    private LinkedList<FloatHeart> heartList = new LinkedList<FloatHeart>();
    /**
     * 准备绘制的数据集，起暂存作用， 当开启绘制的时候，把这个集合里面的数据全部转移到绘制集里面
     */
    private LinkedList<FloatHeart> prepareHeartList = new LinkedList<FloatHeart>();

    private int screenHeight;
    private int screenWidth;


    /**
     * 标记当前View是否初始化完成了（包括大小分配等情况）
     */
    private boolean isPrepared;
    /**
     * 判断是否是初始化显示
     */
    private boolean isInited = false;
    private SurfaceHolder holder;
    private FloatHeartThread floatHeartThread;

    public FloatHeartBubbleSurfaceView(Context context) {
        this(context, null);
    }

    public FloatHeartBubbleSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatHeartBubbleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        holder = this.getHolder();
        holder.addCallback(this);

        setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        setBackgroundColor(Color.TRANSPARENT);
        this.context = context;

        ViewTreeObserver observer = this.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (!isInited) {
                    screenWidth = getMeasuredWidth();
                    screenHeight = getMeasuredHeight();
                    isInited = true;
                    isPrepared = true;
                    prepareDrawLogic();
                }
                return true;
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null == floatHeartThread) {
            floatHeartThread = new FloatHeartThread(holder);
            floatHeartThread.isRun = true;
            floatHeartThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        floatHeartThread.isRun = false;
        floatHeartThread = null;
    }

    public void addAnHeart(int resId) {
        if (isPrepared) {
            heartList.add(createHeart(resId));
        } else {
            prepareHeartList.add(createHeart(resId));
        }
    }

    public void addSomeHeart(int num, int resId) {
        ArrayList<FloatHeart> temp = new ArrayList<FloatHeart>();
        for (int i = 0; i < num; i++) {
            temp.add(createHeart(resId));
        }
        addHeartList(temp);
    }

    public void addHeartList(ArrayList<FloatHeart> hearts) {
        if (isPrepared) {
            heartList.addAll(hearts);
        } else {
            prepareHeartList.addAll(hearts);
        }
    }

    public void clear() {
        heartList.clear();
        prepareHeartList.clear();
    }

    private void prepareDrawLogic() {
        if (prepareHeartList.size() > 0 && isPrepared) {
            boolean isAddOk = heartList.addAll(prepareHeartList);
            prepareHeartList.clear();
        }
    }

    private void drawHeart(Canvas c) {
        for (int i = 0; i < heartList.size(); i++) {
            FloatHeart h = heartList.get(i);
            Bitmap contentBitmap = getScaleBitmap(h.bitmap, h.scale);
            c.drawBitmap(contentBitmap,
                    h.point.x - contentBitmap.getWidth() / 2,
                    h.point.y - contentBitmap.getHeight(),
                    h.paint);
            //            contentBitmap.recycle();
        }
    }

    private void drawOverLogic() {
        for (int i = 0; i < heartList.size(); i++) {
            FloatHeart h = heartList.get(i);
            h.scale += 1;
            if (h.scale >= scaleSize) {
                h.scale = scaleSize;
            }
            h.point.x += h.xSpeed;
            if (h.point.x - h.bitmap.getWidth() / 2 <= 1) {
                h.point.x = 1 + h.bitmap.getWidth() / 2;
                h.xSpeed = -h.xSpeed;
            }
            if (h.point.x + h.bitmap.getWidth() / 2 >= screenWidth - 1) {
                h.point.x = screenWidth - 1 - h.bitmap.getWidth() / 2;
                h.xSpeed = -h.xSpeed;
            }
            h.point.y -= 5;
            if (h.scale == scaleSize) {
                h.point.y -= speedY;
                h.alpha = 1f / (screenHeight) * h.point.y;
                h.paint = setBitmapPaint(h.paint, h.alpha);
            }

            if (h.point.x <= 0 || h.point.y <= 0 || h.alpha <= 0) {
                h.bitmap.recycle();
                heartList.remove(i);
            }

        }
    }

    private int getXSpeed() {
        Random random = new Random();
        int speedX = random.nextInt(5);
        int symbolX = random.nextInt(2) >= 1 ? 1 : -1;
        return speedX * symbolX;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        screenWidth = getWidth();
        screenHeight = getHeight();
        isPrepared = true;
        prepareDrawLogic();
    }

    protected Bitmap getScaleBitmap(Bitmap bitmap, int percent) {
        if (bitmap != null && !bitmap.isRecycled()) {
            float scalePercent = (float) percent / scaleSize;
            int w = (int) (bitmap.getWidth() * scalePercent);
            int h = (int) (bitmap.getHeight() * scalePercent);
            return Bitmap.createScaledBitmap(bitmap, w, h, false);
        }
        return bitmap;
    }

    protected Bitmap getBitmap(int resId) {
        String typeNmae = getResources().getResourceTypeName(resId);
        if (!TextUtils.isEmpty(typeNmae) &&
                typeNmae.equalsIgnoreCase("drawable")) {
            return getDrawableBitmap(resId);
        } else {
            return getColorBitmap(resId);
        }
    }

    protected Bitmap getDrawableBitmap(int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                resId);
        return bitmap;
    }

    /**
     * 绘制带有白色环状的图片
     *
     * @param color 中间心形的颜色
     * @return
     */
    protected Bitmap getColorBitmap(int color) {
        int dxdy = DXDY;
        Bitmap sourceBitmap = BitmapFactory.decodeResource(getResources(),
                HEART_RESOURCE);
        int w = sourceBitmap.getWidth();
        int h = sourceBitmap.getHeight();
        int bkgW = w + 2 * dxdy;
        int bkgH = h + 2 * dxdy;

        Bitmap resultMap = Bitmap.createBitmap(bkgW, bkgH, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(resultMap);
        Rect bkgRect = new Rect(0, 0, bkgW, bkgH);
        Rect heartRect = new Rect(dxdy, dxdy, bkgW - dxdy, bkgH - dxdy);

        //draw background heart
        Paint bkgPaint = new Paint();
        bkgPaint.setAntiAlias(true);
        bkgPaint.setDither(true);
        ColorFilter filter = new PorterDuffColorFilter(getColor(BKG_COLORS), PorterDuff.Mode.SRC_IN);
        bkgPaint.setColorFilter(filter);
        c.drawBitmap(sourceBitmap, bkgRect, bkgRect, bkgPaint);

        //draw content heart
        Paint heartPaint = new Paint();
        heartPaint.setAntiAlias(true);
        heartPaint.setDither(true);
        ColorFilter hFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
        heartPaint.setColorFilter(hFilter);
        c.drawBitmap(sourceBitmap, bkgRect, heartRect, heartPaint);
        sourceBitmap.recycle();
        return resultMap;
    }

    private Paint setBitmapPaint(Paint paint, float alpha) {
        if (paint == null) {
            paint = new Paint();
        }
        int alph = (int) (255 * alpha);
        paint.setAlpha(alph);
        paint.setAntiAlias(true);
        paint.setDither(true);
        return paint;
    }

    private int getColor(int resId) {
        return getResources().getColor(resId);
    }

    private FloatHeart createHeart(int resId) {
        FloatHeart heart = new FloatHeart();
        Point p = new Point();
        p.x = screenWidth / 2;
        p.y = screenHeight;
        heart.point = p;
        heart.alpha = 1;
        heart.paint = setBitmapPaint(null, 1f);
        heart.bitmap = getBitmap(resId);
        heart.scale = 3;
        heart.xSpeed = getXSpeed();
        return heart;
    }

    class FloatHeart {
        Point point;
        Paint paint;
        int scale;
        float alpha;
        Bitmap bitmap;
        int xSpeed;
    }

    class FloatHeartThread extends Thread {
        private SurfaceHolder holder;
        public boolean isRun;

        public FloatHeartThread(SurfaceHolder holder) {
            this.holder = holder;
            isRun = true;
        }

        @Override
        public void run() {
            super.run();
            while (isRun) {
                Canvas c = null;
                try {
                    synchronized (holder) {
                        c = holder.lockCanvas();//锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了。
                        if (c != null) {
                            c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//设置画布背景颜色

                            drawHeart(c);
                            drawOverLogic();
                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);//结束锁定画图，并提交改变。
                    }
                }

                try {
                    Thread.sleep(timeGap);//睡眠时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
