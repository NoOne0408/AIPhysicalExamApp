package com.example.mediapipeposetracking.doubleBarflexionProject;

import com.google.mediapipe.formats.proto.LandmarkProto;


public class BodyModule {

    public static Point[] getBodyKeyPoints(LandmarkProto.NormalizedLandmarkList landmarkList){
        //左右肩
        LandmarkProto.NormalizedLandmark LShoulderPoint=landmarkList.getLandmarkList().get(11);
        LandmarkProto.NormalizedLandmark RShoulderPoint=landmarkList.getLandmarkList().get(12);
        System.out.println("肩膀z1: "+LShoulderPoint.getZ());
        System.out.println("肩膀z2: "+RShoulderPoint.getZ());
        System.out.println("肩膀visibility: "+LShoulderPoint.getVisibility());
        System.out.println("肩膀Presence: "+LShoulderPoint.getPresence());
        Point LShoulder = new Point(LShoulderPoint.getX()*PoseTest.width,LShoulderPoint.getY()*PoseTest.hight,LShoulderPoint.getPresence());
        Point RShoulder = new Point(RShoulderPoint.getX()*PoseTest.width,RShoulderPoint.getY()*PoseTest.hight,RShoulderPoint.getPresence());

        //左右髋骨
        LandmarkProto.NormalizedLandmark LHipPoint=landmarkList.getLandmarkList().get(23);
        LandmarkProto.NormalizedLandmark RHipPoint=landmarkList.getLandmarkList().get(24);
        System.out.println("髋骨visibility: "+LHipPoint.getVisibility());
        System.out.println("髋骨Presence: "+LHipPoint.getPresence());
        Point LHip = new Point(LHipPoint.getX()*PoseTest.width,LHipPoint.getY()*PoseTest.hight,LHipPoint.getPresence());
        Point RHip = new Point(RHipPoint.getX()*PoseTest.width,RHipPoint.getY()*PoseTest.hight,RHipPoint.getPresence());


        //左右膝盖
        LandmarkProto.NormalizedLandmark LKeenPoint=landmarkList.getLandmarkList().get(25);
        LandmarkProto.NormalizedLandmark RKeenPoint=landmarkList.getLandmarkList().get(26);
        System.out.println("膝盖visibility: "+LKeenPoint.getVisibility());
        System.out.println("膝盖Presence: "+LKeenPoint.getPresence());
        Point LKeen = new Point(LKeenPoint.getX()*PoseTest.width,LKeenPoint.getY()*PoseTest.hight,LKeenPoint.getPresence());
        Point RKeen = new Point(RKeenPoint.getX()*PoseTest.width,RKeenPoint.getY()*PoseTest.hight,RKeenPoint.getPresence());



        //左右脚踝
        LandmarkProto.NormalizedLandmark LAnklePoint=landmarkList.getLandmarkList().get(27);
        LandmarkProto.NormalizedLandmark RAnklePoint=landmarkList.getLandmarkList().get(28);
        Point LAnkle = new Point(LAnklePoint.getX()*PoseTest.width,LAnklePoint.getY()*PoseTest.hight,LAnklePoint.getPresence());
        Point RAnkle = new Point(RAnklePoint.getX()*PoseTest.width,RAnklePoint.getY()*PoseTest.hight,RAnklePoint.getPresence());




        Point[] body_points={RShoulder,LShoulder,RHip,LHip,RKeen,LKeen,RAnkle,LAnkle};
        return body_points;
    }

    //检测腿部是否挺直
    public static boolean isLegStright(Point Hip,Point Keen,Point Ankle,int threshold_leg_angle){
        if(Hip.rate == 0.0 || Keen.rate == 0.0 || Ankle.rate == 0.0){
            return false;
        }
        //假设所需关键点已经全部检测到
//        System.out.println("threshold_leg_angle:"+threshold_leg_angle);
        float angle = utils.calAngle(Hip,Keen,Keen,Ankle);

        if(angle>threshold_leg_angle){
            return true;
        }
        return false;
    }

    //检测身体是否挺直
    public static boolean isBodyStright(Point Shoulder,Point Hip,Point Knee,int threshold_body_leg_angle){
        if(Hip.rate == 0.0 || Shoulder.rate == 0.0 || Knee.rate == 0.0){
            return false;
        }
        //假设所需关键点已经全部检测到
        float angle = utils.calAngle(Shoulder,Hip,Hip,Knee);
        if(angle>threshold_body_leg_angle){
            return true;
        }
        return  false;
    }

}
