package com.example.mediapipeposetracking.obliquePullUpsProject;

import com.google.mediapipe.formats.proto.LandmarkProto;

public class FootModule {
    public static Point[] getFootKeyPoints(LandmarkProto.NormalizedLandmarkList landmarkList){
        //左右脚根
        LandmarkProto.NormalizedLandmark LHeelPoint=landmarkList.getLandmarkList().get(29);
        LandmarkProto.NormalizedLandmark RHeelPoint=landmarkList.getLandmarkList().get(30);
        Point LHeel=new Point(LHeelPoint.getX()*PoseTest.width,LHeelPoint.getY()*PoseTest.hight,LHeelPoint.getVisibility());
        Point RHeel=new Point(RHeelPoint.getX()*PoseTest.width,RHeelPoint.getY()*PoseTest.hight,RHeelPoint.getVisibility());

        //左右脚趾
        LandmarkProto.NormalizedLandmark LIndexPoint=landmarkList.getLandmarkList().get(31);
        LandmarkProto.NormalizedLandmark RIndexPoint=landmarkList.getLandmarkList().get(32);
        Point LIndex=new Point(LIndexPoint.getX()*PoseTest.width,LIndexPoint.getY()*PoseTest.hight,LIndexPoint.getVisibility());
        Point RIndex=new Point(RIndexPoint.getX()*PoseTest.width,RIndexPoint.getY()*PoseTest.hight,RIndexPoint.getVisibility());

        Point[] foot_points={RHeel,LHeel,RIndex,LIndex};
        return foot_points;
    }

    //判断双脚是否移动
    public static boolean isFeetMoved(Point LAnklePoint,Point RAnklePoint, Point LAnkle,Point RAnkle,
                              Point LHeelPoint,Point RHeelPoint, Point LHeel,Point RHeel,
                              Point LIndexPoint,Point RIndexPoint, Point LIndex,Point RIndex,
                              float xthreshold,float ythreshold){
        boolean flag1=false;
        boolean flag2=false;
        boolean flag3=false;

        //左右踝x，y差值
        float ankle_x_left_sub=Math.abs(LAnkle.X-LAnklePoint.X);
        float ankle_x_right_sub=Math.abs(RAnkle.X-RAnklePoint.X);

        float ankle_y_left_sub=Math.abs(LAnkle.Y-LAnklePoint.Y);
        float ankle_y_right_sub=Math.abs(RAnkle.Y-RAnklePoint.Y);

        if((ankle_x_left_sub>xthreshold && ankle_x_right_sub>xthreshold)&&
                (ankle_y_left_sub>ythreshold && ankle_y_right_sub>ythreshold)){
            flag1=true;
        }

        //左右脚跟x，y差值
        float heel_x_left_sub=Math.abs(LHeel.X-LHeelPoint.X);
        float heel_x_right_sub=Math.abs(RHeel.X-RHeelPoint.X);

        float heel_y_left_sub=Math.abs(LHeel.Y-LHeelPoint.Y);
        float heel_y_right_sub=Math.abs(RHeel.Y-RHeelPoint.Y);
        if((heel_x_left_sub>xthreshold && heel_x_right_sub>xthreshold)&&
                (heel_y_left_sub>ythreshold && heel_y_right_sub>ythreshold)){
            flag2=true;
        }

        //左右脚趾x，y差值
        float index_x_left_sub=Math.abs(LIndex.X-LIndexPoint.X);
        float index_x_right_sub=Math.abs(RIndex.X-RIndexPoint.X);

        float index_y_left_sub=Math.abs(LIndex.Y-LIndexPoint.Y);
        float index_y_right_sub=Math.abs(RIndex.Y-RIndexPoint.Y);
        if((index_x_left_sub>xthreshold && index_x_right_sub>xthreshold)&&
                (index_y_left_sub>ythreshold && index_y_right_sub>ythreshold)){
            flag3=true;
        }

        if((flag1 && flag2 &&flag3)|| (flag1 && flag2)|| (flag1 && flag3)|| (flag2 && flag3))return true;


        return false;
    }
}
