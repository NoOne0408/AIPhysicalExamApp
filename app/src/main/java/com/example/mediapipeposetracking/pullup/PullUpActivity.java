package com.example.mediapipeposetracking.pullup;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    private FrameProcessor processor;//帧处理器，将相机预览帧发送到MediaPipe图中进行处理，并在Surface显示处理后的图像
    private ExternalTextureConverter converter;//将Android摄像头的GL_TEXTURE_EXTERNAL_OES纹理转换为常规纹理，供FrameProcessor和底层MediaPipe图使用
    private ApplicationInfo applicationInfo;//ApplicationInfo用于检索清单中定义的元数据
    private CameraXPreviewHelper cameraHelper;//通过CameraX Jetpack支持库处理相机访问
    static {System.loadLibrary("mediapipe_jni");System.loadLibrary("opencv_java3");}//加载应用程序所需的所有本机库,mediapipe依赖//opencv依赖//这俩个so文件都包含在arr库中，

    //自己加的依赖
    private TextView showData,showData2,showData3,showData4,showData5;//提示输出框
    private int findFirstCorrectFrameFlag=0;//找到初始正确位置帧，0代表未找到，1代表找到
    private int isCorrectElbowAngleFlag=0;//判断肘部角度是否满足条件，0代表不满足，1代表满足
    private int isFinishedFlag=0;//判断动作是否结束，0代表未结束，1代表结束
    private int frameIndex=0;//帧序号
    private int num=0;//正确次数
    int frameNum=0; //帧数
    private float hightOfPole=0;//杠高
    private float shouldIsUp=0;//肩膀是否在上升，0代表正在下降，1代表正在升高
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String str = msg.getData().getString("text1");//接受msg传递过来的参数
                    String strr = msg.getData().getString("text2");//接受msg传递过来的参数
                    showData2.setText("开始帧："+str+"，杠高："+strr+"px");
                    break;
                case 2:
                    String str1 = msg.getData().getString("text1");//接受msg传递过来的参数
                    showData4.setText(str1);
                    break;
                case 3:
                    String str2 = msg.getData().getString("text1");//接受msg传递过来的参数
                    String str22 = msg.getData().getString("text2");//接受msg传递过来的参数
                    String str222 = msg.getData().getString("text3");//接受msg传递过来的参数
                    showData3.setText("计数："+str2+",度数:"+str22+"||"+str222);
                    break;
                case 4:
                    String str4 = msg.getData().getString("text1");//接受msg传递过来的参数
                    String str44 = msg.getData().getString("text2");//接受msg传递过来的参数
                    showData.setText(str4+"|"+str44);
                    break;
                case 5:
                    String str5 = msg.getData().getString("text1");//接受msg传递过来的参数
                    showData5.setText(str5);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.pullup);

        //获取APP相关数据
        try {
            applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

        //创建SurfaceView对象并设置监听函数并放到盒子中，如果用findById方式初始化SurfaceView会出现黑屏问题
        previewDisplayView = new SurfaceView(this);
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        showData=new TextView(this);
        showData.setTextColor(Color.YELLOW);
        viewGroup.addView(showData);

        showData2=new TextView(this);
        showData2.setTextColor(Color.RED);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin=50;
        showData2.setLayoutParams(lp);
        viewGroup.addView(showData2);

        showData3=new TextView(this);
        showData3.setTextColor(Color.GREEN);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp3.topMargin=100;
        showData3.setLayoutParams(lp3);
        viewGroup.addView(showData3);

        showData4=new TextView(this);
        showData4.setTextColor(Color.GREEN);
        LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp4.topMargin=250;
        showData4.setLayoutParams(lp4);
        viewGroup.addView(showData4);

        showData5=new TextView(this);
        showData5.setTextColor(Color.GREEN);
        LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lp5.topMargin=300;
        showData5.setLayoutParams(lp5);
        viewGroup.addView(showData5);


//        previewDisplayView.setZOrderOnTop(true);//使用此方法后，SurfaceView会挡住其他布局，但挡不住dialog之类的
//        previewDisplayView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        previewDisplayView.getHolder().addCallback(new SurfaceHolder.Callback() {//在SurfaceView中可以通过getHolder()方法获取到SurfaceHolder实例,SurfaceHolder实例可以用来操控Surface。创建SurfaceView的时候需要实现SurfaceHolder.Callback接口，它可以用来监听SurfaceView的状态，生命周期包括创建、改变、销毁
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
//        Paint textPaint = new Paint();
//        textPaint.setColor(Color.WHITE);
//        textPaint.setTextSize(60.0f);
//        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);
        processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME, (packet) -> {

            Bundle bundle5=new Bundle();
            bundle5.putString("text1",String.valueOf(frameNum));  //往Bundle中存放数据
            Message message5=new Message();
            message5.what=5;
            message5.setData(bundle5);
            handler.sendMessage(message5);

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


//                    System.out.println("nose:"+nose.getY());
//                    System.out.println(left_shoulder.getY()+"|"+left_hip.getY());
//                    System.out.println(right_shoulder.getY()+"|"+right_hip.getY());

                //判断节点是否正确识别到人
                if(nose.getY()>left_shoulder.getY()||nose.getY()>right_shoulder.getY()||left_shoulder.getY()>left_hip.getY()||right_shoulder.getY()>right_hip.getY()){
                    Bundle bundle=new Bundle();
                    bundle.putString("text1","未识别到正确的人体");  //往Bundle中存放数据
                    Message message=new Message();
                    message.what=2;
                    message.setData(bundle);
                    handler.sendMessage(message);
                    return;
                }else{
                    Bundle bundle=new Bundle();
                    bundle.putString("text1","");  //往Bundle中存放数据
                    Message message=new Message();
                    message.what=2;
                    message.setData(bundle);
                    handler.sendMessage(message);
                }

                //判断动作是否已经结束
                if(isFinishedFlag==1){
                    return;
                }

                //判断起始位置：（1）手、肘、肩角度在[150,180]之间；（2）手部结点在[-10,10]范围内变换


                if(findFirstCorrectFrameFlag==0){
                    //判断初始状态帧（即跃上杠之后的第一帧）
                    double angle_left_shoulder=getAngle(left_elbow,left_shoulder,left_hip);
                    double angle_right_shoulder=getAngle(right_elbow,right_shoulder,right_hip);
                    //判断是否举起了双臂
                    if(angle_left_shoulder>=130&&angle_left_shoulder<=180&&angle_right_shoulder>=130&&angle_right_shoulder<=180){
                        //获得杠高
                        hightOfPole=(left_index.getY()*height+right_index.getY()*height)/2;
                        Bundle bundle=new Bundle();
                        bundle.putString("text1",String.valueOf(frameIndex));  //往Bundle中存放数据
                        bundle.putString("text2",String.valueOf(hightOfPole));
                        Message message=new Message();
                        message.what=1;
                        message.setData(bundle);
                        handler.sendMessage(message);
                        findFirstCorrectFrameFlag=1;
                    }
                }else{
                    double angle_left_elbow=getAngle(left_wrist,left_elbow,left_shoulder);
                    double angle_right_elbow=getAngle(right_wrist,right_elbow,right_shoulder);
                    double angle_shoulder_left=getAngle(left_elbow,left_shoulder,left_hip);
                    double angle_shoulder_right=getAngle(right_elbow,right_shoulder,right_hip);
                    if((angle_left_elbow<=65||Math.abs(angle_left_elbow-65)<20)||(angle_right_elbow<=65||Math.abs(angle_right_elbow-65)<20)){//&&angle_shoulder_left<=65&&angle_shoulder_right<=65
                        if(isCorrectElbowAngleFlag==0){
                            num=num+1;
                            isCorrectElbowAngleFlag=1;
                        }
                    }else {
                        isCorrectElbowAngleFlag = 0;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("text1",String.valueOf(num));  //往Bundle中存放数据
                    bundle.putString("text2",angle_left_elbow+","+angle_right_elbow);
                    bundle.putString("text3",angle_shoulder_left+","+angle_shoulder_right);
                    Message message=new Message();
                    message.what=3;
                    message.setData(bundle);//mes利用Bundle传递数据
                    handler.sendMessage(message);
                    //左指与杠高、右指与杠高距离大于50像素时认为下杠，动作结束
                    float l1=Math.abs(left_index.getY()*height-hightOfPole);
                    float l2=Math.abs(right_index.getY()*height-hightOfPole);
                    if(l1>100&&l2>100){
                        System.out.println("count:"+num);
                        Bundle bundle22 = new Bundle();
                        bundle22.putString("text1","动作结束,不再计数");  //往Bundle中存放数据
                        bundle22.putString("text2",l1+","+l2);  //往Bundle中存放数据
                        Message message22=new Message();
                        message22.what=4;
                        message22.setData(bundle22);//mes利用Bundle传递数据
                        handler.sendMessage(message22);
                        isFinishedFlag=1;
                    }
                }
                frameIndex++;
                frameNum++;
            } catch (InvalidProtocolBufferException e) {
                Log.e(TAG, "Couldn't Exception received - " + e);
                return;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();
        previewDisplayView.setVisibility(View.GONE);//隐藏预览显示，直到我们再次打开相机。
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

    //在屏幕上写文字描述
    protected void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.drawText(text, x, y, paint);
    }


}
