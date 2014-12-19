// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/** TableRow that draws a divider between each cell. To be used with {@link CalendarGridView}. */
public class CalendarRowView extends ViewGroup {
  private boolean isHeaderRow;
  private MonthView.Listener listener;
  private int cellSize;
  private Context context;

  private VelocityTracker mVelocityTracker = null;
  private int lastX;
  private int lastY;
  private int startY;
  private float fingerX;
  private float fingerY;
  private int touchedCell = -1;
  private int priorTouchedCell = -1;
  private boolean userPanned = false;

  private MonthCellDescriptor.RangeState cellState = MonthCellDescriptor.RangeState.NONE;

  public CalendarRowView(Context context, AttributeSet attrs) {
    super(context, attrs);

    this.context = context;
  }

  @Override public void addView(View child, int index, ViewGroup.LayoutParams params) {

    super.addView(child, index, params);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    long start = System.currentTimeMillis();
    final int totalWidth = MeasureSpec.getSize(widthMeasureSpec);
    cellSize = totalWidth / 7;
    int cellWidthSpec = makeMeasureSpec(cellSize, EXACTLY);
    int cellHeightSpec = isHeaderRow ? makeMeasureSpec(cellSize, AT_MOST) : cellWidthSpec;
    int rowHeight = 0;
    for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
      final View child = getChildAt(c);
      child.measure(cellWidthSpec, cellHeightSpec);
      // The row height is the height of the tallest cell.
      if (child.getMeasuredHeight() > rowHeight) {
        rowHeight = child.getMeasuredHeight();
      }
    }
    final int widthWithPadding = totalWidth + getPaddingLeft() + getPaddingRight();
    final int heightWithPadding = rowHeight + getPaddingTop() + getPaddingBottom();
    setMeasuredDimension(widthWithPadding, heightWithPadding);
    Logr.d("Row.onMeasure %d ms", System.currentTimeMillis() - start);
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    long start = System.currentTimeMillis();
    int cellHeight = bottom - top;
    for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
      final View child = getChildAt(c);
      child.layout(c * cellSize, 0, (c + 1) * cellSize, cellHeight);
    }
    Logr.d("Row.onLayout %d ms", System.currentTimeMillis() - start);
  }

  public void setIsHeaderRow(boolean isHeaderRow) {
    this.isHeaderRow = isHeaderRow;
  }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // processes initial touch down and move.  If return true, forward touches until next touch up or cancel to onTouchEvent()

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:

                // find cell touched
                fingerX = ev.getX();
                fingerY = ev.getY();
                startY = (int)fingerY;

                // determine cell initial hit is on
                touchedCell = fingerIntersectsChildNumber(fingerX, fingerY);

                priorTouchedCell = touchedCell;

                // set up velocity tracker
                Log.d("Touched Cell", "touched cell is " + touchedCell);
                cellState = ((MonthCellDescriptor)getChildAt(touchedCell).getTag()).getRangeState();

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();

                } else {
                    mVelocityTracker.clear();
                }

                // Add a user's movement to the tracker
                mVelocityTracker.addMovement(ev);

                break;

            case MotionEvent.ACTION_MOVE:
                fingerX = ev.getX();
                fingerY = ev.getY();

                // determine cell initial hit is on
                touchedCell = fingerIntersectsChildNumber(fingerX, fingerY);

                priorTouchedCell = touchedCell;

                float xDelta = Math.abs(fingerX - lastX);
                float yDelta = Math.abs(fingerY - lastY);

                float yDeltaTotal = fingerY - startY;
                if (yDelta < xDelta && Math.abs(yDeltaTotal) < ViewConfiguration.get(context).getScaledTouchSlop()) {
                    startY = (int)fingerY;
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:

                    View initialView = getChildAt(touchedCell);
                    for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
                        final View child = getChildAt(c);

                        if (LibUtils.isPointInsideView(fingerX, fingerY, child)) {
                            if (listener != null && child == initialView)
                                listener.handleClick((MonthCellDescriptor) initialView.getTag());

                            break;
                        }
                    }

                break;

            case MotionEvent.ACTION_CANCEL:

                // mVelocityTracker.recycle();
                mVelocityTracker = null;

                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:

                // find cell touched
                fingerX = event.getX();
                fingerY = event.getY();

                // check if scrolling vertically
                float xDelta = Math.abs(fingerX - event.getX());
                float yDelta = Math.abs(fingerY - event.getY());

                float yDeltaTotal = fingerY - startY;
                if (yDelta > xDelta && Math.abs(yDeltaTotal) > ViewConfiguration.get(context).getScaledTouchSlop()) {
                    startY = (int)fingerY;
                    return true;
                }


                // restore initial code
                mVelocityTracker.addMovement(event);

                // get velocity per second
                mVelocityTracker.computeCurrentVelocity(1000);
                float currentVelocity = mVelocityTracker.getXVelocity();

                touchedCell = fingerIntersectsChildNumber(fingerX, fingerY);

                Log.d("Line #177", "touched cell is " + touchedCell + ", fingerX is " + fingerX + ", fingerY is " + fingerY);

                if (touchedCell < 0 || touchedCell > getChildCount() - 1) {
                    return true;
                }

                MonthCellDescriptor mcd = ((MonthCellDescriptor)getChildAt(touchedCell).getTag());

                boolean isClosed = true;
                boolean isSelectable = false;
                if (mcd != null) {
                    isClosed = mcd.isClosed();
                    isSelectable = mcd.isSelectable();
                } else {
                    return true;
                }

                if (priorTouchedCell != -1 && priorTouchedCell != touchedCell) {
                    userPanned = true;
                }
                priorTouchedCell = touchedCell;

                // only track movement if touched down in First or Last cell
                if (cellState == MonthCellDescriptor.RangeState.FIRST && touchedCell != -1) {

                    // check have not reached Last and have not hit Closed Day or prior month
                    if (fingerX <= getLastLeft() && isSelectable) {

                        //retain color of right cell

                        // set color of current cell
                        ((TextView)getChildAt(touchedCell)).setBackgroundResource(R.color.calendar_selected_day_bg);
                        ((TextView)getChildAt(touchedCell)).setTextColor(getResources().getColor(R.color.calendar_text_selected));


                        // if moving left, prior cell is light blue, unless prior cell is LastCell
                        if (currentVelocity < 0) {

                            if (touchedCell + 1 < getChildCount()) {
                                if (((MonthCellDescriptor) getChildAt(touchedCell + 1).getTag()).getRangeState() != MonthCellDescriptor.RangeState.LAST)
                                    ((TextView) getChildAt(touchedCell + 1)).setBackgroundResource(R.color.calendar_selected_range_bg);
                            }

                        } else {
                            // if moving right, prior cell is white
                            if (touchedCell - 1 >= 0) {
                                int priorBackgroundColor = LibUtils.getPriorCellColors((MonthCellDescriptor)getChildAt(touchedCell - 1).getTag()).get(LibUtils.BACKGROUND_COLOR);
                                ((TextView) getChildAt(touchedCell - 1)).setBackgroundResource(priorBackgroundColor);

                                int priorTextColor = LibUtils.getPriorCellColors((MonthCellDescriptor)getChildAt(touchedCell - 1).getTag()).get(LibUtils.TEXT_COLOR);
                                //((TextView) getChildAt(touchedCell - 1)).setTextColor(priorTextColor);
                                ((TextView) getChildAt(touchedCell - 1)).setTextColor(priorTextColor);

                            }
                        }
                    }


                } else if (cellState == MonthCellDescriptor.RangeState.LAST) {

                    // check have not reached First
                    if (fingerX >= getFirstRight() && isSelectable) {

                        // set color of current cell
                        ((TextView)getChildAt(touchedCell)).setBackgroundResource(R.color.calendar_selected_day_bg);
                        ((TextView)getChildAt(touchedCell)).setTextColor(getResources().getColor(R.color.calendar_text_selected));

                        // if moving left, prior cell is white
                        if (currentVelocity < 0) {
                            if (touchedCell + 1 < getChildCount()) {
                                int priorBackgroundColor = LibUtils.getPriorCellColors((MonthCellDescriptor)getChildAt(touchedCell + 1).getTag()).get(LibUtils.BACKGROUND_COLOR);
                                ((TextView) getChildAt(touchedCell + 1)).setBackgroundResource(priorBackgroundColor);


                                int priorTextColor = LibUtils.getPriorCellColors((MonthCellDescriptor)getChildAt(touchedCell + 1).getTag()).get(LibUtils.TEXT_COLOR);
                                ((TextView) getChildAt(touchedCell + 1)).setTextColor(getResources().getColor(priorTextColor));
                            }

                        } else {
                            // if moving right, prior cell is blue
                            if (touchedCell - 1 >= 0) {
                                if (((MonthCellDescriptor) getChildAt(touchedCell - 1).getTag()).getRangeState() != MonthCellDescriptor.RangeState.FIRST)
                                    ((TextView) getChildAt(touchedCell - 1)).setBackgroundResource(R.color.calendar_selected_range_bg);
                            }

                        }
                    }
                }

                // determine which cell is touching
                View touchedView = null;
                for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
                    final View child = getChildAt(c);

                    if (LibUtils.isPointInsideView(fingerX, fingerY, child)) {
                        touchedView = child;
                        break;
                    }
                }



                // update prior x and y positions
                lastX = (int)fingerX;
                lastY = (int)fingerY;

                break;

            case MotionEvent.ACTION_UP:

                // if the user has not tried to expand the range by panning
                if (userPanned == false || (cellState != MonthCellDescriptor.RangeState.FIRST && cellState != MonthCellDescriptor.RangeState.LAST)) {
                    // if position is in same cell as started, then treat as onClick()

                    View initialView = getChildAt(touchedCell);
                    for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
                        final View child = getChildAt(c);

                        if (LibUtils.isPointInsideView(fingerX, fingerY, child)) {
                            if (listener != null && child == initialView)
                                listener.handleClick((MonthCellDescriptor) initialView.getTag());

                            break;
                        }
                    }
                } else {
                    // User has modified start and/or end dates.  Update CalendarPickerView with new endpoints

                    if (listener != null && priorTouchedCell >= 0 && priorTouchedCell <= getChildCount())
                        listener.handleSlideUpdate(cellState, (MonthCellDescriptor) getChildAt(priorTouchedCell).getTag());

                }

                // restore global variables
                userPanned = false;
                priorTouchedCell = -1;
                cellState = MonthCellDescriptor.RangeState.NONE;

                break;

            case MotionEvent.ACTION_CANCEL:

                // mVelocityTracker.recycle();
                mVelocityTracker = null;

                break;
        }

        return true;
    }

    /*
     * Provides X coordinate of Left side of last Date cell
     */
    private int getLastLeft() {

        for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
            final View child = getChildAt(c);

            if (((MonthCellDescriptor)child.getTag()).getRangeState() == MonthCellDescriptor.RangeState.LAST) {
                return child.getLeft();
            }
        }

        return Integer.MAX_VALUE;
    }

    /*
     * Provides X coordinate of Right side of First date cell
     */
    private int getFirstRight() {

        for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
            final View child = getChildAt(c);

            if (((MonthCellDescriptor)child.getTag()).getRangeState() == MonthCellDescriptor.RangeState.FIRST) {
                return child.getLeft() + child.getWidth();
            }
        }

        return Integer.MIN_VALUE;
    }

    /*
     * determine which if any date cell is intersected by the user's touch
     */
    private int fingerIntersectsChildNumber(float x, float y) {

        for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
            final View child = getChildAt(c);

            int viewX = child.getLeft();

            //point is inside view bounds
            if ((x > viewX && x < (viewX + child.getWidth()))) {
                return c;
            }
        }

        return -1;
    }


    public void setListener(MonthView.Listener listener) {
    this.listener = listener;
  }

  public void setCellBackground(int resId) {
    for (int i = 0; i < getChildCount(); i++) {
      getChildAt(i).setBackgroundResource(resId);
    }
  }

  public void setCellTextColor(int resId) {
    for (int i = 0; i < getChildCount(); i++) {
      ((TextView) getChildAt(i)).setTextColor(resId);
    }
  }

  public void setCellTextColor(ColorStateList colors) {
    for (int i = 0; i < getChildCount(); i++) {
      ((TextView) getChildAt(i)).setTextColor(colors);
    }
  }

  public void setTypeface(Typeface typeface) {
    for (int i = 0; i < getChildCount(); i++) {
      ((TextView) getChildAt(i)).setTypeface(typeface);
    }
  }
}
