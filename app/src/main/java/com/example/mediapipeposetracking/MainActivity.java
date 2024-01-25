package com.example.mediapipeposetracking;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediapipeposetracking.doubleBarflexionProject.doubleBarflexion;
import com.example.mediapipeposetracking.jumpingHighProject.jumpingHigh;
import com.example.mediapipeposetracking.obliquePullUpsProject.ArmModule;
import com.example.mediapipeposetracking.obliquePullUpsProject.BodyModule;
import com.example.mediapipeposetracking.obliquePullUpsProject.FootModule;
import com.example.mediapipeposetracking.obliquePullUpsProject.ObliquePullUps;
//import com.example.mediapipeposetracking.obliquePullUpsProject.ObliquePullUpsActivity;
import com.example.mediapipeposetracking.obliquePullUpsProject.Point;
import com.example.mediapipeposetracking.obliquePullUpsProject.PoseTest;
import com.example.mediapipeposetracking.pullUpsProject.PullUps;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Main activity of MediaPipe example apps.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AIPhysicalExam ";
    private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";

    // private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.BACK;
    // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
    // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
    // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
    // corner, whereas MediaPipe in general assumes the image origin is at top-left.
    private static final boolean FLIP_FRAMES_VERTICALLY = true;



    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    public static SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    public static SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    public static EglManager eglManager;
    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    public static FrameProcessor processor;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    public static ExternalTextureConverter converter;
    // ApplicationInfo for retrieving metadata defined in the manifest.
    public static ApplicationInfo applicationInfo;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    public static CameraXPreviewHelper cameraHelper;


    ObliquePullUps obliquePullUps = new ObliquePullUps();
    doubleBarflexion _doubleBarflexion = new doubleBarflexion();
    PullUps pullUps=new PullUps();
    jumpingHigh _jumpingHigh = new jumpingHigh();

    Point RShoulder, LShoulder, RHip, LHip, RKeen, LKeen, RAnkle, LAnkle, Nose;
    Point RElbow, LElbow, RWrist, LWrist;
    Point RHeel, LHeel, RIndex, LIndex;

    //points of doubleBarflexion
    com.example.mediapipeposetracking.doubleBarflexionProject.Point RShoulder_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point LShoulder_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point RHip_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point LHip_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point RKeen_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point LKeen_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point RAnkle_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point LAnkle_doubleBarflexion;
    com.example.mediapipeposetracking.doubleBarflexionProject.Point Nose_doubleBarflexion;

    //points of jumpingHigh
    com.example.mediapipeposetracking.jumpingHighProject.Point J_RShoulder, J_LShoulder, J_RHip, J_LHip, J_RKeen, J_LKeen, J_RAnkle, J_LAnkle, J_Nose;
    com.example.mediapipeposetracking.jumpingHighProject.Point J_RElbow, J_LElbow, J_RWrist, J_LWrist;
    com.example.mediapipeposetracking.jumpingHighProject.Point J_RHeel, J_LHeel, J_RIndex, J_LIndex;

    //用于显示计数结果的控件
    public static TextView tvCount;
    //用于显示建议动作的控件
    public static TextView tvAction;
    //利用 Handler来发送消息和处理消息，更改 UI上的内容
    public static String project_name;

    public static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            //要做的事情
            if (project_name.equals("obliquePullUps")) {
                String out = ObliquePullUps.count + "";
                MainActivity.tvCount.setText(out);
                MainActivity.tvAction.setText(PoseTest.keyMessage);
            }
            if (project_name.equals("doubleBarBuckling")) {
                String out = doubleBarflexion.count + "";
                MainActivity.tvCount.setText(out);
                MainActivity.tvAction.setText(com.example.mediapipeposetracking.doubleBarflexionProject.PoseTest.keyMessage);
            }
            if(project_name.equals("pullUps")){
                MainActivity.tvCount.setText(PullUps.count+"");
                MainActivity.tvAction.setText(PullUps.keyMessage);
            }
            if (project_name.equals("jumpingHigh")) {
                String out = jumpingHigh.L_msg + "";
                MainActivity.tvCount.setText(out);
                MainActivity.tvAction.setText(com.example.mediapipeposetracking.jumpingHighProject.PoseTest.keyMessage);
            }
            super.handleMessage(msg);

        }
    };
    public static Timer timer = new Timer();


    public void poseDetection(String project) {
        tvCount = findViewById(R.id.count_result);
        tvAction = findViewById(R.id.action_advice);

        //显示数据结果
        findViewById(R.id.rLayoutShow).setVisibility(View.VISIBLE);
        findViewById(R.id.rLayoutButton).setVisibility(View.INVISIBLE);


        // 获取是否检测到pose模型输出
        processor.addPacketCallback(

                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.v(TAG, "Received multi-pose landmarks packet.");

                    Log.v(TAG, packet.toString());
                    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                    try {
                        NormalizedLandmarkList landmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
                        if (landmarks == null) {
                            Log.v(TAG, "[TS:" + packet.getTimestamp() + "] No iris landmarks.");
                            return;
                        }
                        // Note: If eye_presence is false, these landmarks are useless.
                        Log.v(
                                TAG,
                                "[TS:"
                                        + packet.getTimestamp()
                                        + "] #Landmarks for iris: "
                                        + landmarks.getLandmarkCount());
//                            Log.v(TAG, getLandmarksDebugString(landmarks));


                        //斜身引体关键点获取方式

                        //获取本帧关键点坐标
                        Point[] body_points = BodyModule.getBodyKeyPoints(landmarks);

                        RShoulder = body_points[0];
                        LShoulder = body_points[1];
                        RHip = body_points[2];
                        LHip = body_points[3];
                        RKeen = body_points[4];
                        LKeen = body_points[5];
                        RAnkle = body_points[6];
                        LAnkle = body_points[7];
                        Nose = body_points[8];


                        Point[] arm_points = ArmModule.getArmKeyPoints(landmarks);
                        RWrist = arm_points[0];
                        LWrist = arm_points[1];
                        RElbow = arm_points[2];
                        LElbow = arm_points[3];

                        Point[] feet_points = FootModule.getFootKeyPoints(landmarks);
                        RHeel = feet_points[0];
                        LHeel = feet_points[1];
                        RIndex = feet_points[2];
                        LIndex = feet_points[3];

                        //其他关键点获取可以写在这里

                        //双杠臂屈伸
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point[] body_points_doubleBarflexion = com.example.mediapipeposetracking.doubleBarflexionProject.BodyModule.getBodyKeyPoints(landmarks);
                        RShoulder_doubleBarflexion = body_points_doubleBarflexion[0];
                        LShoulder_doubleBarflexion = body_points_doubleBarflexion[1];
                        RHip_doubleBarflexion = body_points_doubleBarflexion[2];
                        LHip_doubleBarflexion = body_points_doubleBarflexion[3];
                        RKeen_doubleBarflexion = body_points_doubleBarflexion[4];
                        LKeen_doubleBarflexion = body_points_doubleBarflexion[5];
                        RAnkle_doubleBarflexion = body_points_doubleBarflexion[6];
                        LAnkle_doubleBarflexion = body_points_doubleBarflexion[7];


                        com.example.mediapipeposetracking.doubleBarflexionProject.Point[] arm_points_doubleBarflexion = com.example.mediapipeposetracking.doubleBarflexionProject.ArmModule.getArmKeyPoints(landmarks);
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point RWrist_doubleBarflexion = arm_points_doubleBarflexion[0];
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point LWrist_doubleBarflexion = arm_points_doubleBarflexion[1];
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point RElbow_doubleBarflexion = arm_points_doubleBarflexion[2];
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point LElbow_doubleBarflexion = arm_points_doubleBarflexion[3];

                        com.example.mediapipeposetracking.doubleBarflexionProject.Point[] feet_points_doubleBarflexion = com.example.mediapipeposetracking.doubleBarflexionProject.FootModule.getFootKeyPoints(landmarks);
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point RHeel_doubleBarflexion = feet_points_doubleBarflexion[0];
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point LHeel_doubleBarflexion = feet_points_doubleBarflexion[1];
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point RIndex_doubleBarflexion = feet_points_doubleBarflexion[2];
                        com.example.mediapipeposetracking.doubleBarflexionProject.Point LIndex_doubleBarflexion = feet_points_doubleBarflexion[3];

                        //跳高
                        com.example.mediapipeposetracking.jumpingHighProject.Point[] J_body_points = com.example.mediapipeposetracking.jumpingHighProject.BodyModule.getBodyKeyPoints(landmarks);

                        J_RShoulder = J_body_points[0];
                        J_LShoulder = J_body_points[1];
                        J_RHip = J_body_points[2];
                        J_LHip = J_body_points[3];
                        J_RKeen = J_body_points[4];
                        J_LKeen = J_body_points[5];
                        J_RAnkle = J_body_points[6];
                        J_LAnkle = J_body_points[7];


                        com.example.mediapipeposetracking.jumpingHighProject.Point[] J_arm_points = com.example.mediapipeposetracking.jumpingHighProject.ArmModule.getArmKeyPoints(landmarks);
                        J_RWrist = J_arm_points[0];
                        J_LWrist = J_arm_points[1];
                        J_RElbow = J_arm_points[2];
                        J_LElbow = J_arm_points[3];

                        com.example.mediapipeposetracking.jumpingHighProject.Point[] J_feet_points = com.example.mediapipeposetracking.jumpingHighProject.FootModule.getFootKeyPoints(landmarks);
                        J_RHeel = J_feet_points[0];
                        J_LHeel = J_feet_points[1];
                        J_RIndex = J_feet_points[2];
                        J_LIndex = J_feet_points[3];


                        //按照自己的项目调用方式调用
                        if (project.equals("obliquePullUps")) {
                            //斜身引体动作检测开始
                            obliquePullUps.updatePoints(RShoulder, LShoulder, RHip, LHip, RKeen, LKeen, RAnkle, LAnkle, Nose,
                                    RElbow, LElbow, RWrist, LWrist, RHeel, LHeel, RIndex, LIndex);
                            obliquePullUps.startDetection();

                        } else if (project.equals("pullUps")) {
                            //引体向上动作检测开始
                            int width=previewDisplayView.getWidth();
                            int height=previewDisplayView.getHeight();
                            pullUps.startDetection(landmarks,width,height);
                        } else if (project.equals("SitUps")) {
                            //仰卧起坐动作检测开始
                        } else if (project.equals("doubleBarBuckling")) {
                            //双杠臂屈伸项目检测开始
                            _doubleBarflexion.updatePoints(RShoulder_doubleBarflexion, LShoulder_doubleBarflexion, RHip_doubleBarflexion, LHip_doubleBarflexion, RKeen_doubleBarflexion, LKeen_doubleBarflexion, RAnkle_doubleBarflexion, LAnkle_doubleBarflexion, Nose_doubleBarflexion,
                                    RElbow_doubleBarflexion, LElbow_doubleBarflexion, RWrist_doubleBarflexion, LWrist_doubleBarflexion, RHeel_doubleBarflexion, LHeel_doubleBarflexion, RIndex_doubleBarflexion, LIndex_doubleBarflexion);
                            _doubleBarflexion.startDetection();
                        } else if(project.equals("jumpingHigh")){
                            //跳高检测开始
                            _jumpingHigh.updatePoints(J_RShoulder,J_LShoulder,J_RHip,J_LHip,J_RKeen,J_LKeen,J_RAnkle,J_LAnkle,J_Nose,J_RElbow,J_LElbow,J_RWrist,J_LWrist,J_RHeel,J_LHeel,J_RIndex,J_LIndex);
                            _jumpingHigh.startDetection();
                        }


                    } catch (InvalidProtocolBufferException e) {
                        Log.e(TAG, "Couldn't Exception received - " + e);
                        return;
                    }
                });
    }


    private long zzzz = 0;
    private boolean isLuZhi = false;
    private List<List> poseFinalList = new ArrayList();
    private static TextView zzzzTextView;
    public void poseDetectionAndSendToServer() {
        tvCount = findViewById(R.id.count_result);
        tvAction = findViewById(R.id.action_advice);

        //显示数据结果
        findViewById(R.id.SendPoseToServerLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.rLayoutButton).setVisibility(View.INVISIBLE);

        // 获取是否检测到pose模型输出
        processor.addPacketCallback(

                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
//                    Log.v(TAG, "Received multi-pose landmarks packet.");

//                    Log.v(TAG, packet.toString());
                    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                    try {
                        NormalizedLandmarkList landmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
                        if (landmarks == null) {
                            Log.v(TAG, "[TS:" + packet.getTimestamp() + "] No iris landmarks.");
                            return;
                        }
                        if(isLuZhi){
                            zzzz += 1;
                            List<List<Float>> poseList = new ArrayList<>();
                            for (int i = 0; i < 33; i++) {
                                List<Float> innerList = new ArrayList<>();
                                innerList.add(landmarks.getLandmark(i).getX());
                                innerList.add(1 - landmarks.getLandmark(i).getY()); // 坐标是上下颠倒的，所以是1减去y坐标
                                innerList.add(landmarks.getLandmark(i).getZ());
                                poseList.add(innerList);
                            }
                            poseFinalList.add(poseList);
                        }
                        Log.v(TAG, "zzzz的数字： "+zzzz+"");
                        Log.v(TAG, "poseFinalList的长度： "+poseFinalList.size()+"");

                        landmarks.getLandmark(1).getX();
                        landmarks.getLandmark(1).getY();
                        landmarks.getLandmark(1).getVisibility();

                    } catch (InvalidProtocolBufferException e) {
                        Log.e(TAG, "Couldn't Exception received - " + e);
                        return;
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutResId());
        onResumeTest();


        try {
            applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

        previewDisplayView = new SurfaceView(this);
        setupPreviewDisplayView();

        //用于选择项目的按钮控件
        Button button_oblique_pullups = findViewById(R.id.button_1);
        Button button_pullups = findViewById(R.id.button_2);
//        Button button3 = findViewById(R.id.button_3);
        Button button_doublebarflexion = findViewById(R.id.button_4);
        Button button_jumpingHigh = findViewById(R.id.button_5);
        Button PoseSegmentationButton = findViewById(R.id.PoseSegmentationButton); //主页面的按钮
        Button StartButton = findViewById(R.id.StartButton); //开始录制的按钮
        Button EndButton = findViewById(R.id.EndButton); //结束录制的按钮
        Button sendButton = findViewById(R.id.sendButton); //发送pose到客户端并接收返回的结果
        Button ButtonCloseSendPoseToServer = findViewById(R.id.button_close_SendPoseToServerLayout); //返回主页面按钮
        Button startAnimationButton = findViewById(R.id.startAnimationButton); //展示返回结果
        Button TrySkeletonButton = findViewById(R.id.TrySkeletonButton); //尝试绘画骨骼动画
        Button ShowResultLayoutCloseButton = findViewById(R.id.ShowResultLayoutCloseButton); //从展示页面返回主页面
        zzzzTextView = findViewById(R.id.zzzzTextView);
        Button test1Button = findViewById(R.id.test1Button); //引体向上测试
        Button test2Button = findViewById(R.id.test2Button); //深蹲测试

        Button button_close = findViewById(R.id.button_close);

        button_oblique_pullups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                System.out.println("斜身引体项目检测");
                button_oblique_pullups.setText("斜身引体检测");
                project_name = "obliquePullUps";
                onResumeTest();
                checkCamera();
                poseDetection("obliquePullUps");
//                Intent intent=new Intent(context, ObliquePullUpsActivity.class);
//                startActivity(intent);
            }
        });


        button_pullups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                System.out.println("引体向上项目检测");
                button_pullups.setText("引体向上检测");
                project_name = "pullUps";
                onResumeTest();
                checkCamera();
                poseDetection("pullUps");
            }
        });



        button_doublebarflexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                System.out.println("双杠臂屈伸项目检测");
                button_doublebarflexion.setText("双杠臂屈伸检测");
                project_name = "doubleBarBuckling";
                onResumeTest();
                checkCamera();
                poseDetection("doubleBarBuckling");
            }
        });


        button_jumpingHigh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // 在其中写入响应方法
                System.out.println("纵跳摸高项目检测");
                button_jumpingHigh.setText("纵跳摸高检测");
                project_name = "jumpingHigh";
                onResumeTest();
                checkCamera();
                poseDetection("jumpingHigh");
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                isLuZhi = false;
                sendJsonToServer(poseFinalList,"深蹲单个动作特征序列");
            }
        });

        test1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                try {
                    Context context = getApplicationContext();
                    AssetManager assetManager = context.getAssets();
                    InputStream inputStream = assetManager.open("引体向上视频骨骼序列.json");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    bufferedReader.close();

                    // 将读取的 JSON 字符串转换为 JSONArray
                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                    System.out.println(jsonArray);
                    System.out.println(jsonArray.length());
                    List<List> poseToServer = new ArrayList<>();

                    for(int i=0;i<jsonArray.length();i++){
                        List<List<Float>> poseSubList = new ArrayList<>();
                        JSONArray poseArray = jsonArray.getJSONArray(i);
                        for(int j=0;j<33;j++){
                            JSONArray poseXYZ = poseArray.getJSONArray(j);
                            float xxx = Float.valueOf(String.valueOf(poseXYZ.get(0)));
                            float yyy = Float.valueOf(String.valueOf(poseXYZ.get(1)));
                            float zzz = Float.valueOf(String.valueOf(poseXYZ.get(2)));
                            List<Float> innerList = new ArrayList<>();
                            innerList.add(xxx);
                            innerList.add(yyy);
                            innerList.add(zzz);
                            poseSubList.add(innerList);
                        }
                        poseToServer.add(poseSubList);
                    }
                    sendJsonToServer(poseToServer,"相机的引体向上正面特征序列");

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        test2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                try {
                    Context context = getApplicationContext();
                    AssetManager assetManager = context.getAssets();
                    InputStream inputStream = assetManager.open("深蹲视频骨骼序列.json");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    bufferedReader.close();

                    // 将读取的 JSON 字符串转换为 JSONArray
                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());
                    System.out.println(jsonArray);
                    System.out.println(jsonArray.length());
                    List<List> poseToServer = new ArrayList<>();

                    for(int i=0;i<jsonArray.length();i++){
                        List<List<Float>> poseSubList = new ArrayList<>();
                        JSONArray poseArray = jsonArray.getJSONArray(i);
                        for(int j=0;j<33;j++){
                            JSONArray poseXYZ = poseArray.getJSONArray(j);
                            float xxx = Float.valueOf(String.valueOf(poseXYZ.get(0)));
                            float yyy = Float.valueOf(String.valueOf(poseXYZ.get(1)));
                            float zzz = Float.valueOf(String.valueOf(poseXYZ.get(2)));
                            List<Float> innerList = new ArrayList<>();
                            innerList.add(xxx);
                            innerList.add(yyy);
                            innerList.add(zzz);
                            poseSubList.add(innerList);
                        }
                        poseToServer.add(poseSubList);
                    }
                    sendJsonToServer(poseToServer,"深蹲单个动作特征序列");

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        PoseSegmentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在按钮点击时执行的操作
                poseFinalList = new ArrayList<>();
                project_name = "PoseSegmentation";
                onResumeTest();
                checkCamera();
                poseDetectionAndSendToServer();
            }
        });

        ButtonCloseSendPoseToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLuZhi = false;
                zzzz = 0;
                findViewById(R.id.SendPoseToServerLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.rLayoutButton).setVisibility(View.VISIBLE);

                converter.close();

                // Hide preview display until we re-open the camera again.
                previewDisplayView.setVisibility(View.GONE);

            }
        });

        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                poseFinalList = new ArrayList<>();
                zzzz = 0;
                isLuZhi = true;
                zzzzTextView.setText("正在录制中");
            }
        });

        EndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLuZhi = false;
                zzzzTextView.setText("录制完毕：共录制"+zzzz+"帧");
            }
        });

        startAnimationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.SendPoseToServerLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.ShowResultLayout).setVisibility(View.VISIBLE);
                converter.close();

                // Hide preview display until we re-open the camera again.
                previewDisplayView.setVisibility(View.GONE);
            }
        });

        TrySkeletonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(intent);
            }
        });

        ShowResultLayoutCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLuZhi = false;
                zzzz = 0;
                findViewById(R.id.ShowResultLayout).setVisibility(View.INVISIBLE);
                findViewById(R.id.rLayoutButton).setVisibility(View.VISIBLE);

                converter.close();

                // Hide preview display until we re-open the camera again.
                previewDisplayView.setVisibility(View.GONE);

            }
        });

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button_close.setText("结束测试");
                tvCount.setText("");
                tvAction.setText("");

                findViewById(R.id.rLayoutShow).setVisibility(View.INVISIBLE);
                findViewById(R.id.rLayoutButton).setVisibility(View.VISIBLE);

                converter.close();

                // Hide preview display until we re-open the camera again.
                previewDisplayView.setVisibility(View.GONE);

                //重新刷新内容
                if (project_name.equals("obliquePullUps")) {
                    obliquePullUps.recover();

                }
                if (project_name.equals("doubleBarBuckling")) {
                    _doubleBarflexion.recover();
                }
                if (project_name.equals("pullUps")) {
                    pullUps.recover();
                }
                if (project_name.equals("jumpingHigh")) {
                    _jumpingHigh.recover();
                }

            }
        });


    }


    // Used to obtain the content view for this application. If you are extending this class, and
    // have a custom layout, override this method and return the custom layout.
    protected int getContentViewLayoutResId() {
        return R.layout.activity_main;
    }


    public void onResumeTest() {
        // Initialize asset manager so that MediaPipe native libraries can access the app assets, e.g.,
        // binary graphs.
        AndroidAssetUtil.initializeNativeAssetManager(this);
        eglManager = new EglManager(null);
        // 通过加载获取一个帧处理器
        processor =
                new FrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor
                .getVideoSurfaceOutput()
                .setFlipY(FLIP_FRAMES_VERTICALLY);

        PermissionHelper.checkAndRequestCameraPermissions(this);
        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
//        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
//        processor.setInputSidePackets(inputSidePackets);


        converter =
                new ExternalTextureConverter(
                        eglManager.getContext(), 2);
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);

    }

    //检查相机权限
    public void checkCamera(){
        if (PermissionHelper.cameraPermissionsGranted(MainActivity.this)) {
            startCamera();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();

        // Hide preview display until we re-open the camera again.
        previewDisplayView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onCameraStarted(SurfaceTexture surfaceTexture) {
        previewFrameTexture = surfaceTexture;
        // Make the display view visible to start showing the preview. This triggers the
        // SurfaceHolder.Callback added to (the holder of) previewDisplayView.
        previewDisplayView.setVisibility(View.VISIBLE);
    }

    protected Size cameraTargetResolution() {
        return null; // No preference and let the camera (helper) decide.
    }

    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    onCameraStarted(surfaceTexture);
                });
        CameraHelper.CameraFacing cameraFacing = CAMERA_FACING;
        cameraHelper.startCamera(
                this, cameraFacing, /*unusedSurfaceTexture=*/ null, cameraTargetResolution());

    }

    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    protected void onPreviewDisplaySurfaceChanged(
            SurfaceHolder holder, int format, int width, int height) {
        // (Re-)Compute the ideal size of the camera-preview display (the area that the
        // camera-preview frames get rendered onto, potentially with scaling and rotation)
        // based on the size of the SurfaceView that contains the display.
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();

        // Connect the converter to the camera-preview frames as its input (via
        // previewFrameTexture), and configure the output width and height as the computed
        // display size.
        converter.setSurfaceTextureAndAttachToGLContext(
                previewFrameTexture,
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

    private void sendJsonToServer(List poseFinalList, String actionType) {
        // 创建 JSON 对象
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pose", poseFinalList);
            jsonObject.put("actionType", actionType);
            // 添加其他键值对...
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 使用 AsyncTask 执行网络请求（在主线程中执行可能导致网络请求异常）
        new SendJsonTask().execute(jsonObject.toString());
    }

    private TextView jsonPoseTextView;
    private TextView jsonResultTextView;
    private class SendJsonTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String json = params[0];

            // 使用 OkHttp 发送 POST 请求
            OkHttpClient client =         client = new OkHttpClient.Builder()
                    .connectTimeout(200, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                    .readTimeout(200, TimeUnit.SECONDS) // 设置读取超时时间为50秒
                    .writeTimeout(200, TimeUnit.SECONDS)  // 设置写入超时时间为50秒
                    .build();

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("http://10.112.132.13:5000/video_pose_segmentation_by_sample_feature") // 替换为我的服务器 URL
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // 在网络请求完成后执行的操作
            // 处理服务器响应
            try{
                JSONObject jsonObject = new JSONObject(result);
                System.out.println("JSONObject:");
                System.out.println(jsonObject);
                JSONArray data = jsonObject.getJSONArray("data");
                JSONArray pose = jsonObject.getJSONArray("pose");
//                System.out.println(pose.get(1));
                jsonResultTextView = findViewById(R.id.resultTextView);
//                jsonResultTextView.setText("分割结果: "+data.toString());
                jsonPoseTextView = findViewById(R.id.poseTextView);
//                jsonPoseTextView.setText("pose序列: "+pose.toString());
                System.out.println("JSONArray 的形状（长度）为: " + pose.length());
                System.out.println("pose.getJSONArray(0): " + pose.getJSONArray(0).getJSONArray(0));
                DrawActivity.poseArrays = pose;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // 代码执行完毕后，在主线程中弹窗通知已经接收到服务器的response
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast toast = Toast.makeText(MainActivity.this, "服务器处理完毕", Toast.LENGTH_SHORT);
//                                toast.setGravity(0, 0, 16);
//                                toast.show();
//                            }
//                        });
//                    }
//                }).start();
            }catch(JSONException e){
                e.printStackTrace();
            }

//            System.out.println("onPostExecute:"+result);
        }
    }
}