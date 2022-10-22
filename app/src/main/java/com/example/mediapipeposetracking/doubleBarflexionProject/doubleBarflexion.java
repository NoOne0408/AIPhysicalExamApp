package com.example.mediapipeposetracking.doubleBarflexionProject;
import android.os.Message;

import com.example.mediapipeposetracking.MainActivity;

import java.util.TimerTask;

public class doubleBarflexion {
    PoseTest poseTest=new PoseTest(
            5,
            5,
            5,
            5,
            10,
            10);

    //起始、计数标记位
    private static boolean start_flag=false;
    private static boolean count_flag=false;

    //每个条件满足情况
    private static boolean condition_satisfy=false;

    //起始计数时间戳
    private static long max_time = 999999999;
    private static long start_time = max_time;
    private static long count_time = max_time;
    private static long count_slot;



    //违规判断标记位,塌腰挺腹，曲腿，移动脚，超过十秒
    private static boolean bow_flag=false;
    private static boolean bend_leg_flag=false;
    private static boolean move_feet_flag=false;
//    private static boolean over_time_flag=false;


    private static int bow_count=0;
    private static int bend_leg_count=0;
    private static int move_feet_count =0;

    private int n=0;
    public static int count=0;
    //用于判断是不是达到准备动作要求
    private static boolean isReady=false;


    Point RShoulder,LShoulder,RHip,LHip,RKeen,LKeen,RAnkle,LAnkle,Nose;
    Point RElbow,LElbow,RWrist,LWrist;
    Point RHeel,LHeel,RIndex,LIndex;

    public void recover(){
        //起始、计数标记位
        start_flag=false;
        count_flag=false;

        //每个条件满足情况
        condition_satisfy=false;

        //起始计数时间戳
        max_time = 999999999;
        start_time = max_time;
        count_time = max_time;


        //违规判断标记位,塌腰挺腹，曲腿，移动脚，超过十秒
        bow_flag=false;
        bend_leg_flag=false;
        move_feet_flag=false;


        bow_count=0;
        bend_leg_count=0;
        move_feet_count =0;

        n=0;
        count=0;
        //用于判断是不是达到准备动作要求
        isReady=false;


    }




    public void updatePoints(Point RShoulder,Point LShoulder,Point RHip,Point LHip,Point RKeen,Point LKeen,Point RAnkle,Point LAnkle,Point Nose,
                             Point RElbow,Point LElbow,Point RWrist,Point LWrist,
                             Point RHeel,Point LHeel,Point RIndex,Point LIndex){
        this.RShoulder=RShoulder;
        this.LShoulder=LShoulder;
        this.RHip=RHip;
        this.LHip=LHip;
        this.RKeen=RKeen;
        this.LKeen=LKeen;
        this.RAnkle=RAnkle;
        this.LAnkle=LAnkle;
        this.Nose=Nose;

        this.RElbow=RElbow;
        this.LElbow=LElbow;
        this.RWrist=RWrist;
        this.LWrist=LWrist;

        this.RHeel=RHeel;
        this.LHeel=LHeel;
        this.RIndex=RIndex;
        this.LIndex=LIndex;


    }

    public void startDetection(){
        //如果没准备好，那就不断尝试ready的姿态,如果准备好了，那就获取初始状态
        if (!isReady){
            isReady=poseTest.isReady(LShoulder,LWrist,LElbow,LAnkle,
                    RShoulder,RWrist,RElbow,RAnkle);
            if (isReady){
                poseTest.initFrame(LWrist, LElbow, LShoulder, LAnkle, LHeel,LIndex,
                        RWrist, RElbow, RShoulder, RAnkle,RHeel,RIndex);
            }

            PoseTest.keyMessage="请调整姿势！" ;
        }


        else{
//            PoseTest.keyMessage="已做好准备！" ;
            poseJudge();
            n++;
        }


        //每隔一秒使用 handler发送一下消息,也就是每隔一秒执行一次,一直重复执行

//        DoubleBarflexionActivity.timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                //使用handler发送消息
//                Message message = new Message();
//                DoubleBarflexionActivity.mHandler.sendMessage(message);
//            }
//        },1000,1000);//每 1s执行一次

        MainActivity.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //使用handler发送消息
                Message message = new Message();
                MainActivity.mHandler.sendMessage(message);
            }
        },1000,1000);//每 1s执行一次

    }



    private void poseJudge(){

        boolean[] result=poseTest.isPoseCorrect(LShoulder,LHip,LKeen,LAnkle,LHeel,LIndex,
                RShoulder,RHip,RKeen,RAnkle,RHeel,RIndex);
        bend_leg_flag=result[0];
        bow_flag=result[1];

        if (bend_leg_flag){
            System.out.println("请伸直双腿！");
            PoseTest.keyMessage="请伸直双腿！";
            //这里有一个计数环节别忘了
            bend_leg_count+=1;
        }
        else System.out.println("双腿直");

        if(bow_flag){
            System.out.println("请挺直躯干！");
            PoseTest.keyMessage="请挺直躯干！";
            bow_count+=1;
        }
        else System.out.println("躯干直");



        //判断是否到达准备状态
        boolean isStartPoseFlag=poseTest.isStartPose(LShoulder,LWrist,LElbow,LAnkle,
                RShoulder,RWrist,RElbow,RAnkle);

        if(isStartPoseFlag) {
            System.out.println("到达准备状态!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            PoseTest.keyMessage="到达准备状态！";
            start_flag=true;
            //获取当前时间戳
            start_time= System.currentTimeMillis();
            bend_leg_flag=false;
            bow_flag=false;
            move_feet_flag=false;
        }
        else {
            System.out.println("未到达准备状态");
        }

        //是否塌腰挺腹多次/弯腿多次
        boolean finish_correct_flag=true;

        //本次动作是否标准
        boolean now_correct=!bend_leg_flag && !bow_flag;
//      boolean now_correct=true;

        if(now_correct==false){
            PoseTest.keyMessage="动作不规范,本次不计数";
            start_flag=false;
            return;
        }

        //判断是否到达计数状态
        condition_satisfy=poseTest.satisfyCondition(LWrist,LElbow,LShoulder,LAnkle,RWrist,RElbow,RShoulder,RAnkle,Nose);
//
        if (condition_satisfy){
            System.out.println("满足要求");
            PoseTest.keyMessage="达到计数状态！";
            count_flag = true;
            count_time = System.currentTimeMillis();

        }



        //计数时间点是否准确
        boolean finish_time=start_flag && count_flag && start_time<count_time;

        //判断是否计数,满足上述四个条件

        if(condition_satisfy&&finish_correct_flag&&now_correct&&finish_time){
            count+=1;
            System.out.println("计数+1 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  当前个数："+count);
            PoseTest.keyMessage="计数+1 ！";
            start_flag=false;
            count_flag=false;
            condition_satisfy=false;

            start_time=max_time;
            count_time=max_time;

            bend_leg_count=0;
            bow_count=0;

            count_slot=System.currentTimeMillis();
            System.out.println("计数间隔时间："+count_slot);
        }

        n+=1;
        bend_leg_flag=false;
        bow_flag=false;
        move_feet_flag=false;

    }

}
