package bpk.light.vkg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

public class Game extends AppCompatActivity {
Bitmap bitmap, pointVoice;
Rect rectSrc;
Rect rectDst;
int x1,x2, disW, disH, pointX, pointY;
Matrix matrix;
float k;
Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public int max = 0;
    public static Timer mTimer;
    public static MyTimerTask mMyTimerTask;
    public int minSize;
    public AudioRecord ar;
    public short[] buffer;
    public String volMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView(this));
        minSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
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
            pointX = disW/2;
            pointY = disH/2;
            bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.song1);
            pointVoice = BitmapFactory.decodeResource(getResources(),R.drawable.point);
            //k = disH / bitmap.getHeight();
            x1 = 0;
            x2 = bitmap.getWidth()/15;
            Log.d(getString(R.string.LL),"Dispaly res "+ disW +" x "+ disH);
            Log.d(getString(R.string.LL),"bitmap res "+bitmap.getWidth()+" x "+bitmap.getHeight());
            drawThread = new DrawThread(getHolder());
            drawThread.setRunning(true);
            drawThread.start();
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
                } catch (InterruptedException e){

                }
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){

            }
            if (event.getAction() == MotionEvent.ACTION_MOVE){
                pointX = ((int) event.getX());
                pointY = ((int) event.getY());
            }
            if (event.getAction() == MotionEvent.ACTION_UP){

            }
                return true;
        }
    }
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ar.getState() == AudioRecord.RECORDSTATE_STOPPED) {
                        buffer = new short[minSize];
                        ar.startRecording();
                        ar.read(buffer, 0, minSize);
                        max = 0;
                        for (int i = 0; i < minSize; i++) {
                            short b = buffer[i];
                            if (b < 0) b = (short) -b;
                            if (b > max) max = b;
                        }
                        volMax = "" + max;
                        ar.stop();
                    }
                }
            });
        }
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
            while (running) {
                canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    if (canvas == null) continue;
                    rectSrc = new Rect(x1, 0, x2, bitmap.getHeight());
                    rectDst = new Rect(0, 0, disW, disH);
                    if(x2<bitmap.getWidth()){
                        x1++;
                        x2++;
                    }
                    canvas.drawBitmap(bitmap,rectSrc,rectDst,paint);
                    canvas.drawBitmap(pointVoice,pointX,pointY,paint);
                    //canvas.drawText(volMax,0,0,paint);
                    //Log.d(getString(R.string.LL),"pixel "+bitmap.getPixel(pointX,pointY));
                    // Прорисовка
                    // canvas.drawColor(Color.GREEN);
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}
