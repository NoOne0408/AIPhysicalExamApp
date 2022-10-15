package com.example.mediapipeposetracking.doubleBarflexionProject;

import com.google.mediapipe.formats.proto.LandmarkProto;


public class BodyModule {

    public static Point[] getBodyKeyPoints(LandmarkProto.NormalizedLandmarkList landmarkList){
        //左右肩
        LandmarkProto.NormalizedLandmark LShoulderPoint=landmarkList.getLandmarkList().get(11);
        LandmarkProto.NormalizedLandmark RShoulderPoint=landmarkList.getLandmarkList().get(12);
        Point LShoulder = new Point(LShoulderPoint.getX()*PoseTest.width,LShoulderPoint.getY()*PoseTest.hight,LShoulderPoint.getVisibility());
        Point RShoulder = new Point(RShoulderPoint.getX()*PoseTest.width,RShoulderPoint.getY()*PoseTest.hight,RShoulderPoint.getVisibility());

        //左右髋骨
        LandmarkProto.NormalizedLandmark LHipPoint=landmarkList.getLandmarkList().get(23);
        LandmarkProto.NormalizedLandmark RHipPoint=landmarkList.getLandmarkList().get(24);
        Point LHip = new Point(LHipPoint.getX()*PoseTest.width,LHipPoint.getY()*PoseTest.hight,LHipPoint.getVisibility());
        Point RHip = new Point(RHipPoint.getX()*PoseTest.width,RHipPoint.getY()*PoseTest.hight,RHipPoint.getVisibility());


        //左右膝盖
        LandmarkProto.NormalizedLandmark LKeenPoint=landmarkList.getLandmarkList().get(25);
        LandmarkProto.NormalizedLandmark RKeenPoint=landmarkList.getLandmarkList().get(26);
        Point LKeen = new Point(LKeenPoint.getX()*PoseTest.width,LKeenPoint.getY()*PoseTest.hight,LKeenPoint.getVisibility());
        Point RKeen = new Point(RKeenPoint.getX()*PoseTest.width,RKeenPoint.getY()*PoseTest.hight,RKeenPoint.getVisibility());



        //左右脚踝
        LandmarkProto.NormalizedLandmark LAnklePoint=landmarkList.getLandmarkList().get(27);
        LandmarkProto.NormalizedLandmark RAnklePoint=landmarkList.getLandmarkList().get(28);
        Point LAnkle = new Point(LAnklePoint.getX()*PoseTest.width,LAnklePoint.getY()*PoseTest.hight,LAnklePoint.getVisibility());
        Point RAnkle = new Point(RAnklePoint.getX()*PoseTest.width,RAnklePoint.getY()*PoseTest.hight,RAnklePoint.getVisibility());




        Point[] body_points={RShoulder,LShoulder,RHip,LHip,RKeen,LKeen,RAnkle,LAnkle};
        return body_points;
    }

    //检测腿部是否挺直
    public static boolean isLegStright(Point Hip,Point Keen,Point Ankle,int threshold_leg_angle){
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
        //假设所需关键点已经全部检测到
        float angle = utils.calAngle(Shoulder,Hip,Hip,Knee);
        if(angle>threshold_body_leg_angle){
            return true;
        }
        return  false;
    }

}

