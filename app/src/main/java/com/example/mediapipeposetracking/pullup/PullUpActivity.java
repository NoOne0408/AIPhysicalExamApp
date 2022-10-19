package com.example.mediapipeposetracking.pullup;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
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
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;

import com.example.mediapipeposetracking.MainActivity;
import com.example.mediapipeposetracking.R;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Date;

public class PullUpActivity extends AppCompatActivity {

    //原有的依赖
    private static final String TAG = "PULL_UP";//输出日志的TAG
    private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";//指定二进制文件
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";//输入视频流
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";//输出视频流
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";//人体姿态关节点
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.BACK;//指定打开后置摄像头FRONT、BACK
    private static final boolean FLIP_FRAMES_VERTICALLY = true;//垂直翻转帧在将相机预览帧发送到帧处理器之前垂直翻转它们在MediaPipe图中处理，并在显示处理过的帧时将其翻转回来。这是必需的，因为OpenGL表示的图像假设图像原点在左下角角，而MediaPipe一般假设图像原点在左上角
    private SurfaceTexture previewFrameTexture;//SurfaceTexture在这里可以访问相机预览帧。SurfaceTexture可以用作非直接输出的内容流，这样就提供二次处理的机会
    private SurfaceView previewDisplayView;//SurfaceView显示由MediaPipe图形处理的摄像机预览帧 ,用于动态显示视频的组件
    private EglManager eglManager;//创建和管理一个EGLContext，EglManager主要作用是管理OpenGL上下文，比如创建EglSurface、指定当前操作的Surface、swapBuffers等，主要负责场景及节点的管理工作：
//    private FrameProcessor processor;//帧处理器，将相机预览帧发送到MediaPipe图中进行处理，并在Surface显示处理后的图像
    private FrameProcessor processor;
    private ExternalTextureConverter converter;//将Android摄像头的GL_TEXTURE_EXTERNAL_OES纹理转换为常规纹理，供FrameProcessor和底层MediaPipe图使用
    private ApplicationInfo applicationInfo;//ApplicationInfo用于检索清单中定义的元数据
    private CameraXPreviewHelper cameraHelper;//通过CameraX Jetpack支持库处理相机访问
    static {System.loadLibrary("mediapipe_jni");System.loadLibrary("opencv_java3");}//加载应用程序所需的所有本机库,mediapipe依赖//opencv依赖//这俩个so文件都包含在arr库中，

    //自己加的依赖
    private SurfaceView sv_test;
    private TextView tv_count,tv_hintinfo,no_camera_access_view;//tv_arc;
    private int findFirstCorrectFrameFlag=0;//找到初始正确位置帧，0代表未找到，1代表找到
    private int isCorrectElbowAngleFlag=0;//判断肘部角度是否满足条件，0代表不满足，1代表满足
    private int isFinishedFlag=0;//判断动作是否结束，0代表未结束，1代表结束
    private int frameIndex=0;//帧序号
    private int num=0;//正确次数
    int frameNum=0; //帧数
    private float heightOfPole=0;//杠高
    private float shouldIsUp=0;//肩膀是否在上升，0代表正在下降，1代表正在升高
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String actionCount = msg.getData().getString("actionCount");//接受msg传递过来的参数
                    tv_count.setText(actionCount);
                    break;
                case 2:
                    String hintInfo = msg.getData().getString("hintInfo");//接受msg传递过来的参数
                    tv_hintinfo.setText(hintInfo);
                    break;
//                case 3:
//                    String arcinfo = msg.getData().getString("arcinfo");//接受msg传递过来的参数
//                    tv_arc.setText(arcinfo);
//                    break;
            }
        }
    };

    Button button_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pullup);

        tv_count=findViewById(R.id.count_result_p3);
        tv_hintinfo=findViewById(R.id.action_advice_p3);
