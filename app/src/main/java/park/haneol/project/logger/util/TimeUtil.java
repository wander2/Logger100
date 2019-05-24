package park.haneol.project.logger.util;

import android.content.Context;

import java.util.Locale;

import park.haneol.project.logger.R;

public class TimeUtil {

    private static String[] df_DDD;
    private static String[] df_Ddd;
    private static String[] df_DDDD;
    private static String[] df_MMM;
    private static String[] df_Mmm;
    private static String[] df_MMMM;

    private static final int[] MONTH_DAYS_NORMAL = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30 };
    private static final int[] MONTH_DAYS_LEAP = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30 };

    public static void init(Context context) {
        df_DDD = context.getResources().getStringArray(R.array.df_DDD);
        df_Ddd = context.getResources().getStringArray(R.array.df_Ddd);
        df_DDDD = context.getResources().getStringArray(R.array.df_DDDD);
        df_MMM = context.getResources().getStringArray(R.array.df_MMM);
        df_Mmm = context.getResources().getStringArray(R.array.df_Mmm);
        df_MMMM = context.getResources().getStringArray(R.array.df_MMMM);
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

    private static int getLocalTime(int time) {
        return (time + PrefUtil.timeZoneOffset) % 1440;
    }

    public static String getTimeString(int time) {
        int localTime = getLocalTime(time);
        int h = localTime / 60;
        int m = localTime % 60;
        StringBuilder sb = new StringBuilder();
        if (h < 10) sb.append(' '); sb.append(h); sb.append(':');
        if (m < 10) sb.append('0'); sb.append(m); sb.append(' ');
        return sb.toString();
    }

    public static String getDateString(int time) {
        final int[] each = getEach(time);
        final int year = each[0];
        final int month = each[1];
        final int days = each[2];
        final int week = each[3];
        return PrefUtil.dateFormat.replace("YYYY", String.valueOf(year))
                .replace("YY", getYY(year))
                .replace("MMMM", df_MMMM[month-1])
                .replace("MMM", df_MMM[month-1])
                .replace("Mmm", df_Mmm[month-1])
                .replace("MM", String.format(Locale.ENGLISH, "%02d", month))
                .replace("SM", String.format(Locale.ENGLISH, "%1$2s", month))
                .replace("M", String.valueOf(month))
                .replace("DDDD", df_DDDD[week])
                .replace("DDD", df_DDD[week])
                .replace("Ddd", df_Ddd[week])
                .replace("DD", String.format(Locale.ENGLISH, "%02d", days))
                .replace("SD", String.format(Locale.ENGLISH, "%1$2s", days))
                .replace("D", String.valueOf(days));
    }

    public static String getDefaultDateFormat(int time) {
        final int[] each = getEach(time);
        final int year = each[0];
        final int month = each[1];
        final int days = each[2];
        return year + "-" + month + "-" + days;
    }

    public static int getWeek(int days) {
        return (days + 4) % 7;
    }

    private static int[] getEach(int time) {
        int days = getLocalDays(time);
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
