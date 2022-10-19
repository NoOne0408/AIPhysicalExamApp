package com.example.mediapipeposetracking.doubleBarflexionProject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.mediapipeposetracking.R;
import com.example.mediapipeposetracking.doubleBarflexionProject.PoseTest;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Main activity of MediaPipe example apps.
 */
public class DoubleBarflexionActivity extends AppCompatActivity {
    private static final String TAG = "DoubleBarflexion";
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
    private Context context=this;



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


//    ObliquePullUps obliquePullUps = new ObliquePullUps();
    doubleBarflexion _doubleBarflexion = new doubleBarflexion();

    //points of doubleBarflexion
    Point RShoulder_doubleBarflexion;
    Point LShoulder_doubleBarflexion;
    Point RHip_doubleBarflexion;
    Point LHip_doubleBarflexion;
    Point RKeen_doubleBarflexion;
    Point LKeen_doubleBarflexion;
    Point RAnkle_doubleBarflexion;
    Point LAnkle_doubleBarflexion;
    Point Nose_doubleBarflexion;


    //用于显示计数结果的控件
    public static TextView tvCount;
    //用于显示建议动作的控件
    public static TextView tvAction;
    //利用 Handler来发送消息和处理消息，更改 UI上的内容
    public static String project_name;
    public static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String out = doubleBarflexion.count + "";
            DoubleBarflexionActivity.tvCount.setText(out);
            DoubleBarflexionActivity.tvAction.setText(PoseTest.keyMessage);
            super.handleMessage(msg);

        }
    };
    public static Timer timer = new Timer();


    public void poseDetection() {
        tvCount = findViewById(R.id.count_result_p2);
        tvAction = findViewById(R.id.action_advice_p2);

        // 获取是否检测到pose模型输出
        processor.addPacketCallback(

                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.v(TAG, "Received multi-pose landmarks packet.");

                    Log.v(TAG, packet.toString());
                    byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                    try {
                        LandmarkProto.NormalizedLandmarkList landmarks = LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
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
                        //双杠臂屈伸
                        Point[] body_points_doubleBarflexion = BodyModule.getBodyKeyPoints(landmarks);
                        RShoulder_doubleBarflexion = body_points_doubleBarflexion[0];
                        LShoulder_doubleBarflexion = body_points_doubleBarflexion[1];
                        RHip_doubleBarflexion = body_points_doubleBarflexion[2];
                        LHip_doubleBarflexion = body_points_doubleBarflexion[3];
                        RKeen_doubleBarflexion = body_points_doubleBarflexion[4];
                        LKeen_doubleBarflexion = body_points_doubleBarflexion[5];
                        RAnkle_doubleBarflexion = body_points_doubleBarflexion[6];
                        LAnkle_doubleBarflexion = body_points_doubleBarflexion[7];


                        Point[] arm_points_doubleBarflexion = ArmModule.getArmKeyPoints(landmarks);
                        Point RWrist_doubleBarflexion = arm_points_doubleBarflexion[0];
                        Point LWrist_doubleBarflexion = arm_points_doubleBarflexion[1];
                        Point RElbow_doubleBarflexion = arm_points_doubleBarflexion[2];
                        Point LElbow_doubleBarflexion = arm_points_doubleBarflexion[3];

                        Point[] feet_points_doubleBarflexion = FootModule.getFootKeyPoints(landmarks);
                        Point RHeel_doubleBarflexion = feet_points_doubleBarflexion[0];
                        Point LHeel_doubleBarflexion = feet_points_doubleBarflexion[1];
                        Point RIndex_doubleBarflexion = feet_points_doubleBarflexion[2];
                        Point LIndex_doubleBarflexion = feet_points_doubleBarflexion[3];

                        //双杠臂屈伸项目检测开始
                        _doubleBarflexion.updatePoints(RShoulder_doubleBarflexion, LShoulder_doubleBarflexion, RHip_doubleBarflexion, LHip_doubleBarflexion, RKeen_doubleBarflexion, LKeen_doubleBarflexion, RAnkle_doubleBarflexion, LAnkle_doubleBarflexion, Nose_doubleBarflexion,
                                RElbow_doubleBarflexion, LElbow_doubleBarflexion, RWrist_doubleBarflexion, LWrist_doubleBarflexion, RHeel_doubleBarflexion, LHeel_doubleBarflexion, RIndex_doubleBarflexion, LIndex_doubleBarflexion);
                        _doubleBarflexion.startDetection();


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
        checkCamera();

        try {
            applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

        previewDisplayView = new SurfaceView(this);
        setupPreviewDisplayView();

        poseDetection();

        Button button_finish = findViewById(R.id.button_finish_p2);

        button_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在其中写入响应方法
                finish();
            }
        });


    }


    // Used to obtain the content view for this application. If you are extending this class, and
    // have a custom layout, override this method and return the custom layout.
    protected int getContentViewLayoutResId() {
        return R.layout.double_barflexion;
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
        if (PermissionHelper.cameraPermissionsGranted(DoubleBarflexionActivity.this)) {
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
}