//        tv_arc=findViewById(R.id.tv_arc);
        no_camera_access_view=findViewById(R.id.no_camera_access_view);
        button_close=findViewById(R.id.button_finish_p3);

        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
               finish();
            }
        });

        if(PermissionHelper.cameraPermissionsGranted(this)){
            no_camera_access_view.setVisibility(View.GONE);
        }

        //获取APP相关数据
        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

//        previewDisplayView=findViewById(R.id.sv_test);
        previewDisplayView = new SurfaceView(this);
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);
//        previewDisplayView.setZOrderOnTop(true);
//        previewDisplayView.setZOrderMediaOverlay(true);

//        previewDisplayView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        //在SurfaceView中可以通过getHolder()方法获取到SurfaceHolder实例,SurfaceHolder实例可以用来操控Surface。创建SurfaceView的时候需要实现SurfaceHolder.Callback接口，它可以用来监听SurfaceView的状态，生命周期包括创建、改变、销毁
        previewDisplayView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //(重新)计算相机预览显示的理想大小(相机预览帧渲染的区域，潜在的缩放和旋转)基于包含显示的SurfaceView的大小。
                Size viewSize = new Size(width, height);
                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                boolean isCameraRotated = cameraHelper.isCameraRotated();

                //连接转换器到相机预览帧作为它的输入(通过previewFrameTexture)，并配置输出宽度和高度作为计算的显示大小。
                converter.setSurfaceTextureAndAttachToGLContext(previewFrameTexture,
                        isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                        isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                processor.getVideoSurfaceOutput().setSurface(null);
            }
        });

        //初始化资产管理器，以便MediaPipe本机库可以访问应用程序资产，如二进制图
        AndroidAssetUtil.initializeNativeAssetManager(this);
        eglManager = new EglManager(null);

        //创建一个帧处理器并添加监听函数
        processor = new FrameProcessor(this, eglManager.getNativeContext(), BINARY_GRAPH_NAME, INPUT_VIDEO_STREAM_NAME, OUTPUT_VIDEO_STREAM_NAME);
        processor.getVideoSurfaceOutput().setFlipY(FLIP_FRAMES_VERTICALLY);//把帧垂直翻转,垂直翻转的概念：从上向下或从下向上180度翻转；
        PermissionHelper.checkAndRequestCameraPermissions(this);//判断相机是否有访问权限，如果没有则请求权限

        converter = new ExternalTextureConverter(eglManager.getContext(), 2);
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            cameraHelper = new CameraXPreviewHelper();
            cameraHelper.setOnCameraStartedListener(surfaceTexture -> {
                previewFrameTexture = surfaceTexture;
                previewDisplayView.setVisibility(View.VISIBLE);//使显示视图可见，以开始显示预览。 这触发了SurfaceHolder。 回调函数添加到previewDisplayView的holder中。
            });
            CameraHelper.CameraFacing cameraFacing = CAMERA_FACING;
            cameraHelper.startCamera(this, cameraFacing, /*unusedSurfaceTexture=*/ null, null);
        }

        processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME, (packet) -> {

            long startTime=System.currentTimeMillis();

            Log.d(TAG, "执行轮次: "+frameNum);

            byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
            int width=previewDisplayView.getWidth();
            int height=previewDisplayView.getHeight();
            try {

                //判断是否识别到节点数据
                LandmarkProto.NormalizedLandmarkList landmarks = LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);//landmarks是33个关键点，x_\y_\z_
                if (landmarks == null) {
                    Log.v(TAG, "没有查询到关节点");
                    return;
                }

                //鼻子
                LandmarkProto.NormalizedLandmark nose=landmarks.getLandmarkList().get(PoseLandmark.NOSE);
                //嘴
                LandmarkProto.NormalizedLandmark mouth=landmarks.getLandmarkList().get(PoseLandmark.LEFT_MOUTH);
                //左指
                LandmarkProto.NormalizedLandmark left_index=landmarks.getLandmarkList().get(PoseLandmark.LEFT_INDEX);
                //左手
                LandmarkProto.NormalizedLandmark left_wrist=landmarks.getLandmarkList().get(PoseLandmark.LEFT_WRIST);
                //左肘
                LandmarkProto.NormalizedLandmark left_elbow=landmarks.getLandmarkList().get(PoseLandmark.LEFT_ELBOW);
                //左肩
                LandmarkProto.NormalizedLandmark left_shoulder=landmarks.getLandmarkList().get(PoseLandmark.LEFT_SHOULDER);
                //左胯
                LandmarkProto.NormalizedLandmark left_hip=landmarks.getLandmarkList().get(PoseLandmark.LEFT_HIP);

                //右指
                LandmarkProto.NormalizedLandmark right_index=landmarks.getLandmarkList().get(PoseLandmark.RIGHT_INDEX);
                //右手
                LandmarkProto.NormalizedLandmark right_wrist=landmarks.getLandmarkList().get(PoseLandmark.RIGHT_WRIST);
                //右肘
                LandmarkProto.NormalizedLandmark right_elbow=landmarks.getLandmarkList().get(PoseLandmark.RIGHT_ELBOW);
                //右肩
                LandmarkProto.NormalizedLandmark right_shoulder=landmarks.getLandmarkList().get(PoseLandmark.RIGHT_SHOULDER);
                //右胯
                LandmarkProto.NormalizedLandmark right_hip=landmarks.getLandmarkList().get(PoseLandmark.RIGHT_HIP);

                //判断节点是否正确识别到人
                Bundle hintInfobundle=new Bundle();
                Message hintInfoMessage=new Message();
                hintInfoMessage.what=2;
                if(nose.getY()>left_shoulder.getY()||nose.getY()>right_shoulder.getY()||left_shoulder.getY()>left_hip.getY()||right_shoulder.getY()>right_hip.getY()){
                    hintInfobundle.putString("hintInfo","未识别到正确的人体");  //往Bundle中存放数据
                    hintInfoMessage.setData(hintInfobundle);
                    handler.sendMessage(hintInfoMessage);
                    return;
                }else{
                    hintInfobundle.putString("hintInfo","");  //往Bundle中存放数据
                    hintInfoMessage.setData(hintInfobundle);
                    handler.sendMessage(hintInfoMessage);
                }

                //判断动作是否已经结束
                if(isFinishedFlag==1){
                    Log.v(TAG, "引体向上动作已经结束");
                    Bundle actionEndBundle = new Bundle();
                    actionEndBundle.putString("hintInfo","动作结束,不再计数");  //往Bundle中存放数据
                    Message actionEndMessage=new Message();
                    actionEndMessage.what=2;
                    actionEndMessage.setData(actionEndBundle);//mes利用Bundle传递数据
                    handler.sendMessage(actionEndMessage);
                    return;
                }

                //判断起始位置：（1）手、肘、肩角度在[150,180]之间；（2）手部结点在[-10,10]范围内变换
                if(findFirstCorrectFrameFlag==0){
                    //判断初始状态帧（即跃上杠之后的第一帧）
                    double angle_left_shoulder=getAngle(left_elbow,left_shoulder,left_hip);
                    double angle_right_shoulder=getAngle(right_elbow,right_shoulder,right_hip);
                    System.out.println("肩部角度："+angle_left_shoulder+","+angle_right_shoulder+","+frameIndex);
                    //判断是否举起了双臂
                    if(angle_left_shoulder>=130&&angle_left_shoulder<=180&&angle_right_shoulder>=130&&angle_right_shoulder<=180){
                        //获得杠高
                        heightOfPole=(left_index.getY()*height+right_index.getY()*height)/2;
                        System.out.println("杠高:"+heightOfPole+",轮次："+frameIndex);
                        findFirstCorrectFrameFlag=1;
                    }
                }else{
                    double angle_left_elbow=getAngle(left_wrist,left_elbow,left_shoulder);
                    double angle_right_elbow=getAngle(right_wrist,right_elbow,right_shoulder);
                    double angle_shoulder_left=getAngle(left_elbow,left_shoulder,left_hip);
                    double angle_shoulder_right=getAngle(right_elbow,right_shoulder,right_hip);
                    System.out.println("肘部角度："+angle_left_elbow+","+angle_right_elbow+","+frameIndex+","+num);

//                    Bundle arcBundle = new Bundle();
//                    arcBundle.putString("arcinfo",angle_left_elbow+","+angle_right_elbow);  //往Bundle中存放数据
//                    Message arcMessage=new Message();
//                    arcMessage.what=3;
//                    arcMessage.setData(arcBundle);//mes利用Bundle传递数据
//                    handler.sendMessage(arcMessage);

                    float l=heightOfPole-mouth.getY()*height;
                    Log.d(TAG, "杠高与鼻子的距离: "+l);
                    Log.d(TAG, "鼻子高度: "+nose.getY()*height+",轮次:"+frameIndex);
                    if((Math.abs(angle_left_elbow-65)<20&&angle_shoulder_left<90)||(Math.abs(angle_right_elbow-65)<20&&angle_shoulder_right<90)){//||l>20&&angle_shoulder_left<=65&&angle_shoulder_right<=65
                        if(isCorrectElbowAngleFlag==0){
                            num=num+1;
                            System.out.println("成功次数+1");
                            isCorrectElbowAngleFlag=1;
                        }
                    }else {
                        isCorrectElbowAngleFlag = 0;
                    }
                    Bundle actionCountBundle = new Bundle();
                    actionCountBundle.putString("actionCount",String.valueOf(num));  //往Bundle中存放数据
                    Message actionCountMessage=new Message();
                    actionCountMessage.what=1;
                    actionCountMessage.setData(actionCountBundle);//mes利用Bundle传递数据
                    handler.sendMessage(actionCountMessage);
                    //左指与杠高、右指与杠高距离大于200像素时认为下杠，动作结束
                    float l1=Math.abs(left_index.getY()*height-heightOfPole);
                    float l2=Math.abs(right_index.getY()*height-heightOfPole);
                    System.out.println("差距:"+l1+","+l2+","+frameIndex);
                    if(l1>200&&l2>200){//&&angle_shoulder_left<30&&angle_shoulder_right<30
                        Bundle actionEndBundle = new Bundle();
                        actionEndBundle.putString("hintInfo","动作结束,不再计数");  //往Bundle中存放数据
                        Message actionEndMessage=new Message();
                        actionEndMessage.what=2;
                        actionEndMessage.setData(actionEndBundle);//mes利用Bundle传递数据
                        handler.sendMessage(actionEndMessage);
                        isFinishedFlag=1;
                    }
                }
                frameIndex++;
                frameNum++;
                System.out.println("耗时:"+(System.currentTimeMillis()-startTime)+"ms");
            } catch (InvalidProtocolBufferException e) {
                Log.e(TAG, "Couldn't Exception received - " + e);
                return;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();
//        previewDisplayView.setVisibility(View.GONE);//隐藏预览显示，直到我们再次打开相机。
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //获得角度
    public double getAngle(LandmarkProto.NormalizedLandmark firstPoint, LandmarkProto.NormalizedLandmark midPoint, LandmarkProto.NormalizedLandmark lastPoint){
        double AB;
        double AC;
        double BC;
        AB = Math.sqrt(Math.pow(midPoint.getX()  - firstPoint.getX() , 2) + Math.pow(midPoint.getY() - firstPoint.getY() , 2));
        AC = Math.sqrt(Math.pow(lastPoint.getX()  - firstPoint.getX() , 2) + Math.pow(lastPoint.getY() - firstPoint.getY() , 2));
        BC = Math.sqrt(Math.pow(lastPoint.getX()  - midPoint.getX() , 2) + Math.pow(lastPoint.getY() - midPoint.getY() , 2));
        double degree = (AB * AB - AC * AC + BC * BC) / ( 2 * BC * AB);
        double temp=Math.acos(degree);
        degree = Math.toDegrees(temp);
        return degree;
    }

}
