// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
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
        float fingerX = view.getLeft() + motionEvent.getX();
        float fingerY = view.getTop() + motionEvent.getY();

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

                // only track movement if touched down in First or Last cell
                if (cellState == MonthCellDescriptor.RangeState.FIRST) {

                } else if (cellState == MonthCellDescriptor.RangeState.LAST) {

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


                // if this is a start or end position
                if (monthCellDescriptor.getRangeState() == MonthCellDescriptor.RangeState.FIRST || monthCellDescriptor.getRangeState() == MonthCellDescriptor.RangeState.LAST) {
                    // determine which cells touch is overlapping

                    // notify listener of new
                }



                break;

            case MotionEvent.ACTION_UP:

                // remove range state
                cellState = MonthCellDescriptor.RangeState.NONE;

                // get x and y position

                // if position is in same cell as started, then treat as onClick()
                for (int c = 0, numChildren = getChildCount(); c < numChildren; c++) {
                    final View child = getChildAt(c);

                    if (LibUtils.isPointInsideView(fingerX, fingerY, child)) {
                        if (listener != null && child == view)
                            listener.handleClick((MonthCellDescriptor) view.getTag());

                        break;
                    }
                }



                break;

            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                break;

        }

        return true;
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
