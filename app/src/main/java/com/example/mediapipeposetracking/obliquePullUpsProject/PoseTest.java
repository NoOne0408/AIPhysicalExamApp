package com.example.mediapipeposetracking.obliquePullUpsProject;


public class PoseTest {
    private static float threshold_small;//胳膊小阈值
    private static int threshold_arm_angle_sub;//胳膊弯曲角度误差值
    private static int  threshold_angle_sub;//用于检测动作完成的角度误差值
    private static int  threshold_body_floor_angle_sub;//用于判断开始动作时与地面角度的误差值
    private static int threshold_arm_body_angle_sub;//用于判断准备动作时胳膊与身体夹角的误差值
    private static float  threshold_nose_dis;
    private static int threshold_leg_angle;//腿部弯曲角度误差值
    private static int threshold_body_leg_angle;//塌腰挺腹角度误差值
    private static boolean is_first_satisfy_angle=false;
    public static int width=1080;
    public static int hight=2340;
    public static String keyMessage="";


    //设置测试阈值
    public PoseTest(int threshold_arm_angle_sub,
                    int threshold_angle_sub,
                    int threshold_body_floor_angle_sub,
                    int threshold_arm_body_angle_sub,
                    int threshold_leg_angle,
                    int threshold_body_leg_angle){

        PoseTest.threshold_arm_angle_sub = threshold_arm_angle_sub;
        PoseTest.threshold_angle_sub =threshold_angle_sub;
        PoseTest.threshold_body_floor_angle_sub=threshold_body_floor_angle_sub;
        PoseTest.threshold_arm_body_angle_sub=threshold_arm_body_angle_sub;
        PoseTest.threshold_leg_angle = threshold_leg_angle;
        PoseTest.threshold_body_leg_angle = threshold_body_leg_angle;
    }

//    public float[]  getVaules(){
//        float[] vaules={ threshold_small,
//                threshold_arm_angle_sub,
//        threshold_angle_sub,
//        threshold_nose_dis,
//                threshold_leg_angle,
//                threshold_body_leg_angle};
//        return vaules;
//    }

    //用于更新胳膊夹角的值，采用前n次最小的角度
    public void updateArmAngle(Point LWrist,Point LElbow,Point LShoulder,
                               Point RWrist,Point RElbow,Point RShoulder){
        //获取初始胳膊角度
        float LArmAngleNew=utils.calAngle(LWrist, LElbow, LElbow, LShoulder);
        float RArmAngleNew=utils.calAngle(RWrist, RElbow, RElbow, RShoulder);

        //判断静态变量：左右胳膊夹角是否需要更新

        if (LArmAngleNew<PoseTest.LArmAngle&&LArmAngleNew>5){
            PoseTest.LArmAngle=LArmAngleNew;
            System.out.println("更新状态：左胳膊夹角"+PoseTest.LArmAngle);
        }

        if (RArmAngleNew<PoseTest.RArmAngle&&RArmAngleNew>5){
            PoseTest.RArmAngle=RArmAngleNew;
            System.out.println("更新状态：右胳膊夹角"+PoseTest.RArmAngle);
        }


    }

