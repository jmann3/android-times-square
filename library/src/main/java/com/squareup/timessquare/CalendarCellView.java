// Copyright 2013 Square, Inc.

package com.squareup.timessquare;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class CalendarCellView extends TextView {
  private static final int[] STATE_SELECTABLE = {
      R.attr.state_selectable
  };
  private static final int[] STATE_CURRENT_MONTH = {
      R.attr.state_current_month
  };
  private static final int[] STATE_TODAY = {
      R.attr.state_today
  };
  private static final int[] STATE_DAY_CLOSED = {
      R.attr.state_day_closed
  };
  private static final int[] STATE_HIGHLIGHTED = {
      R.attr.state_highlighted
  };
  private static final int[] STATE_RANGE_FIRST = {
      R.attr.state_range_first
  };
  private static final int[] STATE_RANGE_MIDDLE = {
      R.attr.state_range_middle
  };
  private static final int[] STATE_RANGE_LAST = {
      R.attr.state_range_last
  };

  private boolean isSelectable = false;
  private boolean isCurrentMonth = false;
  private boolean isToday = false;
  private boolean isClosed = false;
  private boolean isHighlighted = false;
  private RangeState rangeState = RangeState.NONE;

  @SuppressWarnings("UnusedDeclaration") //
  public CalendarCellView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setSelectable(boolean isSelectable) {
    this.isSelectable = isSelectable;
    refreshDrawableState();
  }

  public void setCurrentMonth(boolean isCurrentMonth) {
    this.isCurrentMonth = isCurrentMonth;
    refreshDrawableState();
  }

  public void setToday(boolean isToday) {
    this.isToday = isToday;
    refreshDrawableState();
  }

  public void setClosed(boolean isClosed) {
    this.isClosed = isClosed;
    refreshDrawableState();
  }

  public void setRangeState(RangeState rangeState) {
    this.rangeState = rangeState;

    refreshDrawableState();
  }

  public void setHighlighted(boolean highlighted) {
    isHighlighted = highlighted;
    refreshDrawableState();
  }

  @Override protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 8);

    if (isSelectable) {
      mergeDrawableStates(drawableState, STATE_SELECTABLE);
    }

    if (isCurrentMonth) {
      mergeDrawableStates(drawableState, STATE_CURRENT_MONTH);
    }

    if (isToday) {
      mergeDrawableStates(drawableState, STATE_TODAY);
    }

    if (isClosed) {
      mergeDrawableStates(drawableState, STATE_DAY_CLOSED);
    }

    if (isHighlighted) {
      mergeDrawableStates(drawableState, STATE_HIGHLIGHTED);
    }

    if (rangeState == RangeState.FIRST) {
      mergeDrawableStates(drawableState, STATE_RANGE_FIRST);
    } else if (rangeState == RangeState.MIDDLE) {
      mergeDrawableStates(drawableState, STATE_RANGE_MIDDLE);
    } else if (rangeState == RangeState.LAST) {
      mergeDrawableStates(drawableState, STATE_RANGE_LAST);
    }

    if (rangeState == RangeState.MIDDLE && isClosed == true)
        Log.d("Middle State", "Middle State should be blue");

    return drawableState;
  }
}
