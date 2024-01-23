package com.example.mediapipeposetracking;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import java.util.*;


public class DrawActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置Activity的ContentView为自定义的DrawView
        setContentView(new DrawView(this));

        // 添加返回按钮
        Button backButton = new Button(this);
        backButton.setText("返回");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击按钮时关闭当前Activity，返回到前一个Activity
                finish();
            }
        });

        // 将按钮添加到布局中
        addContentView(backButton, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    // 自定义的DrawView类，用于画线
    private class DrawView extends View {

        private Paint paint;
        private int linesToDraw = 200;
        private int currentLine = 0;
        private Handler handler;
        private List<ArrayList<Double>> pose;

        public DrawView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DrawView(Context context) {
            super(context);
            init();
        }

        private void init() {
            // 初始化画笔
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);

            handler = new Handler(Looper.getMainLooper());

            pose = new ArrayList<>();
            pose.add(new ArrayList<>(Arrays.asList(0.470828265,0.6307204,5.9173999999998644e-05)));
            pose.add(new ArrayList<>(Arrays.asList(0.4707455725,0.674992985,0.05448199325)));
            pose.add(new ArrayList<>(Arrays.asList(0.468582265,0.733608735,0.025105644250000003)));
            pose.add(new ArrayList<>(Arrays.asList(0.46650165,0.7479519,-0.058693524)));
            pose.add(new ArrayList<>(Arrays.asList(0.42876896,0.71458244,0.15538035)));
            pose.add(new ArrayList<>(Arrays.asList(0.40870392,0.6758349,0.04317484)));
            pose.add(new ArrayList<>(Arrays.asList(0.42142233,0.7124254,-0.17581108)));
            pose.add(new ArrayList<>(Arrays.asList(0.4273717,0.72355545,-0.22447158)));
            pose.add(new ArrayList<>(Arrays.asList(0.5125568,0.7239487,0.062429275)));
            pose.add(new ArrayList<>(Arrays.asList(0.517501,0.7684523,-0.0071535846)));
            pose.add(new ArrayList<>(Arrays.asList(0.52202743,0.8097373,-0.011431715)));
            pose.add(new ArrayList<>(Arrays.asList(0.52143437,0.82070446,-0.038967714)));
            pose.add(new ArrayList<>(Arrays.asList(0.44329125,0.6304191,0.032037098)));
            pose.add(new ArrayList<>(Arrays.asList(0.43453565,0.56600964,4.620914e-05)));
            pose.add(new ArrayList<>(Arrays.asList(0.44833565,0.52329314,0.25514364)));
            pose.add(new ArrayList<>(Arrays.asList(0.44769672,0.5029539,0.16696012)));
            pose.add(new ArrayList<>(Arrays.asList(0.49836528,0.6310217,-0.03191875)));
            pose.add(new ArrayList<>(Arrays.asList(0.5044455,0.56414783,-0.077779904)));
            pose.add(new ArrayList<>(Arrays.asList(0.5065926,0.54205954,0.3266681)));
            pose.add(new ArrayList<>(Arrays.asList(0.5048757,0.51857275,0.30640507)));
            pose.add(new ArrayList<>(Arrays.asList(0.47066288,0.71926557,0.1089048125)));
            pose.add(new ArrayList<>(Arrays.asList(0.4248277,0.7221795,-0.2190883)));
            pose.add(new ArrayList<>(Arrays.asList(0.42979854,0.7216065,-0.18807222)));
            pose.add(new ArrayList<>(Arrays.asList(0.52386326,0.8216026,-0.046301883)));
            pose.add(new ArrayList<>(Arrays.asList(0.5210631,0.8150847,-0.014310787)));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            DisplayMetrics metrics = new DisplayMetrics();
            Context context = getApplicationContext();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
            System.out.println("整个屏幕的宽度有多大: "+metrics.widthPixels);//1080
            System.out.println("整个屏幕的长度有多高: "+metrics.heightPixels);//2290
            System.out.println("pose的长度："+pose.size());

            // 在Canvas上画线
            drawLinesWithDelay(canvas);
        }

        private void drawLinesWithDelay(final Canvas canvas) {
            if (currentLine < linesToDraw) {
                // 绘制一条线
                canvas.drawLine(500 + currentLine, 500 + currentLine, 600 + currentLine, 600 + currentLine, paint);
                for(int i=0;i<25;i++){
                    float x = pose.get(i).get(0).floatValue()*1080 +currentLine;
                    float y = (1 - pose.get(i).get(1).floatValue())*2290 +currentLine;
                    canvas.drawPoint(x,y,paint);
                }
                // 延时一段时间后继续绘制下一条线
                currentLine++;
                if(currentLine<linesToDraw){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidate(); // 请求重绘视图
                        }
                    }, 20); // 50毫秒的延时，可以根据需要调整
                }

            }
        }

    }
}
