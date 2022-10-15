package com.example.mediapipeposetracking.doubleBarflexionProject;


import com.google.common.collect.Lists;

import java.util.List;

public class utils {
    // 由于输出坐标与直角坐标轴不符合，对纵坐标进行简单转换，用于后续数学计算
    public static Point coordChange(Point keypoint, float yLength){
        float y_new = yLength - keypoint.Y;
        return new Point(keypoint.X,y_new,keypoint.rate);
    }



    //计算直线斜率
    public static float cal_k(Point point1, Point point2){
        //设置y坐标范围为360
        Point newPoint1 = coordChange(point1, PoseTest.hight);
        Point newPoint2 = coordChange(point2, PoseTest.hight);
//    #     print("len:",imageToProcess.shape[0])
        float y_len = newPoint1.Y - newPoint2.Y;
        float x_len = newPoint1.X-newPoint2.X;
        float k = y_len / x_len;
        return k;
    }



    //计算两点之间距离
    public static float cal_distance(Point point1,Point point2){
        // 转换y坐标范围为360
        Point p1 = coordChange(point1, 360);
        Point p2 = coordChange(point2, 360);
        float dis =(float) Math.sqrt((p1.X-p2.X)*(p1.X-p2.X)+(p1.Y- p2.Y)*(p1.Y- p2.Y));
        return dis;
    }




    //通过四个点计算角度
    public static float calAngle(Point p1, Point p2, Point p3, Point p4){
        List<Float> l1= Lists.newArrayList(p1.X-p2.X,p2.Y-p1.Y);
        List<Float> l2= Lists.newArrayList(p4.X-p3.X,p3.Y-p4.Y);
        double x1=0,x2=0,y1=0,y2=0;
        double cosine = 0;
        // 计算手臂角度
        x1 = l1.get(0);
        y1 = l1.get(1);
        x2 = l2.get(0);
        y2 = l2.get(1);
        cosine= (x1 * x2 + y1 * y2) / (Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2 + y2 * y2));
        float angle=(float)Math.toDegrees(Math.acos(cosine));
//        System.out.println("angle : "+angle);
        return angle;
    }


}
