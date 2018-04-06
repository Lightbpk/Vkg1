package bpk.light.vkg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class Game extends Activity {

    Bitmap bitmap, bitmap1, pointVoice;
    Rect rectSrc, rectDst, rectTextBG, rectAct;

    int x1, x2, disW, disH, pointX, pointY, pointYRT = 0, volMax, kVol, level;
    double kH,kW;


    final Handler uiHandler = new Handler();
    Timer myTimer;
    SoundMeter sm;
    SharedPreferences sP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new DrawView(this));
        Intent intent = getIntent();
        level = intent.getIntExtra("level",1);
        sP = PreferenceManager.getDefaultSharedPreferences(this);
        myTimer = new Timer();
        sm = new SoundMeter();
        kVol =sP.getInt("kVol",40);
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
            pointX = 20;
            pointY = disH - 240;
            //bitmap1 = Bitmap.createBitmap(disW, disH, Bitmap.Config.ARGB_8888);
            switch (level){
                case 1:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.song1);
                    break;
                case 2:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.songeasy);
                    break;
                case 3:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.songhard);
                    break;
            }
            //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.song1);
            pointVoice = BitmapFactory.decodeResource(getResources(), R.drawable.point);
            kH = disH / bitmap.getHeight();
            x1 = 0;
            x2 = (bitmap.getWidth()*bitmap.getHeight())/disH;
            kW = bitmap.getWidth()/x2;
            //x2 = bitmap.getWidth() / 5;
            Log.d(getString(R.string.LL), "Dispaly res " + disW + " x " + disH);
            Log.d(getString(R.string.LL), "bitmap res " + bitmap.getWidth() + " x " + bitmap.getHeight());
            Log.d(getString(R.string.LL), "x2 = " + x2);
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

       /* @Override // ---touch control---
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
        }*/
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
        final long FPS = sP.getInt("FPS",100);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint paintBG = new Paint();
        Paint paintText = new Paint();


        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas, canvasText;
            paintBG.setColor(Color.GRAY);
            paintBG.setStyle(Paint.Style.FILL);
            paintText.setTextSize(25);
            String textSong = sP.getString("textSong",getString(R.string.TextSong));
            Bitmap bitmapText = Bitmap.createBitmap(bitmap.getWidth(),(bitmap.getHeight()/10), Bitmap.Config.ARGB_8888);
            canvasText = new Canvas(bitmapText);
            canvasText.drawText(textSong,0,0,paintText);
            long ticksPS = 1000 / FPS;
            long startTime;
            long sleepTime;

            while (running) {
                canvas = null;
                startTime = System.currentTimeMillis();
                try {

                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null) continue;
                    rectSrc = new Rect(x1, 0, x2, bitmap.getHeight());
                    rectDst = new Rect(0, 0, disW, disH);
                    rectTextBG = new Rect (0,disH-(disH/10),disW,disH);
                    if (x2 < bitmap.getWidth()) {
                        x1++;
                        x2++;
                    } else running = false;
                    bitmap1 = Bitmap.createBitmap(bitmap, x1, 0, x2 - x1, bitmap.getHeight());
                    pointYRT = pointY - (volMax / kVol);
                    try{
                        if (bitmap1.getPixel((int) Math.round(pointX / kW), (int) Math.round(pointYRT / kW)) != -1) {
                            running = false;  //----- voice control------
                        }
                    } catch (Exception e){
                        /*Log.d(getString(R.string.LL), "y = "+(int) Math.round(pointYRT / kW));
                        Log.d(getString(R.string.LL), "bitmap height = "+ bitmap1.getHeight());
                        Log.d(getString(R.string.LL), "kW = "+ kW);*/
                    }
                    canvas.drawBitmap(bitmap, rectSrc, rectDst, paint);
                    canvas.drawBitmap(pointVoice, pointX, pointYRT, paint);
                    canvas.drawRect(rectTextBG,paintBG);
                    canvas.drawText(textSong,0-x1,disH - 50,paintText);
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
                sleepTime = ticksPS-(System.currentTimeMillis() - startTime);
                try {
                    if (sleepTime > 0)
                        sleep(sleepTime);
                    else
                        sleep(10);
                } catch (Exception e) {}
            }
        }
    }
}
