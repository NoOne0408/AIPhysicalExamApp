package com.example.mediapipeposetracking.jumpingHighProject;


import java.util.LinkedList;

public class PoseTest {
    private static float threshold_small;//胳膊小阈值
    private static int threshold_arm_angle_sub;//胳膊弯曲角度误差值
    private static int  threshold_angle_sub;//用于检测动作完成的角度误差值
    private static int  threshold_body_floor_angle_sub;//用于判断开始动作时与地面角度的误差值
    private static int threshold_arm_body_angle_sub;//用于判断准备动作时胳膊与身体夹角的误差值
    private static float  threshold_nose_dis;
    private static int threshold_leg_angle_sub;//腿部弯曲角度误差值
    private static int threshold_body_leg_angle_sub;//塌腰挺腹角度误差值
    private static boolean is_first_satisfy_angle=false;
    public static int width=1080;
    public static int hight=2340;
    public static String keyMessage="";

    //用来判断脚的移动速度和幅度
    public static LinkedList<Point> feet_L_index_list = new LinkedList<>();
    public static LinkedList<Point> feet_R_index_list = new LinkedList<>();
//    public static LinkedList<Point> feet_L_distance_list = new LinkedList<>();
//    public static LinkedList<Point> feet_R_distance_list = new LinkedList<>();
    public static int feet_list_maxSize = 10;
    private static Point init_L_feet_point;
    private static Point init_R_feet_point;
    public static float feet_L_distance_sum = 0;//记录左脚maxSize个时间内的移动距离之和
    public static float feet_R_distance_sum = 0;//记录左脚maxSize个时间内的移动距离之和


    //设置测试阈值
    public PoseTest(int threshold_arm_angle_sub,
                    int threshold_angle_sub,
                    int threshold_body_floor_angle_sub,
                    int threshold_arm_body_angle_sub,
                    int threshold_leg_angle_sub,
                    int threshold_body_leg_angle_sub){

        PoseTest.threshold_arm_angle_sub = threshold_arm_angle_sub;
        PoseTest.threshold_angle_sub =threshold_angle_sub;
        PoseTest.threshold_body_floor_angle_sub=threshold_body_floor_angle_sub;
        PoseTest.threshold_arm_body_angle_sub=threshold_arm_body_angle_sub;
        PoseTest.threshold_leg_angle_sub = threshold_leg_angle_sub;
        PoseTest.threshold_body_leg_angle_sub = threshold_body_leg_angle_sub;
    }

    public float[]  getVaules(){
        float[] vaules={ threshold_small,
                threshold_arm_angle_sub,
        threshold_angle_sub,
        threshold_nose_dis,
                threshold_leg_angle_sub,
                threshold_body_leg_angle_sub};
        return vaules;
    }


    //检测姿势是否正确 1.腿部弯曲  2.身体弯曲  3.脚部移动
    public boolean[] isPoseCorrect(Point LShoulder,Point LHip,Point LKeen,Point LAnkle,Point LHeel,Point LIndex,
                                   Point RShoulder,Point RHip,Point RKeen,Point RAnkle,Point RHeel,Point RIndex){
        boolean leg_bend=false;
        boolean body_bend=false;
        boolean feet_moved;
        //首先检测腿是否伸直
        boolean Lleg=BodyModule.isLegStright(LHip,LKeen,LAnkle, 155);
        boolean Rleg=BodyModule.isLegStright(RHip,RKeen,RAnkle, 155);
        //当一条腿不直时，提醒腿部弯曲
        if(!Lleg||!Rleg){
            leg_bend=true;
//            System.out.println("请伸直双腿");
        }

        //其次检测身体是否笔直
        boolean Lbody=BodyModule.isBodyStright(LShoulder,LHip,LKeen, 155);
        boolean Rbody=BodyModule.isBodyStright(RShoulder,RHip,RKeen, 155);
        //当一边身体不直时，提醒弯曲
        if(!Lbody||!Rbody){
            body_bend=true;
//            System.out.println("请挺直身体");
        }

        //返回检测结果
        boolean[] result={leg_bend,body_bend};
        return result;
    }


    //检测是否到达准备状态,当手臂与地面夹角为90度左右，胳膊伸直(角度小于阈值)，躯干与地面夹角为90度左右时视作”准备开始状态“
    public boolean isStartPose(Point LShoulder,Point LWrist,Point LElbow,Point LAnkle,
                               Point RShoulder,Point RWrist,Point RElbow,Point RAnkle){

        //手臂是否呈现出90°？
        float threshold_arm_angle_left = this.LArmAngle + threshold_arm_angle_sub;
        float threshold_arm_angle_right = this.RArmAngle + threshold_arm_angle_sub;
        System.out.println("左右胳膊角度阈值："+threshold_arm_angle_left+"  "+threshold_arm_angle_right);
        boolean isArm90_left = ArmModule.isArm90(LWrist, LElbow, LShoulder, 90);
        boolean isArm90_right = ArmModule.isArm90(RWrist, RElbow, RShoulder, 90);
        //tag1
        boolean arm90Flag=(isArm90_left || isArm90_right);


        if(arm90Flag){
            return true;
        }
        return false;
    }

