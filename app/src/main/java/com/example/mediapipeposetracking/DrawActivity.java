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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.*;


public class DrawActivity extends Activity {

    public static JSONArray poseArrays = new JSONArray();

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
        private Paint paintPoint;
        private int linesToDraw = 200;
        private int currentLine = 0;
        private Handler handler;

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
            paintPoint = new Paint();
            paintPoint.setColor(Color.YELLOW);
            paintPoint.setStrokeWidth(10);

            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            DisplayMetrics metrics = new DisplayMetrics();
            Context context = getApplicationContext();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
//            System.out.println("整个屏幕的宽度有多大: "+metrics.widthPixels);//1080
//            System.out.println("整个屏幕的长度有多高: "+metrics.heightPixels);//2290

            // 在Canvas上画线
            drawLinesWithDelay(canvas);
        }

        private void drawLinesWithDelay(final Canvas canvas) {
            if (currentLine < DrawActivity.poseArrays.length()) {

                // 绘制人体动画
                try {
                    float x0 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(0).get(0))) * 1080;
                    float y0 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(0).get(1)))) * 2290;
                    float x1 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(1).get(0))) * 1080;
                    float y1 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(1).get(1)))) * 2290;
                    float x2 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(2).get(0))) * 1080;
                    float y2 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(2).get(1)))) * 2290;
                    float x3 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(3).get(0))) * 1080;
                    float y3 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(3).get(1)))) * 2290;
                    float x4 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(4).get(0))) * 1080;
                    float y4 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(4).get(1)))) * 2290;
                    float x5 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(5).get(0))) * 1080;
                    float y5 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(5).get(1)))) * 2290;
                    float x6 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(6).get(0))) * 1080;
                    float y6 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(6).get(1)))) * 2290;
                    float x7 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(7).get(0))) * 1080;
                    float y7 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(7).get(1)))) * 2290;
                    float x8 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(8).get(0))) * 1080;
                    float y8 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(8).get(1)))) * 2290;
                    float x9 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(9).get(0))) * 1080;
                    float y9 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(9).get(1)))) * 2290;
                    float x10 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(10).get(0))) * 1080;
                    float y10 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(10).get(1)))) * 2290;
                    float x11 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(11).get(0))) * 1080;
                    float y11 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(11).get(1)))) * 2290;
                    float x12 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(12).get(0))) * 1080;
                    float y12 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(12).get(1)))) * 2290;
                    float x13 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(13).get(0))) * 1080;
                    float y13 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(13).get(1)))) * 2290;
                    float x14 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(14).get(0))) * 1080;
                    float y14 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(14).get(1)))) * 2290;
                    float x15 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(15).get(0))) * 1080;
                    float y15 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(15).get(1)))) * 2290;
                    float x16 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(16).get(0))) * 1080;
                    float y16 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(16).get(1)))) * 2290;
                    float x17 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(17).get(0))) * 1080;
                    float y17 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(17).get(1)))) * 2290;
                    float x18 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(18).get(0))) * 1080;
                    float y18 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(18).get(1)))) * 2290;
                    float x19 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(19).get(0))) * 1080;
                    float y19 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(19).get(1)))) * 2290;
                    float x20 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(20).get(0))) * 1080;
                    float y20 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(20).get(1)))) * 2290;
                    float x21 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(21).get(0))) * 1080;
                    float y21 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(21).get(1)))) * 2290;
                    float x22 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(22).get(0))) * 1080;
                    float y22 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(22).get(1)))) * 2290;
                    float x23 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(23).get(0))) * 1080;
                    float y23 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(23).get(1)))) * 2290;
                    float x24 = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(24).get(0))) * 1080;
                    float y24 = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(24).get(1)))) * 2290;
                    canvas.drawLine(x0, y0, x1, y1, paint); // 躯干
                    canvas.drawLine(x1, y1, x20, y20, paint); // 躯干
                    canvas.drawLine(x20, y20, x2, y2, paint); // 躯干
                    canvas.drawLine(x2, y2, x3, y3, paint); // 躯干

                    canvas.drawLine(x0, y0, x16, y16, paint); // 左腿
                    canvas.drawLine(x16, y16, x17, y17, paint); // 左腿
                    canvas.drawLine(x17, y17, x17, y18, paint); // 左腿
                    canvas.drawLine(x18, y18, x19, y19, paint); // 左腿

                    canvas.drawLine(x0, y0, x12, y12, paint); // 右腿
                    canvas.drawLine(x12, y12, x13, y13, paint); // 右腿
                    canvas.drawLine(x13, y13, x14, y14, paint); // 右腿
                    canvas.drawLine(x14, y14, x15, y15, paint); // 右腿

                    canvas.drawLine(x20, y20, x8, y8, paint); // 左手
                    canvas.drawLine(x8, y8, x9, y9, paint); // 左手
                    canvas.drawLine(x9, y9, x10, y10, paint); // 左手
                    canvas.drawLine(x10, y10, x11, y11, paint); // 左手
                    canvas.drawLine(x10, y10, x23, y23, paint); // 左手
                    canvas.drawLine(x10, y10, x24, y24, paint); // 左手

                    canvas.drawLine(x20, y20, x4, y4, paint); // 右手
                    canvas.drawLine(x4, y4, x5, y5, paint); // 右手
                    canvas.drawLine(x5, y5, x6, y6, paint); // 右手
                    canvas.drawLine(x6, y6, x7, y7, paint); // 右手
                    canvas.drawLine(x6, y6, x21, y21, paint); // 右手
                    canvas.drawLine(x6, y6, x22, y22, paint); // 右手

                    canvas.drawPoint(x0,y0,paintPoint);
                    canvas.drawPoint(x1,y1,paintPoint);
                    canvas.drawPoint(x2,y2,paintPoint);
                    canvas.drawPoint(x3,y3,paintPoint);
                    canvas.drawPoint(x4,y4,paintPoint);
                    canvas.drawPoint(x5,y5,paintPoint);
                    canvas.drawPoint(x6,y6,paintPoint);
                    canvas.drawPoint(x7,y7,paintPoint);
                    canvas.drawPoint(x8,y8,paintPoint);
                    canvas.drawPoint(x9,y9,paintPoint);
                    canvas.drawPoint(x10,y10,paintPoint);
                    canvas.drawPoint(x11,y11,paintPoint);
                    canvas.drawPoint(x12,y12,paintPoint);
                    canvas.drawPoint(x13,y13,paintPoint);
                    canvas.drawPoint(x14,y14,paintPoint);
                    canvas.drawPoint(x15,y15,paintPoint);
                    canvas.drawPoint(x16,y16,paintPoint);
                    canvas.drawPoint(x17,y17,paintPoint);
                    canvas.drawPoint(x18,y18,paintPoint);
                    canvas.drawPoint(x19,y19,paintPoint);
                    canvas.drawPoint(x20,y20,paintPoint);
                    canvas.drawPoint(x21,y21,paintPoint);
                    canvas.drawPoint(x22,y22,paintPoint);
                    canvas.drawPoint(x23,y23,paintPoint);
                    canvas.drawPoint(x24,y24,paintPoint);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < 25; i++) {
                    try {
                        JSONArray poseArray = DrawActivity.poseArrays.getJSONArray(currentLine); // 25*3
                        JSONArray poseXYZ = poseArray.getJSONArray(i);
                        float xxx = Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(i).get(0))) * 1080;
                        float yyy = (1 - Float.valueOf(String.valueOf(DrawActivity.poseArrays.getJSONArray(currentLine).getJSONArray(i).get(1)))) * 2290;
                        System.out.println("xxx:" + xxx);
                        System.out.println("yyy:" + yyy);
                        canvas.drawPoint(xxx, yyy, paint);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                // 延时一段时间后继续绘制下一条线
                currentLine++;
                if (currentLine < linesToDraw) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidate(); // 请求重绘视图
                        }
                    }, 33); // 33毫秒的延时，可以根据需要调整
                }

            }
        }

    }
}
