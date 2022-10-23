package com.example.mediapipeposetracking.pullUpsProject;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.mediapipeposetracking.MainActivity;
import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.Date;
import java.util.TimerTask;

public class PullUps {

    private static final String TAG = "PULL_UP";//输出日志的TAG
    public static int count=0;
    public static String keyMessage="";//提示信息


    private static final int THRESHOLD_ELBOW_ANGLE=90;//肘部弯曲角度阈值(单位：度)
    private static final int OFFSET_ELBOW_ANGLE=20;//肘部弯曲角度变化范围(单位：度)
    private static final int MIN_VALUE_SHOULDER_ANGLE=130;//肩部角度最小值(单位：度)
    private static final int MAX_VALUE_SHOULDER_ANGLE=180;//肩部角度最大值(单位：度)
    private static final int THRESHOLD_SHOULDER_ANGLE=90;//肩部角度阈值(单位：度)
    private static final int DIS_BETWEEN_FIGER_WITH_POLE=200;//手指到杠的距离（单位：px）

    private int frame_wave=0;//至少需要过20帧才可以计数加一


    private int findFirstCorrectFrameFlag=0;//找到初始正确位置帧，0代表未找到，1代表找到
    private int isCorrectElbowAngleFlag=0;//判断肘部角度是否满足条件，0代表不满足，1代表满足
    private int isFinishedFlag=0;//判断动作是否结束，0代表未结束，1代表结束
    private int frameIndex=0;//帧序号
    private int num=0;//正确次数
    private float heightOfPole=0;//杠高

    public void startDetection(LandmarkProto.NormalizedLandmarkList landmarks,int width,int height){

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



        //判断动作是否已经结束
        if(isFinishedFlag==1){
            Log.v(TAG, "引体向上动作已经结束");
            PullUps.keyMessage="动作结束,不再计数";
        }else{

            //判断节点是否正确识别到人
            if(nose.getY()>left_shoulder.getY()||nose.getY()>right_shoulder.getY()||left_shoulder.getY()>left_hip.getY()||right_shoulder.getY()>right_hip.getY()){
                PullUps.keyMessage="未识别到正确的人体";
                return;
            }else{
                PullUps.keyMessage="";
            }

            //判断起始位置：（1）手、肘、肩角度在[130,180]之间；（2）手部结点在[-10,10]范围内变换
            if(findFirstCorrectFrameFlag==0){
                //判断初始状态帧（即跃上杠之后的第一帧）
                double angle_left_shoulder=getAngle(left_elbow,left_shoulder,left_hip);
                double angle_right_shoulder=getAngle(right_elbow,right_shoulder,right_hip);
                System.out.println("肩部角度："+angle_left_shoulder+","+angle_right_shoulder+","+frameIndex);
                //判断是否举起了双臂
                if(angle_left_shoulder>=MIN_VALUE_SHOULDER_ANGLE&&angle_left_shoulder<=MAX_VALUE_SHOULDER_ANGLE&&angle_right_shoulder>=MIN_VALUE_SHOULDER_ANGLE&&angle_right_shoulder<=MAX_VALUE_SHOULDER_ANGLE){
                    //获得杠高
                    heightOfPole=(left_index.getY()*height+right_index.getY()*height)/2;
                    System.out.println("杠高:"+heightOfPole+",轮次："+frameIndex);
                    findFirstCorrectFrameFlag=1;
                }
            }else{
                frame_wave+=1;
                double angle_left_elbow=getAngle(left_wrist,left_elbow,left_shoulder);
                double angle_right_elbow=getAngle(right_wrist,right_elbow,right_shoulder);
                double angle_shoulder_left=getAngle(left_elbow,left_shoulder,left_hip);
                double angle_shoulder_right=getAngle(right_elbow,right_shoulder,right_hip);
                System.out.println("肘部角度："+angle_left_elbow+","+angle_right_elbow+","+frameIndex+","+num);
                System.out.println("肩部角度："+angle_shoulder_left+","+angle_shoulder_right+","+frameIndex+","+num);

                float l=heightOfPole-mouth.getY()*height;
                Log.d(TAG, "杠高与鼻子的距离: "+l);
                Log.d(TAG, "鼻子高度: "+nose.getY()*height+",轮次:"+frameIndex);
                if( (angle_left_elbow<THRESHOLD_ELBOW_ANGLE || angle_right_elbow < THRESHOLD_ELBOW_ANGLE) && l>0 ){//||l>20&&angle_shoulder_left<=65&&angle_shoulder_right<=65
                    if(isCorrectElbowAngleFlag==0){
                        System.out.println("过去的帧数："+frame_wave);
                        System.out.println("时间："+new Date(System.currentTimeMillis()));
                        if(frame_wave>10){//若从上次计数+1到这次计数+1已经过了10帧以上，则加一
                            num=num+1;
                            frame_wave=0;
                            System.out.println("成功次数+1");
                        }
                        isCorrectElbowAngleFlag=1;
                    }
                }else {
                    isCorrectElbowAngleFlag = 0;
                }
                PullUps.count=num;
                //左指与杠高、右指与杠高距离大于200像素时认为下杠，动作结束
                float l1=Math.abs(left_index.getY()*height-heightOfPole);
                float l2=Math.abs(right_index.getY()*height-heightOfPole);
                System.out.println("差距:"+l1+","+l2+","+frameIndex);
                if(l1>200&&l2>200){//&&angle_shoulder_left<30&&angle_shoulder_right<30
                    PullUps.keyMessage="动作结束,不再计数";
                    isFinishedFlag=1;
                }
            }
            frameIndex++;
        }

        //调用定时器发送消息给Handler
        MainActivity.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //使用handler发送消息
                Message message = new Message();
                MainActivity.mHandler.sendMessage(message);
            }
        },1000,1000);//每 1s执行一次

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


    public void recover(){
        findFirstCorrectFrameFlag=0;
        isCorrectElbowAngleFlag=0;
        isFinishedFlag=0;
        frameIndex=0;
        num=0;
        heightOfPole=0;
        count=0;
        keyMessage="";
    }
}