    //初始阈值
    private float circumference_left, circumference_right;
    private float arm_length_left,arm_length_right;
    private float angle_wrist_ankle_left, angle_wrist_ankle_right;
    private static float LArmAngle, RArmAngle;//前几次循环中胳膊夹角需要不断更新为最小值，以便确定最准确的阈值
    private float LArmBodyAngle,RArmBodyAngle;
    private float LBodyFloorAngle,RBodyFloorAngle;

    private Point LWristPoint, RWristPoint;
    private Point LAnklePoint, RAnklePoint;
    private Point LHeelPoint,RHeelPoint;
    private Point LIndexPoint,RIndexPoint;


    //初始状态获取
    public  void initFrame(Point LWrist, Point LElbow, Point LShoulder, Point LAnkle,Point LHeel,Point LIndex,
                           Point RWrist, Point RElbow, Point RShoulder, Point RAnkle,Point RHeel,Point RIndex){

        this.init_L_feet_point = new Point((LAnkle.X+LHeel.X+LIndex.X)/3,(LAnkle.Y+LHeel.Y+LIndex.Y)/3,(LAnkle.rate+LHeel.rate+LIndex.rate)/3);
        this.init_R_feet_point = new Point((RAnkle.X+RHeel.X+RIndex.X)/3,(RAnkle.Y+RHeel.Y+RIndex.Y)/3,(RAnkle.rate+RHeel.rate+RIndex.rate)/3);

    }

    //判断是否达到初始判断条件（开始给它运行检测了）
    public boolean isReady(Point LShoulder,Point LWrist,Point LElbow,Point LAnkle,Point LHeel,Point LIndex,
                           Point RShoulder,Point RWrist,Point RElbow,Point RAnkle,Point RHeel,Point RIndex){
        //手臂高于肩膀
        boolean is_L_ArmAboveShoulder = ArmModule.isArmAboveShoulder(LWrist,LElbow,LShoulder);
        boolean is_R_ArmAboveShoulder = ArmModule.isArmAboveShoulder(RWrist,RElbow,RShoulder);
        boolean armAboveShoulder = (is_L_ArmAboveShoulder || is_R_ArmAboveShoulder);
        //为了测试先写成true
        armAboveShoulder = true;
        //脚部是否移动（这个函数里的对于左右脚的距离判断阈值，需要调整）！！！！！
        boolean is_feet_Moved = FootModule.isFeetMoved(LAnkle,RAnkle,LHeel,RHeel,LIndex,RIndex,feet_L_index_list,feet_R_index_list,100,100);

        if (armAboveShoulder && !is_feet_Moved){
            return true;
        }

        return false;
    }


    //判断是否满足胳膊伸长计数的条件
    public boolean satisfyCondition(Point LWrist,Point LElbow,Point LShoulder, Point LAnkle, Point RWrist,Point RElbow,Point RShoulder, Point RAnkle,Point Nose){
        boolean isArmStright_left = ArmModule.isArmStright(LWrist, LElbow, LShoulder, 160);
        boolean isArmStright_right = ArmModule.isArmStright(RWrist, RElbow, RShoulder, 160);
        //添加手臂与地面垂直判断
        Point floor_left = new Point(LWrist.X,3000,0);
        Point floor_right = new Point(RWrist.X,3000,0);
        boolean isArmStright_floar_left = ArmModule.isArmStright(floor_left, LWrist, LElbow, 160);
        boolean isArmStright_floar_right = ArmModule.isArmStright(floor_right, RWrist, RElbow, 160);
        boolean armStrightFlag=(isArmStright_left || isArmStright_right);
        boolean armStright_floar_Flag=(isArmStright_floar_left || isArmStright_floar_right);



        if(armStrightFlag&&armStright_floar_Flag){
            return true;
        }
        return false;
    }

    //在jumpinghigh调用recover函数的时候，调用此函数将列表和距离总和清空
    public void recover(){
        feet_L_index_list = new LinkedList<>();
        feet_R_index_list = new LinkedList<>();
        feet_L_distance_sum = 0;
        feet_R_distance_sum = 0;
        init_L_feet_point = null;
        init_R_feet_point = null;
    }

    //判断开始起跳过程中，脚部开始移动了没有
    //注意现在的逻辑是左右移动
    public boolean hasMoved(Point LAnkle,Point LHeel,Point LIndex,Point RAnkle,Point RHeel,Point RIndex){
        Point L_feet = new Point((LAnkle.X+LHeel.X+LIndex.X)/3,(LAnkle.Y+LHeel.Y+LIndex.Y)/3,(LAnkle.rate+LHeel.rate+LIndex.rate)/3);
        Point R_feet = new Point((RAnkle.X+RHeel.X+RIndex.X)/3,(RAnkle.Y+RHeel.Y+RIndex.Y)/3,(RAnkle.rate+RHeel.rate+RIndex.rate)/3);
        float L_distance = utils.cal_distance(L_feet,init_L_feet_point);
        float R_distance = utils.cal_distance(R_feet,init_R_feet_point);
        //这个阈值还没定好，先随便写一个
        return L_distance> 300 || R_distance>300;

    }



}
