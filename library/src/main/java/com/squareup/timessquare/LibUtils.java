package com.squareup.timessquare;

import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * Created by jmann on 12/16/14.
 */
public class LibUtils {

    public static boolean isPointInsideView(float x, float y, View view){

        int viewX = view.getLeft();
        int viewY = view.getTop();

        //point is inside view bounds
        if(( x > viewX && x < (viewX + view.getWidth()))){
            return true;
        } else {
            return false;
        }
    }

}
