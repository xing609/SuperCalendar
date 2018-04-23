package com.haibin.calendarviewproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.haibin.calendarviewproject.base.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MeiZuActivity extends BaseActivity implements
        CalendarView.OnDateSelectedListener,
        CalendarView.OnYearChangeListener{

    private CalendarView mCalendarView;
    private int mYear;
    private int mMonth;
    private int mDay;
    CalendarLayout mCalendarLayout;
    private Context mContext;

    public static void show(Context context) {
        context.startActivity(new Intent(context, MeiZuActivity.class));
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        mContext = this;
        setStatusBarDarkMode();
        mCalendarView = (CalendarView) findViewById(R.id.calendarView);

        //上个月
        findViewById(R.id.btnUpMonth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCalendarView.getCanScroll()) {
                    return;
                }
                String time = mYear + "-" + mMonth + "-" + mDay;
                try {
                    String upTime = DateUtil.getMonthAgo(DateUtil.ConverToDate(time));
                    Log.e("UPTIME=", upTime);
                    if (!DateUtil.aboveToday(upTime)) {
                        return;
                    }
                    if (upTime != null) {
                        String[] sourceStrArray = upTime.split("-");
                        mCalendarView.scrollToCalendar(Integer.parseInt(sourceStrArray[0]), Integer.parseInt(sourceStrArray[1]), Integer.parseInt(sourceStrArray[2]), false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //下个月
        findViewById(R.id.btnNextMonth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = mYear + "-" + mMonth + "-" + mDay;
                try {
                    String nextTime = DateUtil.getMonthAfter(DateUtil.ConverToDate(time));
                    if (nextTime != null) {
                        String[] sourceStrArray = nextTime.split("-");
                        mCalendarView.scrollToCalendar(Integer.parseInt(sourceStrArray[0]), Integer.parseInt(sourceStrArray[1]), Integer.parseInt(sourceStrArray[2]), false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //上一周
        findViewById(R.id.btnUpWeek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCalendarView.getCanScroll()) {
                    return;
                }
                mCalendarView.scrollToPre();
                showText();

            }
        });

        //下一周
        findViewById(R.id.btnNextWeek).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.scrollToNext();
                showText();
            }
        });

        //滑动
        mCalendarView.pageScrollListener(new CalendarView.PageListener() {
            @Override
            public void onPageSelected() {
                showText();
            }
        });

        //设置日期范围
        //mCalendarView.setRange(2018,4,2018,12);
        mCalendarLayout = (CalendarLayout) findViewById(R.id.calendarLayout);
        mCalendarView.setOnDateSelectedListener(this);
        mCalendarView.setOnYearChangeListener(this);

        mYear = mCalendarView.getCurYear();
    }

    @Override
    protected void initData() {
        List<Calendar> schemes = new ArrayList<>();
        int year = mCalendarView.getCurYear();
        int month = mCalendarView.getCurMonth();

        schemes.add(getSchemeCalendar(year, month, 3, 0xFF40db25, "假"));
        schemes.add(getSchemeCalendar(year, month, 6, 0xFFe69138, "事"));
        schemes.add(getSchemeCalendar(year, month, 9, 0xFFdf1356, "议"));
        schemes.add(getSchemeCalendar(year, month, 13, 0xFFedc56d, "记"));
        schemes.add(getSchemeCalendar(year, month, 14, 0xFFedc56d, "记"));
        schemes.add(getSchemeCalendar(year, month, 15, 0xFFaacc44, "假"));
        schemes.add(getSchemeCalendar(year, month, 18, 0xFFbc13f0, "记"));
        schemes.add(getSchemeCalendar(year, month, 25, 0xFF13acf0, "假"));
        schemes.add(getSchemeCalendar(year, month, 27, 0xFF13acf0, "多"));
        //mCalendarView.setSchemeDate(schemes);

    }



    private Calendar getSchemeCalendar(int year, int month, int day, int color, String text) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        calendar.addScheme(new Calendar.Scheme());
        calendar.addScheme(0xFF008800, "假");
        calendar.addScheme(0xFF008800, "节");
        return calendar;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSelected(Calendar calendar, boolean isClick) {
        mYear = calendar.getYear();
        mMonth = calendar.getMonth();
        mDay = calendar.getDay();
        if (isClick) {
            Toast.makeText(this, getCalendarText(calendar), Toast.LENGTH_SHORT).show();
        }
    }

    private static String getCalendarText(Calendar calendar) {
        return String.format("新历%s \n 农历%s \n 公历节日：%s \n 农历节日：%s \n 节气：%s \n 是否闰月：%s",
                calendar.getMonth() + "月" + calendar.getDay() + "日",
                calendar.getLunarCakendar().getMonth() + "月" + calendar.getLunarCakendar().getDay() + "日",
                TextUtils.isEmpty(calendar.getGregorianFestival()) ? "无" : calendar.getGregorianFestival(),
                TextUtils.isEmpty(calendar.getTraditionFestival()) ? "无" : calendar.getTraditionFestival(),
                TextUtils.isEmpty(calendar.getSolarTerm()) ? "无" : calendar.getSolarTerm(),
                calendar.getLeapMonth() == 0 ? "否" : String.format("闰%s月", calendar.getLeapMonth()));
    }

    @Override
    public void onYearChange(int year) {
//        mTextMonthDay.setText(String.valueOf(year));

    }


    private void showText() {
        Calendar calendar = mCalendarView.getSelectedCalendar();
//        mTextMonthDay.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
//        mTextLunar.setText("今日");
//        mTextCurrentDay.setText(String.valueOf(calendar.getDay()));
    }
}