    //检测姿势是否正确 1.腿部弯曲  2.身体弯曲  3.脚部移动
    public boolean[] isPoseCorrect(Point LShoulder,Point LHip,Point LKeen,Point LAnkle,Point LHeel,Point LIndex,
                                   Point RShoulder,Point RHip,Point RKeen,Point RAnkle,Point RHeel,Point RIndex){
        boolean leg_bend=false;
        boolean body_bend=false;
        boolean feet_moved;
        //首先检测腿是否伸直
        boolean Lleg=BodyModule.isLegStright(LHip,LKeen,LAnkle, threshold_leg_angle);
        boolean Rleg=BodyModule.isLegStright(RHip,RKeen,RAnkle, threshold_leg_angle);
        //当两条腿都不直时，提醒腿部弯曲
        if(!Lleg&&!Rleg){
            leg_bend=true;
//            System.out.println("请伸直双腿");
        }

        //其次检测身体是否笔直
        boolean Lbody=BodyModule.isBodyStright(LShoulder,LHip,LKeen, threshold_body_leg_angle);
        boolean Rbody=BodyModule.isBodyStright(RShoulder,RHip,RKeen, threshold_body_leg_angle);
        //当两边身体都不直时，提醒弯曲
        if(!Lbody&&!Rbody){
            body_bend=true;
//            System.out.println("请挺直身体");
        }

        //检测脚部是否移动
        feet_moved=FootModule.isFeetMoved(this.LAnklePoint,this.RAnklePoint,LAnkle,RAnkle,
                this.LHeelPoint,this.RHeelPoint,LHeel,RHeel,
                this.LIndexPoint,this.RIndexPoint,LIndex,RIndex,15,15);


        //返回检测结果
        boolean[] result={leg_bend,body_bend,feet_moved};
        return result;
    }


