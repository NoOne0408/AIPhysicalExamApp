package com.example.mediapipeposetracking.obliquePullUpsProject;
import android.os.Message;
import com.example.mediapipeposetracking.MainActivity;
import java.util.TimerTask;

import javax.xml.parsers.SAXParser;

public class ObliquePullUps {

   PoseTest poseTest=new PoseTest(
            10,
            5,
            5,
            10,
            20,
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
    private static long count_slot = 0;



    //违规判断标记位,塌腰挺腹，曲腿，移动脚，超过十秒
    private static boolean bow_flag=false;
    private static boolean bend_leg_flag=false;
    private static boolean move_feet_flag=false;
    private static boolean max_angle_flag=false;


    private static int bow_count=0;
    private static int bend_leg_count=0;
    private static int move_feet_count =0;


    public static int count=0;
    //用于判断是不是达到准备动作要求
    private boolean isReady=false;
    private int n=0;

    //用于记录身体与腿部夹角最大值
    public float maxBodyLegAngleLeft=0;
    public float maxBodyLegAngleRight=0;

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
        count_slot=0;


        //违规判断标记位,塌腰挺腹，曲腿，移动脚，超过十秒
        bow_flag=false;
        bend_leg_flag=false;
        move_feet_flag=false;
        max_angle_flag=false;


        bow_count=0;
        bend_leg_count=0;
        move_feet_count =0;

        n=0;
        count=0;
        //用于判断是不是达到准备动作要求
        isReady=false;

        maxBodyLegAngleLeft=0;
        maxBodyLegAngleRight=0;
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
                    RShoulder,RWrist,RElbow,RAnkle,22);

            if (isReady){
                poseTest.initFrame(LWrist, LElbow, LShoulder, LAnkle, LHeel,LIndex,
                        RWrist, RElbow, RShoulder, RAnkle,RHeel,RIndex);
                PoseTest.keyMessage="已做好准备！";
            }
            else{
                PoseTest.keyMessage="请调整姿势！" ;
            }
        }


        else{
            poseJudge();
            n++;
        }


        MainActivity.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //使用handler发送消息
                Message message = new Message();
                MainActivity.mHandler.sendMessage(message);
            }
        },1000,1000);//每 1s执行一次


    }




    //判断1姿势是否正确 2是否到达准备状态 3是否到达计数状态
    private void poseJudge(){
        //从三个方面判断姿势是否正确 1腿部伸直 2躯干伸直 3脚部是否不移动（存疑，因为测试场地条件无法保证）
        judgeCommonPose();

        //判断是否到达准备状态
        judgeStartPose();

        //判断是否到达计数状态
        judgeCountPose();


       //判断条件复位
        bend_leg_flag=false;
        bow_flag=false;
        move_feet_flag=false;
        max_angle_flag=false;

        System.out.println("n:"+n);
        System.out.println("count:"+count);
        System.out.println("bend_leg_count:"+bend_leg_count);
        System.out.println("bow_count:"+bow_count);
    }

    private void setStart_flag_count(){
        start_flag=true;
        //获取当前时间戳
        start_time= System.currentTimeMillis();

        //如果到达准备状态重新进行塌腰挺腹的判断,腿部弯曲状态的判断
        bow_count=0;
        bend_leg_count=0;
        move_feet_count=0;

        maxBodyLegAngleLeft=0;
        maxBodyLegAngleRight=0;
    }

    private void setCount_flag_count(){
        start_flag=false;
        count_flag=false;
        condition_satisfy=false;
        max_angle_flag=false;

        start_time=max_time;
        count_time=max_time;

        //新计数之后将之前的错误动作计数清零
        bend_leg_count=0;
        bow_count=0;
        move_feet_count=0;

        maxBodyLegAngleLeft=0;
        maxBodyLegAngleRight=0;

        count_slot=count_time-start_time;
        System.out.println("计数间隔时间："+count_slot);

    }

    private void judgeCommonPose(){
        boolean[] result=poseTest.isPoseCorrect(LShoulder,LHip,LKeen,LAnkle,LHeel,LIndex,
                RShoulder,RHip,RKeen,RAnkle,RHeel,RIndex);
        bend_leg_flag=result[0];
        bow_flag=result[1];
        move_feet_flag=result[2];

        if (bend_leg_flag){
            System.out.println("请伸直双腿！");
//            PoseTest.keyMessage="请伸直双腿！";
            bend_leg_count+=1;
        }
        else System.out.println("双腿直");

        if(bow_flag){
            System.out.println("请挺直躯干！");
            bow_count+=1;
        }
        else System.out.println("躯干直");

        if (move_feet_flag){
            System.out.println("请勿移动双脚！");
//            PoseTest.keyMessage="请勿移动双脚！";
            move_feet_count +=1;
        }
        else System.out.println("双脚固定");

        //判断身体是否挺直（未塌腰挺腹）
        //计算身体与腿部的夹角，并且更新最大值
        float angle_left = utils.calAngle(LShoulder,LHip,LHip,LKeen);
        float angle_right = utils.calAngle(RShoulder,RHip,RHip,RKeen);
        if (angle_left>this.maxBodyLegAngleLeft)this.maxBodyLegAngleLeft=angle_left;
        if (angle_right>this.maxBodyLegAngleRight)this.maxBodyLegAngleRight=angle_right;
        //判断在当前准备与完成间隙，是否有夹角过大的情况，如果有则不予计数
        max_angle_flag=!(this.maxBodyLegAngleRight>20 && this.maxBodyLegAngleLeft>20);
        if (bow_count>5||!max_angle_flag)PoseTest.keyMessage="请勿塌腰挺腹!!!!!";
        jawMessage();

    }

    private void judgeStartPose(){
        boolean isStartPoseFlag=poseTest.isStartPose(LShoulder,LWrist,LElbow,LAnkle,
                RShoulder,RWrist,RElbow,RAnkle);

        if(isStartPoseFlag) {
            System.out.println("到达准备状态!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            PoseTest.keyMessage="到达准备状态！";
            setStart_flag_count();

        }
        else {
            System.out.println("未到达准备状态");
        }
    }

    private void judgeCountPose(){
        condition_satisfy=poseTest.satisfyCondition(LWrist,LElbow,LShoulder,LAnkle,RWrist,RElbow,RShoulder,RAnkle,Nose);
        if (condition_satisfy){
            System.out.println("满足计数要求");
            count_flag = true;
            count_time = System.currentTimeMillis();
        }


        //本次完成动作是否基本完成标准
        boolean finish_correct=poseTest.isFinish(LShoulder,LHip,LKeen,LAnkle,RShoulder,RHip,RKeen,RAnkle);


        //计数时间点是否准确
        boolean finish_time=start_flag && count_flag && start_time<count_time;

        System.out.println("判断条件 max_angle_flag && condition_satisfy && finish_correct &&  finish_time:"+
                max_angle_flag+" "+condition_satisfy+" "+ finish_correct +" "+  finish_time);

        //判断是否计数,满足上述四个条件
        if(max_angle_flag && condition_satisfy &&  finish_correct &&  finish_time){
            count+=1;
            System.out.println("计数+1 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  当前个数："+count);
            PoseTest.keyMessage="计数+1 ";
            setCount_flag_count();
        }


    }

    private void jawMessage(){
        boolean angle_flag=poseTest.isJawMessage(LShoulder,LAnkle,RShoulder, RAnkle);
        if(bow_flag && start_flag && angle_flag){
            PoseTest.keyMessage="下巴未过杠";
        }
    }

}
