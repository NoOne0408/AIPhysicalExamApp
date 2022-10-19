package com.example.mediapipeposetracking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.example.mediapipeposetracking.doubleBarflexionProject.DoubleBarflexionActivity;
import com.example.mediapipeposetracking.obliquePullUpsProject.ObliquePullUpsActivity;
import com.example.mediapipeposetracking.pullup.PullUpActivity;
import com.google.mediapipe.components.PermissionHelper;

/**
 * Main activity of MediaPipe example apps.
 */
public class MainActivity extends AppCompatActivity {

    private Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutResId());

        //用于选择项目的按钮控件
        Button button_oblique_pullups = findViewById(R.id.button_1);
        Button button_pullups = findViewById(R.id.button_2);
        Button button_double_barflexion = findViewById(R.id.button_4);

        button_oblique_pullups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                System.out.println("斜身引体项目检测");
                button_oblique_pullups.setText("斜身引体检测");
                Intent intent=new Intent(context, ObliquePullUpsActivity.class);
                startActivity(intent);
            }
        });


        button_pullups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                System.out.println("引体向上项目检测");
                button_pullups.setText("引体向上检测");
                Intent intent=new Intent(context, PullUpActivity.class);
                startActivity(intent);

            }
        });


        button_double_barflexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                System.out.println("双杠臂屈伸项目检测");
                button_double_barflexion.setText("双杠臂屈伸检测");
                Intent intent=new Intent(context, DoubleBarflexionActivity.class);
                startActivity(intent);
            }
        });

    }


    // Used to obtain the content view for this application. If you are extending this class, and
    // have a custom layout, override this method and return the custom layout.
    protected int getContentViewLayoutResId() {
        return R.layout.activity_main;
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}