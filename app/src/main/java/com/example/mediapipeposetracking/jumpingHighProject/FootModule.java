package com.example.mediapipeposetracking.jumpingHighProject;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.LinkedList;

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
    public static boolean isFeetMoved(Point LAnkle,Point RAnkle,
                              Point LHeel,Point RHeel,
                              Point LIndex,Point RIndex,
                                      LinkedList<Point> feet_L_index_list,LinkedList<Point> feet_R_index_list,
                              float l_distancwe,float r_distance){

        //根据三个点，合成一个新的脚部中心坐标点
        Point L_feet = new Point((LAnkle.X+LHeel.X+LIndex.X)/3,(LAnkle.Y+LHeel.Y+LIndex.Y)/3,(LAnkle.rate+LHeel.rate+LIndex.rate)/3);
        Point R_feet = new Point((RAnkle.X+RHeel.X+RIndex.X)/3,(RAnkle.Y+RHeel.Y+RIndex.Y)/3,(RAnkle.rate+RHeel.rate+RIndex.rate)/3);

        //判断坐标列表是不是存满了
        int lenList = feet_L_index_list.size();
        //如果还没满
        if(lenList<PoseTest.feet_list_maxSize){
            //容器为空
            if(feet_L_index_list.isEmpty()){
                feet_L_index_list.add(L_feet);
                feet_R_index_list.add(R_feet);
                return true;//true代表移动了
            }
            //容器不为空
            else{
                Point L_history_feet = feet_L_index_list.getFirst();
                Point R_history_feet = feet_R_index_list.getFirst();
                float L_plus = (float)Math.sqrt(Math.abs((L_feet.X-L_history_feet.X)*(L_feet.X-L_history_feet.X)+(L_feet.Y-L_history_feet.Y)*(L_feet.Y-L_history_feet.Y)));
                float R_plus = (float)Math.sqrt(Math.abs((R_feet.X-R_history_feet.X)*(R_feet.X-R_history_feet.X)+(R_feet.Y-R_history_feet.Y)*(R_feet.Y-R_history_feet.Y)));

                PoseTest.feet_L_distance_sum += L_plus;
                PoseTest.feet_R_distance_sum += R_plus;

                feet_L_index_list.addFirst(L_feet);
                feet_R_index_list.addFirst(R_feet);
                return true;
            }
        }
        //如果已经满了
        else{
            //在列表头部增加新的坐标点，移动距离总和加上差值
            Point L_fist_history_feet = feet_L_index_list.getFirst();
            Point R_first_history_feet = feet_R_index_list.getFirst();
            float L_plus = (float)Math.sqrt(Math.abs((L_feet.X-L_fist_history_feet.X)*(L_feet.X-L_fist_history_feet.X)+(L_feet.Y-L_fist_history_feet.Y)*(L_feet.Y-L_fist_history_feet.Y)));
            float R_plus = (float)Math.sqrt(Math.abs((R_feet.X-R_first_history_feet.X)*(R_feet.X-R_first_history_feet.X)+(R_feet.Y-R_first_history_feet.Y)*(R_feet.Y-R_first_history_feet.Y)));

            PoseTest.feet_L_distance_sum += L_plus;
            PoseTest.feet_R_distance_sum += R_plus;

            feet_L_index_list.addFirst(L_feet);
            feet_R_index_list.addFirst(R_feet);

            //在列表尾部删掉旧的坐标点，移动距离总和减去差值
            Point L_last_history_feet = feet_L_index_list.getLast();
            Point R_last_history_feet = feet_R_index_list.getLast();
            feet_L_index_list.removeLast();
            feet_R_index_list.removeLast();
            Point L_last_feet = feet_L_index_list.getLast();
            Point R_last_feet = feet_R_index_list.getLast();
            float L_minus = (float)Math.sqrt(Math.abs((L_last_feet.X-L_last_history_feet.X)*(L_last_feet.X-L_last_history_feet.X)+(L_last_feet.Y-L_last_history_feet.Y)*(L_last_feet.Y-L_last_history_feet.Y)));
            float R_minus = (float)Math.sqrt(Math.abs((R_last_feet.X-R_last_history_feet.X)*(R_last_feet.X-R_last_history_feet.X)+(R_last_feet.Y-R_last_history_feet.Y)*(R_last_feet.Y-R_last_history_feet.Y)));

            PoseTest.feet_L_distance_sum -= L_minus;
            PoseTest.feet_R_distance_sum -= R_minus;
        }

        if(PoseTest.feet_L_distance_sum > l_distancwe && PoseTest.feet_R_distance_sum>r_distance){
            return true;
        }
        else{
            return false;
        }

    }
}
