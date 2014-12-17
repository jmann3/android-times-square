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
import android.view.ViewGroup;
import android.widget.TextView;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/** TableRow that draws a divider between each cell. To be used with {@link CalendarGridView}. */
public class CalendarRowView extends ViewGroup implements View.OnClickListener, View.OnTouchListener {
  private boolean isHeaderRow;
  private MonthView.Listener listener;
  private int cellSize;
  private MonthCellDescriptor.RangeState cellState;

  private VelocityTracker mVelocityTracker = null;
  private int priorTouchedCell = -1;
  private boolean userPanned = false;

  public CalendarRowView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public void addView(View child, int index, ViewGroup.LayoutParams params) {
    child.setOnClickListener(this);

    child.setOnTouchListener(this);

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

  @Override public void onClick(View v) {
    // Header rows don't have a click listener
//    if (listener != null) {
//      listener.handleClick((MonthCellDescriptor) v.getTag());
//    }
  }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int index = motionEvent.getActionIndex();
        int action = motionEvent.getActionMasked();
        int pointerId = motionEvent.getPointerId(index);
        MonthCellDescriptor monthCellDescriptor = (MonthCellDescriptor)view.getTag();
        boolean isClosed = monthCellDescriptor.isClosed();
        boolean isSelectable = monthCellDescriptor.isSelectable();
        float fingerX = view.getLeft() + motionEvent.getX();
        float fingerY = view.getTop() + motionEvent.getY();
        int touchedCell = -1;

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                cellState = monthCellDescriptor.getRangeState();

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();

                } else {
                    mVelocityTracker.clear();
                }

                // Add a user's movement to the tracker
                mVelocityTracker.addMovement(motionEvent);
                break;

            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(motionEvent);

                // get velocity per second
                mVelocityTracker.computeCurrentVelocity(1000);
                float currentVelocity = mVelocityTracker.getXVelocity();

                touchedCell = fingerIntersectsChildNumber(fingerX, fingerY);

                if (priorTouchedCell != -1 && priorTouchedCell != touchedCell) {
                    userPanned = true;
                }
                priorTouchedCell = touchedCell;

                // only track movement if touched down in First or Last cell
                if (cellState == MonthCellDescriptor.RangeState.FIRST && touchedCell != -1) {

                    // check have not reached Last and have not hit closed day or prior month
                    if (fingerX <= getLastLeft(view) && !isClosed && isSelectable) {

//                        int leftBackgroundColor = 0;
//                        int leftTextColor = 0;
//                        int rightBackgroundColor = 0;
//                        int rightTextColor = 0;

//                        // retain color of left cell
//                        if (touchedCell - 1 > 0) {
//                            TextView leftCell = ((TextView) getChildAt(touchedCell - 1));
//                            ColorDrawable leftCellBackgroundColor = (ColorDrawable) leftCell.getBackground();
//                            leftBackgroundColor = leftCellBackgroundColor.getColor();
//                            leftTextColor = leftCell.getCurrentTextColor();
//                        }
//
//                        if (touchedCell + 1 <= getChildCount()) {
//                            TextView leftCell = ((TextView) getChildAt(touchedCell + 1));
//                            ColorDrawable leftCellBackgroundColor = (ColorDrawable) leftCell.getBackground();
//                            rightBackgroundColor = leftCellBackgroundColor.getColor();
//                            rightTextColor = leftCell.getCurrentTextColor();
//                        }

                        //retain color of right cell

                        // set color of current cell
                        ((TextView)getChildAt(touchedCell)).setBackgroundResource(R.color.calendar_selected_day_bg);
                        ((TextView)getChildAt(touchedCell)).setTextColor(getResources().getColor(R.color.calendar_text_selected));


                        // if moving left, prior cell is light blue, unless prior cell is LastCell
                        if (currentVelocity < 0) {

                            if (((MonthCellDescriptor)getChildAt(touchedCell + 1).getTag()).getRangeState() != MonthCellDescriptor.RangeState.LAST)
                                ((TextView)getChildAt(touchedCell + 1)).setBackgroundResource(R.color.calendar_selected_range_bg);

                        } else {
                            // if moving right, prior cell is white
                            if (touchedCell - 1 >= 0) {
                                ((TextView) getChildAt(touchedCell - 1)).setBackgroundResource(R.color.calendar_active_month_bg);
                                ((TextView) getChildAt(touchedCell - 1)).setTextColor(getResources().getColor(R.color.calendar_text_active));
                            }
                        }
                    }


                } else if (cellState == MonthCellDescriptor.RangeState.LAST) {

                    // check have not reached First
                    if (fingerX >= getFirstRight(view) && !isClosed && isSelectable) {

                        // set color of current cell
                        ((TextView)getChildAt(touchedCell)).setBackgroundResource(R.color.calendar_selected_day_bg);
                        ((TextView)getChildAt(touchedCell)).setTextColor(getResources().getColor(R.color.calendar_text_selected));

                        // if moving left, prior cell is white
                        if (currentVelocity < 0) {
                            if (touchedCell + 1 < getChildCount()) {
                                ((TextView) getChildAt(touchedCell + 1)).setBackgroundResource(R.color.calendar_active_month_bg);
                                ((TextView) getChildAt(touchedCell + 1)).setTextColor(getResources().getColor(R.color.calendar_text_active));
                            }

                        } else {
                            // if moving right, prior cell is blue
                            if (((MonthCellDescriptor)getChildAt(touchedCell - 1).getTag()).getRangeState() != MonthCellDescriptor.RangeState.FIRST)
                                ((TextView)getChildAt(touchedCell - 1)).setBackgroundResource(R.color.calendar_selected_range_bg);

                        }
                    }
                }

                // determine which cell is touching
                View touchedView = view;
                for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
                    final View child = getChildAt(c);

                    if (LibUtils.isPointInsideView(fingerX, fingerY, child)) {
                        touchedView = child;
                        break;
                    }
                }

                break;

            case MotionEvent.ACTION_UP:

                // if the user has not tried to expand the range by panning
                if (userPanned == false) {
                    // if position is in same cell as started, then treat as onClick()
                    for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
                        final View child = getChildAt(c);

                        if (LibUtils.isPointInsideView(fingerX, fingerY, child)) {
                            if (listener != null && child == view)
                                listener.handleClick((MonthCellDescriptor) view.getTag());

                            break;
                        }
                    }
                } else {
                    // User has modified start and/or end dates.  Update CalendarPickerView with new endpoints

                    if (listener != null && priorTouchedCell >= 0 && priorTouchedCell <= getChildCount())
                        listener.handleSlideUpdate(cellState, (MonthCellDescriptor) getChildAt(priorTouchedCell).getTag());

                    // clear prior selections


                }

                // restore global variables
                userPanned = false;
                priorTouchedCell = -1;
                cellState = MonthCellDescriptor.RangeState.NONE;

                break;

            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;

        }

        return true;
    }

    /*
     * Provides X coordinate of Left side of last Date cell
     */
    private int getLastLeft(View view) {

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
    private int getFirstRight(View view) {

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
