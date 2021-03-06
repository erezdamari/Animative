package com.example.erezd.animative.utilities.animation;

/**
 * Created by avivam on 30/04/2018.
 */

public class PointCalc {

    public float x,y;

    public PointCalc(float x, float y){
        this.x = x;
        this.y = y;
    }


    public static float Lenght(float x, float y){
        return (float) Math.sqrt(x*x + y*y);
    }

    public static float Distance(float xStart, float yStart, float xEnd, float yEnd){
        return Lenght(xEnd - xStart, yEnd - yStart);
    }

    //returns the x,y of the direction vector
    public static PointCalc Direction(float x, float y){
        return new PointCalc(x/Lenght(x,y), y/Lenght(x,y));
    }
}
