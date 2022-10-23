package com.example.mediapipeposetracking.pullUpsProject;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PoseLandmark {
    public static final int NOSE = 0;
    public static final int LEFT_EYE_INNER = 1;
    public static final int LEFT_EYE = 2;
    public static final int LEFT_EYE_OUTER = 3;
    public static final int RIGHT_EYE_INNER = 4;
    public static final int RIGHT_EYE = 5;
    public static final int RIGHT_EYE_OUTER = 6;
    public static final int LEFT_EAR = 7;
    public static final int RIGHT_EAR = 8;
    public static final int LEFT_MOUTH = 9;
    public static final int RIGHT_MOUTH = 10;
    public static final int LEFT_SHOULDER = 11;
    public static final int RIGHT_SHOULDER = 12;
    public static final int LEFT_ELBOW = 13;
    public static final int RIGHT_ELBOW = 14;
    public static final int LEFT_WRIST = 15;
    public static final int RIGHT_WRIST = 16;
    public static final int LEFT_PINKY = 17;
    public static final int RIGHT_PINKY = 18;
    public static final int LEFT_INDEX = 19;
    public static final int RIGHT_INDEX = 20;
    public static final int LEFT_THUMB = 21;
    public static final int RIGHT_THUMB = 22;
    public static final int LEFT_HIP = 23;
    public static final int RIGHT_HIP = 24;
    public static final int LEFT_KNEE = 25;
    public static final int RIGHT_KNEE = 26;
    public static final int LEFT_ANKLE = 27;
    public static final int RIGHT_ANKLE = 28;
    public static final int LEFT_HEEL = 29;
    public static final int RIGHT_HEEL = 30;
    public static final int LEFT_FOOT_INDEX = 31;
    public static final int RIGHT_FOOT_INDEX = 32;
    @PoseLandmark.LandmarkType
    private final int zza;
    private final PointF zzb;
    private final float zzc;

    PoseLandmark(@PoseLandmark.LandmarkType int var1, @NonNull PointF var2, float var3) {
        this.zza = var1;
        this.zzb = var2;
        this.zzc = var3;
    }

    @PoseLandmark.LandmarkType
    public int getLandmarkType() {
        return this.zza;
    }

    @NonNull
    public PointF getPosition() {
        return this.zzb;
    }

    public float getInFrameLikelihood() {
        return this.zzc;
    }

    @Retention(RetentionPolicy.CLASS)
    public @interface LandmarkType {
    }
}
