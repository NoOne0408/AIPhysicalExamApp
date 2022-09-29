package com.example.mediapipeposetracking.obliquePullUpsProject;

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
        //直线L1,L2斜率
        float k1 = cal_k(p1, p2);
        float k2 = cal_k(p3, p4);
//        System.out.println("k1 k2: "+k1+"  "+k2);

        // 求出方向向量
        Point L1 = new Point(1, k1,0);
        Point L2 = new Point(1, k2,0);
//        System.out.println("L1 : "+L1.X+"  "+L1.Y);
//        System.out.println("L2 : "+L2.X+"  "+L2.Y);

        //求模长
        float L1_len = (float) Math.sqrt(L1.X* L1.X+ L1.Y* L1.Y);
        float L2_len = (float) Math.sqrt(L2.X* L2.X+ L2.Y* L2.Y);
//        System.out.println("L1len L2len : "+L1_len+"  "+L2_len);

        //算角度
        double cos=(L1.X*L2.X+L1.Y*L2.Y )/( L1_len * L2_len);
//        System.out.println("cos : "+cos);
        float pi_angle= (float) Math.acos(cos);
//        System.out.println("pi : "+pi_angle);
        float angle= (float) (pi_angle*180/Math.PI);
//        System.out.println("angle : "+angle);
        return angle;
    }


}
