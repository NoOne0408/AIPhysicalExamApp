package com.example.mediapipeposetracking.doubleBarflexionProject;



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

        this.arm_length_left=utils.cal_distance(LWrist,LShoulder);
        System.out.println("初始状态：左胳膊长"+ arm_length_left);

        this.arm_length_right=utils.cal_distance(RWrist,RShoulder);
        System.out.println("初始状态：右胳膊长"+ arm_length_right);

        //获取腕关节初始点
        this.LWristPoint = LWrist;
        this.RWristPoint = RWrist;

        //获取踝关节初始点
        this.LAnklePoint = LAnkle;
        this.RAnklePoint = RAnkle;

        //获取脚部节点
        this.LHeelPoint=LHeel;
        this.RHeelPoint=RHeel;
        this.LIndexPoint=LIndex;
        this.RIndexPoint=RIndex;


        //获取初始胳膊角度
        PoseTest.LArmAngle=utils.calAngle(LWrist, LElbow, LElbow, LShoulder);
        PoseTest.RArmAngle=utils.calAngle(RWrist, RElbow, RElbow, RShoulder);
        System.out.println("初始状态：左胳膊夹角"+LArmAngle);
        System.out.println("初始状态：右胳膊夹角"+RArmAngle);

        //获取初始胳膊与身体夹角
        //获取双臂与躯干角度
        this.LArmBodyAngle=utils.calAngle(LWrist,LShoulder,LShoulder,LAnkle);
        this.RArmBodyAngle=utils.calAngle(RWrist,RShoulder,RShoulder,RAnkle);
        System.out.println("初始状态：左胳膊身体角度"+LArmBodyAngle);
        System.out.println("初始状态：右胳膊身体角度"+RArmBodyAngle);


    }

    //判断是否达到初始判断条件（开始给它运行检测了）
    public boolean isReady(Point LShoulder,Point LWrist,Point LElbow,Point LAnkle,
                           Point RShoulder,Point RWrist,Point RElbow,Point RAnkle){
        //手臂前端部分在双杠上伸直 tag1
        Point floor_left = new Point(LWrist.X,3000,0);
        Point floor_right = new Point(RWrist.X,3000,0);
        boolean isArmStright_left = ArmModule.isArmStright(floor_left, LWrist, LElbow, 160);
        boolean isArmStright_right = ArmModule.isArmStright(floor_right, RWrist, RElbow, 160);
        boolean armStrightFlag=(isArmStright_left || isArmStright_right);

        //胳膊呈现90°
        boolean isArm90_left = ArmModule.isArm90(LWrist, LElbow, LShoulder, 100);
        boolean isArm90_right = ArmModule.isArm90(RWrist, RElbow, RShoulder, 100);
        boolean arm90Flag = isArm90_left || isArm90_right;

        if (armStrightFlag && arm90Flag){
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



}