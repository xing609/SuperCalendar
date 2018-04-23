package com.haibin.calendarviewproject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    /**
     * 获取一个月前的日期
     *
     * @param date 传入的日期
     * @return
     */
    public static String getMonthAgo(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        String monthAgo = simpleDateFormat.format(calendar.getTime());
        return monthAgo;
    }

    /**
     * 获取一后月前的日期
     *
     * @param date 传入的日期
     * @return
     */
    public static String getMonthAfter(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, +1);
        String monthAgo = simpleDateFormat.format(calendar.getTime());
        return monthAgo;
    }

    //把日期转为字符串
    public static String ConverToString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        return df.format(date);
    }

    //把字符串转为日期
    public static Date ConverToDate(String strDate) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.parse(strDate);
    }


    /**
     * 判断是否超过当天时间
     *
     * @param strDate
     * @return
     */
    public static boolean aboveToday(String strDate) throws Exception {
        boolean flag = false;
        if (strDate != null) {
            Date chooseDate = ConverToDate(strDate);
            Date now = new Date(System.currentTimeMillis());
            String nowTime = ConverToString(now);
            if (strDate.equals(nowTime)) {
                flag = true;
            } else {
                flag = chooseDate.after(now);
            }
        }
        return flag;
    }

}
