package com.squareup.timessquare;

import android.view.View;

/**
 * Created by jmann on 12/16/14.
 */
public class LibUtils {

    public static boolean isPointInsideView(float x, float y, View view){
//        int location[] = new int[2];
//        view.getLocationOnScreen(location);
//        int viewX = location[0];
//        int viewY = location[1];

        int viewX = view.getLeft();

        //point is inside view bounds
        if(( x > viewX && x < (viewX + view.getWidth()))){
            return true;
        } else {
            return false;
        }
    }
}