    //检测是否到达准备状态,当手臂与躯干夹角为90度左右，胳膊伸直(角度小于阈值)，躯干与地面夹角小于45度左右时视作”准备开始状态“
    public boolean isStartPose(Point LShoulder,Point LWrist,Point LElbow,Point LAnkle,
                               Point RShoulder,Point RWrist,Point RElbow,Point RAnkle){

        //手臂直？
        float threshold_arm_angle_left = this.LArmAngle + PoseTest.threshold_arm_angle_sub;
        float threshold_arm_angle_right = this.RArmAngle + PoseTest.threshold_arm_angle_sub;
        System.out.println("左右胳膊角度阈值："+threshold_arm_angle_left+"  "+threshold_arm_angle_right);
        boolean isArmStright_left = ArmModule.isArmStright(LWrist, LElbow, LShoulder, threshold_arm_angle_left);
        boolean isArmStright_right = ArmModule.isArmStright(RWrist, RElbow, RShoulder, threshold_arm_angle_right);
        //tag1
        boolean armStrightFlag=(isArmStright_left || isArmStright_right);


        //获取双臂与躯干角度
        float angle_body_arm_left = utils.calAngle(LWrist, LShoulder, LShoulder, LAnkle);
        float angle_body_arm_right = utils.calAngle(RWrist, RShoulder, RShoulder, RAnkle);

        float upperBoundLeft=this.LArmBodyAngle+PoseTest.threshold_arm_body_angle_sub;
        float lowerBoundLeft=this.LArmBodyAngle-PoseTest.threshold_arm_body_angle_sub;

        float upperBoundRight=this.RArmBodyAngle+PoseTest.threshold_arm_body_angle_sub;
        float lowerBoundRight=this.RArmBodyAngle-PoseTest.threshold_arm_body_angle_sub;
        System.out.println("左右胳膊-躯干角度阈值："+this.LArmBodyAngle+"  "+this.RArmBodyAngle);
//        System.out.println("threshold_arm_body_angle_sub："+threshold_arm_body_angle_sub);


        //tag2
        boolean armAngleFlag=((angle_body_arm_left>=lowerBoundLeft&&angle_body_arm_left<=upperBoundLeft)||
                (angle_body_arm_right>=lowerBoundRight&&angle_body_arm_right<=upperBoundRight));


        //身体地面角度合格？
        float angle_body_floor_left = BodyModule.body_floor_angle(LShoulder, LAnkle);
        float angle_body_floor_right = BodyModule.body_floor_angle(RShoulder, RAnkle);
        //tag3
        float upperBoundLeftBody=this.LBodyFloorAngle+PoseTest.threshold_body_floor_angle_sub;
        float upperBoundRightBody=this.RBodyFloorAngle+PoseTest.threshold_body_floor_angle_sub;
        System.out.println("左右地面-躯干角度阈值："+upperBoundLeftBody+"  "+upperBoundRightBody);
//        System.out.println("threshold_body_floor_angle_sub："+threshold_body_floor_angle_sub);

        boolean bodyFlag= angle_body_floor_left <=upperBoundLeftBody  || angle_body_floor_right <= upperBoundRightBody;
        if (!bodyFlag)PoseTest.keyMessage="身体与地面夹角过大";

//        System.out.println("isArmStright_left: "+isArmStright_left);
//        System.out.println("isArmStright_right: "+isArmStright_right);
//        System.out.println("angle_body_arm_left: "+angle_body_arm_left);
//        System.out.println("angle_body_arm_right: "+angle_body_arm_right);
//        System.out.println("angle_body_floor_left: "+angle_body_floor_left);
//        System.out.println("angle_body_floor_right: "+angle_body_floor_right);

        boolean commonCondition=isReady(LShoulder,LWrist,LElbow,LAnkle, RShoulder,RWrist,RElbow,RAnkle,30);
        System.out.println("stright,angle,anglefloor,commonCondition ？  "+armStrightFlag+" "+armAngleFlag+" "+bodyFlag+" "+commonCondition);

        if(armStrightFlag && armAngleFlag && bodyFlag && commonCondition){
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

        //获取手腕和脚踝与地面构成的夹角
        this.angle_wrist_ankle_left=BodyModule.body_floor_angle(LWrist, LAnkle);
        System.out.println("初始状态：左手腕 脚踝夹角"+angle_wrist_ankle_left);
        this.angle_wrist_ankle_right=BodyModule.body_floor_angle(RWrist, RAnkle);
        System.out.println("初始状态：右手腕 脚踝夹角"+angle_wrist_ankle_right);

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

        //获取初始身体与地面夹角
        this.LBodyFloorAngle=BodyModule.body_floor_angle(LShoulder, LAnkle);
        this.RBodyFloorAngle=BodyModule.body_floor_angle(RShoulder,RAnkle);
        System.out.println("初始状态：左身体与地面角度"+LBodyFloorAngle);
        System.out.println("初始状态：右身体与地面角度"+RBodyFloorAngle);

    }

    //获取准备状态的关键信息
    public float[] getReadyMessage(Point LShoulder,Point LWrist,Point LElbow,Point LAnkle,
                                   Point RShoulder,Point RWrist,Point RElbow,Point RAnkle){
        //胳膊角度
        float angle_arm_left = utils.calAngle(LWrist,LElbow,LElbow,LShoulder);
        float angle_arm_right = utils.calAngle(RWrist,RElbow,RElbow,RShoulder);

        //胳膊身体角度
        float angle_body_arm_left = utils.calAngle(LWrist, LShoulder, LShoulder, LAnkle);
        float angle_body_arm_right = utils.calAngle(RWrist, RShoulder, RShoulder, RAnkle);

        //身体地面角度
        float angle_body_floor_left = BodyModule.body_floor_angle(LShoulder, LAnkle);
        float angle_body_floor_right = BodyModule.body_floor_angle(RShoulder, RAnkle);

        float[] result={angle_arm_left,angle_arm_right,angle_body_arm_left,angle_body_arm_right,angle_body_floor_left,angle_body_floor_right};
        return result;


    }

    //判断是否达到准备判断基本条件/开始条件
    public boolean isReady(Point LShoulder,Point LWrist,Point LElbow,Point LAnkle,
                           Point RShoulder,Point RWrist,Point RElbow,Point RAnkle,float threshold_arm_angle){
        //胳膊伸直 tag1
        boolean isArmStright_left = ArmModule.isArmStright(LWrist, LElbow, LShoulder, threshold_arm_angle);
        boolean isArmStright_right = ArmModule.isArmStright(RWrist, RElbow, RShoulder, threshold_arm_angle);
        boolean armStrightFlag=(isArmStright_left || isArmStright_right);

        //胳膊和身体角度 tag2
        float angle_body_arm_left = utils.calAngle(LWrist, LShoulder, LShoulder, LAnkle);
        float angle_body_arm_right = utils.calAngle(RWrist, RShoulder, RShoulder, RAnkle);
        boolean armAngleFlag=((angle_body_arm_left>75&&angle_body_arm_left<105)||
                (angle_body_arm_right>75&&angle_body_arm_right<105));
        System.out.println("ready arm_body:"+angle_body_arm_left+" "+angle_body_arm_right);

        //身体地面角度合格 tag3
        float angle_body_floor_left = BodyModule.body_floor_angle(LShoulder, LAnkle);
        float angle_body_floor_right = BodyModule.body_floor_angle(RShoulder, RAnkle);
        boolean bodyFlag= angle_body_floor_left <65 && angle_body_floor_right < 65;
        System.out.println("ready body:"+angle_body_floor_left+" "+angle_body_floor_right);

        return armStrightFlag && armAngleFlag && bodyFlag;
    }

    //判断是否达到完成基本条件
    public boolean isFinish(Point LShoulder,Point LHip,Point LKeen,Point LAnkle,
                            Point RShoulder,Point RHip,Point RKeen,Point RAnkle){
        //获取腕踝角度
        float angle_body_floor_left = BodyModule.body_floor_angle(LShoulder, LAnkle);
        float angle_body_floor_right = BodyModule.body_floor_angle(RShoulder, RAnkle);
        //判断腕踝脚是否满足要求
        boolean bodyFloorFlag=angle_body_floor_left>70 ||angle_body_floor_right>70;

        //首先检测腿是否伸直
        boolean Lleg=BodyModule.isLegStright(LHip,LKeen,LAnkle, 30);
        boolean Rleg=BodyModule.isLegStright(RHip,RKeen,RAnkle, 30);
        boolean legFlag=Lleg || Rleg;


        //其次检测身体是否笔直
        boolean Lbody=BodyModule.isBodyStright(LShoulder,LHip,LKeen, 25);
        boolean Rbody=BodyModule.isBodyStright(RShoulder,RHip,RKeen, 25);
        //判断身体腿部夹角是否满足要求
        boolean bodyLegFlag=Lbody ||Rbody;

        return bodyFloorFlag && legFlag && bodyLegFlag;
    }

    //判断是否满足条件1
    public boolean satisfyCondition(Point LWrist,Point LElbow,Point LShoulder, Point LAnkle, Point RWrist,Point RElbow,Point RShoulder, Point RAnkle,Point Nose){
//        System.out.println("初始状态：左手腕 脚踝夹角"+angle_wrist_ankle_left);
//        System.out.println("初始状态：右手腕 脚踝夹角"+angle_wrist_ankle_right);

        //获取腕踝角度
        float angle_body_floor_left = BodyModule.body_floor_angle(LShoulder, LAnkle);
        float angle_body_floor_right = BodyModule.body_floor_angle(RShoulder, RAnkle);

//        System.out.println("当前状态：左手腕 脚踝夹角"+angle_body_floor_left);
//        System.out.println("当前状态：右手腕 脚踝夹角"+angle_body_floor_right);

        float sub_angle_left=Math.abs(this.angle_wrist_ankle_left-angle_body_floor_left);
        float sub_angle_right=Math.abs(this.angle_wrist_ankle_right-angle_body_floor_right);

        System.out.println("当前状态：左手腕脚踝夹角差值:"+sub_angle_left);
        System.out.println("当前状态：右手腕脚踝夹角差值:"+sub_angle_right);

        if(sub_angle_left<PoseTest.threshold_angle_sub ||sub_angle_right<PoseTest.threshold_angle_sub){
            //整个测试时间内第一次满足角度条件时动态更新距离阈值

            if (!is_first_satisfy_angle){
                //动态设置手腕 鼻子距离阈值，取左右平均值
                float dis_LWrist_Nose = utils.cal_distance(LWristPoint, Nose);
                float dis_RWrist_Nose = utils.cal_distance(RWristPoint, Nose);
                PoseTest.threshold_nose_dis=(dis_LWrist_Nose+dis_RWrist_Nose)/2;
                System.out.println("鼻子距离动态设置为:"+PoseTest.threshold_nose_dis);

                //动态设置胳膊周长的小阈值，取左右较小值
                float dis_left = utils.cal_distance(LWrist, LShoulder);
                float dis_right = utils.cal_distance(RWrist, RShoulder);
                PoseTest.threshold_small=Math.min(dis_left,dis_right);
                System.out.println("小阈值距离动态设置为:"+PoseTest.threshold_small);
                this.circumference_left = this.arm_length_left + PoseTest.threshold_small;
                this.circumference_right = this.arm_length_right + PoseTest.threshold_small;
            }

            is_first_satisfy_angle=true;

            //判断是否满足两个辅助条件
            boolean condition_circumference_satisfy=this.satisfyCondition_circumference(LWrist,LElbow,LShoulder,RWrist,RElbow,RShoulder);
            boolean condition_dis_nose_satisfy=this.satisfyCondition_dis_nose(Nose);

            //如果主条件不满足，但是辅助条件都满足，则提醒下巴需要过杠
            boolean additionCondition=condition_circumference_satisfy||condition_dis_nose_satisfy;

            System.out.println("辅助条件1 2 ： "+condition_circumference_satisfy+" "+condition_dis_nose_satisfy);
            if (additionCondition){
                return true;
            }
        }
        //判断是否满足两个辅助条件
        boolean condition_circumference_satisfy=this.satisfyCondition_circumference(LWrist,LElbow,LShoulder,RWrist,RElbow,RShoulder);
        boolean condition_dis_nose_satisfy=this.satisfyCondition_dis_nose(Nose);

        //如果主条件不满足，但是辅助条件都满足，则提醒下巴需要过杠
        boolean additionCondition=condition_circumference_satisfy||condition_dis_nose_satisfy;

        if (additionCondition && (sub_angle_left<20 ||sub_angle_right<20)){
            System.out.println("下巴需要过杠！");
            PoseTest.keyMessage="下巴需要过杠！";
            return true;
        }
        return false;
    }

    //判断是否满足条件3
    public boolean satisfyCondition_circumference(Point LWrist, Point LElbow, Point LShoulder,
                                                  Point RWrist, Point RElbow, Point RShoulder){
        //计算左胳膊周长
        float dis1_left = utils.cal_distance(LWrist, LElbow);
        float dis2_left = utils.cal_distance(LWrist, LShoulder);
        float dis3_left = utils.cal_distance(LElbow, LShoulder);
        float average_left=dis1_left+dis2_left+dis3_left;

        //计算右胳膊周长
        float dis1_right = utils.cal_distance(RWrist, RElbow);
        float dis2_right = utils.cal_distance(RWrist, RShoulder);
        float dis3_right = utils.cal_distance(RElbow, RShoulder);
        float average_right=dis1_right+dis2_right+dis3_right;

        System.out.println("周长左："+average_left);
        System.out.println("周长右："+average_right);


        if (average_left< this.circumference_left ||average_right< this.circumference_right){
            return true;
        }
        return false;
    }


    // 判断是否满足条件4_2,鼻子与手腕距离
    public boolean satisfyCondition_dis_nose(Point Nose){
        float dis_LWrist_Nose = utils.cal_distance(LWristPoint, Nose);
        float dis_RWrist_Nose = utils.cal_distance(RWristPoint, Nose);
        System.out.println("鼻子 left距离:"+dis_LWrist_Nose);
        System.out.println("鼻子 right距离:"+dis_RWrist_Nose);
        if (dis_LWrist_Nose<PoseTest.threshold_nose_dis && dis_RWrist_Nose<PoseTest.threshold_nose_dis){
            return true;
        }
        return false;
    }
}
