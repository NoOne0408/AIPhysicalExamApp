package com.example.mediapipeposetracking.jumpingHighProject;

import android.os.Message;

import com.example.mediapipeposetracking.MainActivity;

import java.util.TimerTask;

public class jumpingHigh {
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
    public static String L_msg = "";
    //用于判断是不是达到准备动作要求
    private static boolean isReady=false;

    //记录跳高的高度，以及判断是否已经移动、跳跃记录成功
    private static float jumping_distance = 0;
    private static boolean Jumping_successive = false;
    private static boolean hasMoved = false;


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

        //调用posetest的recover函数
        poseTest.recover();

        //将跳高记录抹除
        jumping_distance = 0;
        Jumping_successive = false;
        hasMoved = false;

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
        if((LShoulder.rate<0.6&&RShoulder.rate<0.6)||(LWrist.rate<0.6&&RWrist.rate<0.6)||(LElbow.rate<0.6&&RElbow.rate<0.6)||(LHeel.rate<0.6&&RHeel.rate<0.6)){
            PoseTest.keyMessage="人体拍摄不全" ;
            MainActivity.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //使用handler发送消息
                    Message message = new Message();
                    MainActivity.mHandler.sendMessage(message);
                }
            },1000,1000);//每 1s执行一次
        }
        else{
            //如果没准备好，那就不断尝试ready的姿态,如果准备好了，那就获取初始状态

            if (!isReady){
                isReady=poseTest.isReady(LShoulder,LWrist,LElbow,LAnkle,LHeel,LIndex,
                        RShoulder,RWrist,RElbow,RAnkle,RHeel,RIndex);
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


            MainActivity.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //使用handler发送消息
                    Message message = new Message();
                    MainActivity.mHandler.sendMessage(message);
                }
            },1000,1000);//每 1s执行一次
        }


    }



    private void poseJudge(){
        //已经跳跃成功了,输出成绩并直接返回
        if(Jumping_successive == true){
            PoseTest.keyMessage = "跳跃高度: "+jumping_distance;
            return ;
        }

        //还没移动过
        if(hasMoved == false){
            hasMoved = poseTest.hasMoved(LAnkle,LHeel,LIndex,RAnkle,RHeel,RIndex);
        }
        //已经移动过了
        else{
            //先写死所取得的跳高距离
            jumping_distance = 20;
            if(jumping_distance>30){
                Jumping_successive = true;
            }
            //判断垫脚
            else{
                PoseTest.keyMessage = "出现垫脚，请重新准备";
//                这个recover是要用的，现在注释是为了方便看出垫脚或者跳高的输出信息
//                recover();
            }

        }



    }

}

