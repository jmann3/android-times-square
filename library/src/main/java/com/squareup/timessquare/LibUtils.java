package com.squareup.timessquare;

import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jmann on 12/16/14.
 */
public class LibUtils {

    public static final String BACKGROUND_COLOR = "background_color";
    public static final String TEXT_COLOR = "text_color";

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

    public static Map<String, Integer> getPriorCellColors(MonthCellDescriptor monthCellDescriptor) {

        Map<String, Integer> colorMap = new HashMap<String, Integer>();

        // choose Background Color
        if (monthCellDescriptor.isClosed()) {
            colorMap.put(BACKGROUND_COLOR, R.color.not_available_background);
        } else if (monthCellDescriptor.isHighlighted()) {
            colorMap.put(BACKGROUND_COLOR, R.color.calendar_highlighted_day_bg);
        } else if (monthCellDescriptor.isToday()) {
            colorMap.put(BACKGROUND_COLOR, R.color.calendar_bg);
        } else {
            colorMap.put(BACKGROUND_COLOR, R.color.calendar_bg);
        }


        // choose Text Color
        if (monthCellDescriptor.isToday()) {
            colorMap.put(TEXT_COLOR, R.color.calendar_text_active);
        } else if (!monthCellDescriptor.isCurrentMonth()) {
            colorMap.put(TEXT_COLOR, R.color.calendar_text_inactive);
        } else if (!monthCellDescriptor.isSelectable()) {
            colorMap.put(TEXT_COLOR, R.color.calendar_text_unselectable);
        } else if (monthCellDescriptor.isClosed()) {
            colorMap.put(TEXT_COLOR, R.color.calendar_text_active);
        } else if (monthCellDescriptor.isHighlighted()) {
            colorMap.put(TEXT_COLOR, R.color.calendar_highlighted_day_bg);
        } else {
            colorMap.put(TEXT_COLOR, R.color.calendar_text_active);
        }

            return colorMap;
    }

}
