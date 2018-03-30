package bpk.light.vkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class Game extends AppCompatActivity {

    Bitmap bitmap, bitmap1, pointVoice;
    Rect rectSrc;
    Rect rectDst;
    int x1, x2, disW, disH, pointX, pointY, pointYRT = 0, volMax, k = 40;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Handler uiHandler = new Handler();
    Timer myTimer;
    SoundMeter sm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView(this));
        myTimer = new Timer();
        sm = new SoundMeter();
    }

    class DrawView extends SurfaceView implements SurfaceHolder.Callback {
        private DrawThread drawThread;

        public DrawView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            disW = size.x;
            disH = size.y;
            pointX = disW / 2;
            pointY = disH - 15;
            bitmap1 = Bitmap.createBitmap(disW, disH, Bitmap.Config.ARGB_8888);
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.song1);
            pointVoice = BitmapFactory.decodeResource(getResources(), R.drawable.point);
            //k = disH / bitmap.getHeight();
            x1 = 0;
            x2 = bitmap.getWidth() / 15;
            Log.d(getString(R.string.LL), "Dispaly res " + disW + " x " + disH);
            Log.d(getString(R.string.LL), "bitmap res " + bitmap.getWidth() + " x " + bitmap.getHeight());
            drawThread = new DrawThread(getHolder());
            drawThread.setRunning(true);
            drawThread.start();
            startAnaliz();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                pointX = ((int) event.getX());
                pointY = ((int) event.getY());
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {

            }
            return true;
        }
    }
    public static Bitmap getBitmap(Canvas canvas) {
        // mBitmap is a private value inside Canvas.
        // time for some dirty reflection:
        try {
            java.lang.reflect.Field field = Canvas.class.getDeclaredField("mBitmap");
            field.setAccessible(true);
            Log.d("LightLog", " get Bitmap true");
            return (Bitmap) field.get(canvas);
        } catch (Throwable t) {
            Log.d("LightLog", "get Bitmap false");
            return null;
        }
    }

    private void startAnaliz() {
        sm.start();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int result = sm.getAmplitude();
                //sm.stop();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        volMax = result;
                    }
                });
            }
        }, 0L, 1L * 100);
    }

    public class SoundMeter {

        private MediaRecorder mRecorder = null;

        public void start() {
            if (mRecorder == null) {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");
                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRecorder.start();
            }
        }

        public void stop() {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            }
        }

        public int getAmplitude() {
            if (mRecorder != null)
                return Math.round(mRecorder.getMaxAmplitude());
            else
                return 0;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        myTimer.cancel();
        sm.stop();
    }

    class DrawThread extends Thread {

        private boolean running = false;
        private SurfaceHolder surfaceHolder;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas;
            Canvas canvas1;

            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    //canvas1 = new Canvas(bitmap1);
                    if (canvas == null) continue;
                    rectSrc = new Rect(x1, 0, x2, bitmap.getHeight());
                    rectDst = new Rect(0, 0, disW, disH);
                    if (x2 < bitmap.getWidth()) {
                        x1++;
                        x2++;
                    } else running = false;
                    pointYRT = pointY - (volMax / k);
                    Log.d(getString(R.string.LL), "");
                    canvas.drawBitmap(bitmap, rectSrc, rectDst, paint);
                    canvas.drawBitmap(pointVoice, pointX, pointYRT, paint);
                    canvas1 = canvas;
                    bitmap1 = getBitmap(canvas1);
                    if(bitmap1 != null){
                        Log.d(getString(R.string.LL),""+bitmap1.getPixel(pointX,pointYRT));
                    }
                    else Log.d(getString(R.string.LL),"bitmap1 Null");

                    //canvas.drawText(volMax,0,0,paint);
                    Log.d(getString(R.string.LL), "" + volMax);
                    // Прорисовка
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
