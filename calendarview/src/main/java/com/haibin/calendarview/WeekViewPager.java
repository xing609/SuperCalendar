/*
 * Copyright (C) 2016 huanghaibin_dev <huanghaibin_dev@163.com>
 * WebSite https://github.com/MiracleTimes-Dev
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haibin.calendarview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.reflect.Constructor;

/**
 * 周视图滑动ViewPager，需要动态固定高度
 * 周视图是连续不断的视图，因此不能简单的得出每年都有52+1周，这样会计算重叠的部分
 * WeekViewPager需要和CalendarView关联:
 */

public final class WeekViewPager extends ViewPager {

    private int mWeekCount;
    private CustomCalendarViewDelegate mDelegate;

    /**
     * 日历布局，需要在日历下方放自己的布局
     */
    CalendarLayout mParentLayout;

    /**
     * 是否使用滚动到某一天
     */
    private boolean isUsingScrollToCalendar = false;

    public WeekViewPager(Context context) {
        this(context, null);
    }

    public WeekViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setup(CustomCalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        init();
    }

    public static CalendarView.PageListener mPageListener;
    private boolean isCanScroll = true;
    private boolean nowFlage=false;//当前标识
    private int firstPosition;
    /**
     * 上一次x坐标
     */
    private float beforeX;

    //-----禁止左滑-------左滑：上一次坐标 > 当前坐标
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(isCanScroll){
            return super.dispatchTouchEvent(ev);
        }else  {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN://按下如果‘仅’作为‘上次坐标’，不妥，因为可能存在左滑，motionValue大于0的情况（来回滑，只要停止坐标在按下坐标的右边，左滑仍然能滑过去）
                    beforeX = ev.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float motionValue = ev.getX() - beforeX;
                    if (motionValue> 0) {//禁止左滑
                        return true;
                    }
                    beforeX = ev.getX();//手指移动时，再把当前的坐标作为下一次的‘上次坐标’，解决上述问题

                    break;
                default:
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }

    }

    /**
     * 设置 是否可以滑动
     * @param isCanScroll
     */
    public void setLeftScrollble(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public boolean getCanScroll(){
        return isCanScroll;
    }



    public static void pageScrollListener(CalendarView.PageListener pageListener){
        mPageListener=pageListener;
    }


    private void init() {
        mWeekCount = Util.getWeekCountBetweenYearAndYear(mDelegate.getMinYear(), mDelegate.getMinYearMonth(),
                mDelegate.getMaxYear(), mDelegate.getMaxYearMonth());
        setAdapter(new WeekViewPagerAdapter());
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(mPageListener!=null){
                    mPageListener.onPageSelected();
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(!nowFlage){
                    firstPosition=position;
                    nowFlage=true;
                }
                isCanScroll=position==firstPosition?false:true;

                //默认的显示星期四，周视图切换就显示星期4
                if (getVisibility() != VISIBLE) {
                    isUsingScrollToCalendar = false;
                    return;
                }
                if(mPageListener!=null){
                    mPageListener.onPageSelected();
                }

                WeekView view = (WeekView) findViewWithTag(position);
                if (view != null) {
                    view.performClickCalendar(mDelegate.mSelectedCalendar, !isUsingScrollToCalendar);
                }else
                isUsingScrollToCalendar = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    void notifyDataSetChanged() {
        mWeekCount = Util.getWeekCountBetweenYearAndYear(mDelegate.getMinYear(), mDelegate.getMinYearMonth(),
                mDelegate.getMaxYear(), mDelegate.getMaxYearMonth());
        getAdapter().notifyDataSetChanged();
    }

    /**
     * 滚动到指定日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    void scrollToCalendar(int year, int month, int day, boolean smoothScroll) {
        isUsingScrollToCalendar = true;
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setCurrentDay(calendar.equals(mDelegate.getCurrentDay()));
        LunarCalendar.setupLunarCalendar(calendar);
        mDelegate.mSelectedCalendar = calendar;
        updateSelected(calendar, smoothScroll);
        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, false);
        }
        if (mDelegate.mDateSelectedListener != null) {
            mDelegate.mDateSelectedListener.onDateSelected(calendar, false);
        }
    }

    /**
     * 滚动到当前
     */
    void scrollToCurrent(boolean smoothScroll) {
        isUsingScrollToCalendar = true;
        int position = Util.getWeekFromCalendarBetweenYearAndYear(mDelegate.getCurrentDay(),
                mDelegate.getMinYear(),
                mDelegate.getMinYearMonth()) - 1;
        int curItem = getCurrentItem();
        if (curItem == position) {
            isUsingScrollToCalendar = false;
        }
        setCurrentItem(position, smoothScroll);
        WeekView view = (WeekView) findViewWithTag(position);
        if (view != null) {
            view.performClickCalendar(mDelegate.getCurrentDay(), false);
            view.setSelectedCalendar(mDelegate.getCurrentDay());
            view.invalidate();
        }
        if (mDelegate.mDateSelectedListener != null && getVisibility() == VISIBLE) {
            mDelegate.mDateSelectedListener.onDateSelected(mDelegate.createCurrentDate(), false);
        }
    }

    /**
     * 更新任意一个选择的日期
     */
    void updateSelected(Calendar calendar, boolean smoothScroll) {
        int position = Util.getWeekFromCalendarBetweenYearAndYear(calendar, mDelegate.getMinYear(), mDelegate.getMinYearMonth()) - 1;
        int curItem = getCurrentItem();
        if (curItem == position) {
            isUsingScrollToCalendar = false;
        }
        setCurrentItem(position, smoothScroll);
        WeekView view = (WeekView) findViewWithTag(position);
        if (view != null) {
            view.setSelectedCalendar(calendar);
            view.invalidate();
        }
    }


    /**
     * 更新标记日期
     */
    void updateScheme() {
        for (int i = 0; i < getChildCount(); i++) {
            WeekView view = (WeekView) getChildAt(i);
            view.update();
        }
    }

    /**
     * 周视图的高度应该与日历项的高度一致
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getCalendarItemHeight(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 周视图切换
     */
    private class WeekViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mWeekCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Calendar calendar = Util.getFirstCalendarFromWeekCount(mDelegate.getMinYear(), mDelegate.getMinYearMonth(), position + 1);
            WeekView view;
            if (TextUtils.isEmpty(mDelegate.getWeekViewClass())) {
                view = new DefaultWeekView(getContext());
            } else {
                try {
                    Class cls = Class.forName(mDelegate.getWeekViewClass());
                    @SuppressWarnings("unchecked")
                    Constructor constructor = cls.getConstructor(Context.class);
                    view = (WeekView) constructor.newInstance(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            view.mParentLayout = mParentLayout;
            view.setup(mDelegate);
            view.setup(calendar);
            view.setTag(position);
            view.setSelectedCalendar(mDelegate.mSelectedCalendar);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}
