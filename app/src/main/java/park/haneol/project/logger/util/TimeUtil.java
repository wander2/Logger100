package park.haneol.project.logger.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.Locale;

import park.haneol.project.logger.R;

public class TimeUtil {

    private static String[] df_week_short;
    private static String[] df_week;
    private static String[] df_week_han;
    private static String[] df_month_short;
    private static String[] df_month;
    private static String[] df_ymd;

    private static final int[] MONTH_DAYS_NORMAL = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30 };
    private static final int[] MONTH_DAYS_LEAP = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30 };

    public static void init(Context context) {
        df_week_short = context.getResources().getStringArray(R.array.df_week_short);
        df_week = context.getResources().getStringArray(R.array.df_week);
        df_week_han = context.getResources().getStringArray(R.array.df_week_han);
        df_month_short = context.getResources().getStringArray(R.array.df_month_short);
        df_month = context.getResources().getStringArray(R.array.df_month);
        df_ymd = context.getResources().getStringArray(R.array.df_ymd);
    }

    // time -> Minutes
    static int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 60000L);
    }

    private static boolean isLeapYear(int year) {
        return (year & 3) == 0 && ((year % 25) != 0 || (year & 15) == 0);
    }

    public static int getLocalDays(int time) {
        return (time + PrefUtil.timeZoneOffset) / 1440;
    }

    static int getLocalDayMinutes(int time) {
        return (time + PrefUtil.timeZoneOffset) % 1440;
    }

    static int toSystemTime(int[] date, int[] hm) {
        return getDays(date[0], date[1], date[2]) * 1440 + hm[0] * 60 + hm[1] - PrefUtil.timeZoneOffset;
    }

    public static String getTimeString(int time) {
        int localDayMinutes = getLocalDayMinutes(time);
        int h = localDayMinutes / 60;
        int m = localDayMinutes % 60;
        return getTimeString(h, m);
    }

    private static String getTimeString(int h, int m) {
        StringBuilder sb = new StringBuilder();
        if (h < 10) sb.append(' '); sb.append(h); sb.append(':');
        if (m < 10) sb.append('0'); sb.append(m); sb.append(' ');
        return sb.toString();
    }

    public static Spannable getDateString(int time) {
        int[] each = getEach(time);
        return getDateStringFromEach(each);
    }

    static Spannable getDateStringFromDays(int days) {
        int[] each = getEachFromDays(days);
        return getDateStringFromEach(each);
    }

    private static Spannable getDateStringFromEach(int[] each) {
        final int year = each[0];
        final int month = each[1];
        final int days = each[2];
        final int week = each[3];
        String dateString = PrefUtil.dateFormat
                .replace("{YYYY}", String.valueOf(year))
                .replace("{YY}", getYY(year))
                .replace("{MMMM}", df_month[month-1].toUpperCase())
                .replace("{mmmm}", df_month[month-1])
                .replace("{MMM}", df_month_short[month-1].toUpperCase())
                .replace("{mmm}", df_month_short[month-1])
                .replace("{MM}", String.format(Locale.ENGLISH, "%02d", month))
                .replace("{_M}", String.format(Locale.ENGLISH, "%1$2s", month))
                .replace("{M}", String.valueOf(month))
                .replace("{DD}", String.format(Locale.ENGLISH, "%02d", days))
                .replace("{_D}", String.format(Locale.ENGLISH, "%1$2s", days))
                .replace("{D}", String.valueOf(days))
                .replace("{n}", "\n");
        dateString = dateString
                .replace("{DDDD}", df_week[week].toUpperCase())
                .replace("{dddd}", df_week[week])
                .replace("{DDD}", df_week_short[week].toUpperCase())
                .replace("{ddd}", df_week_short[week])
                .replace("{W}", df_week_han[week]);
        int weekStart = dateString.indexOf("{wf}");
        dateString = dateString.replace("{wf}", "");
        int weekEnd = dateString.indexOf("{wt}");
        dateString = dateString.replace("{wt}", "");
        SpannableString spannable = new SpannableString(dateString);
        int weekColor = ColorUtil.WEEK_COLOR[week];
        if (weekStart != -1 && weekEnd != -1) {
            spannable.setSpan(new ForegroundColorSpan(weekColor), weekStart, weekEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    public static String getDefaultDateFormat(int time) {
        final int[] each = getEach(time);
        final int year = each[0];
        final int month = each[1];
        final int dayOfMonth = each[2];
        final int week = each[3];
        return year + df_ymd[0] + month + df_ymd[1] + dayOfMonth + df_ymd[2] + "(" + df_week_short[week].toUpperCase() + ")";
    }

    static String getDefaultDateFormatFromDays(int days) {
        final int[] each = getEachFromDays(days);
        final int year = each[0];
        final int month = each[1];
        final int dayOfMonth = each[2];
        final int week = each[3];
        return year + df_ymd[0] + month + df_ymd[1] + dayOfMonth + df_ymd[2] + "(" + df_week_short[week].toUpperCase() + ")";
    }

    private static int getWeek(int days) {
        return (days + 4) % 7;
    }

    static int[] getEach(int time) {
        int days = getLocalDays(time);
        return getEachFromDays(days);
    }

    static int[] getEachFromDays(int days) {
        int week = getWeek(days);
        int year = 1970;
        int yearDays;
        while (days >= (yearDays = isLeapYear(year) ? 366 : 365)) {
            days -= yearDays;
            year++;
        }
        int month = 1;
        if (isLeapYear(year)) {
            for (int i = 0; i < 11; i++) {
                if (days >= MONTH_DAYS_LEAP[i]) {
                    days -= MONTH_DAYS_LEAP[i];
                    month++;
                } else {
                    break;
                }
            }
        } else {
            for (int i = 0; i < 11; i++) {
                if (days >= MONTH_DAYS_NORMAL[i]) {
                    days -= MONTH_DAYS_NORMAL[i];
                    month++;
                } else {
                    break;
                }
            }
        }
        days++; // 거리 0일 -> 명칭 1일
        return new int[] {year, month, days, week};
    }

    // get days from int[] of local date
    static int getDays(int year, int month, int dayOfMonth) {
        int days = 0; // 1970. 1. 1.
        for (int y = 1970; y < year; y++) {
            days += isLeapYear(y) ? 366 : 365;
        }
        if (isLeapYear(year)) {
            for (int m = 0; m < month - 1; m++) {
                days += MONTH_DAYS_LEAP[m];
            }
        } else {
            for (int m = 0; m < month - 1; m++) {
                days += MONTH_DAYS_NORMAL[m];
            }
        }
        days += dayOfMonth - 1;
        return days;
    }

    static String getUTC() {
        int hour = Math.abs(PrefUtil.timeZoneOffset) / 60;
        int min = Math.abs(PrefUtil.timeZoneOffset) % 60;
        String utc = "UTC";
        if (PrefUtil.timeZoneOffset > 0) {
            utc += "+" + hour;
            if (min != 0) {
                utc += ":" + min;
            }
        } else if (PrefUtil.timeZoneOffset < 0) {
            utc += "-" + hour;
            if (min != 0) {
                utc += ":" + min;
            }
        } else {
            utc += " 0";
        }
        return utc;
    }

    private static String getYY(int year) {
        String YYYY = String.valueOf(year);
        if (YYYY.length() >= 4) {
            return YYYY.substring(YYYY.length() - 2);
        }
        return YYYY;
    }

}
