package park.haneol.project.logger.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.TimeZone;

public class PrefUtil {

    private static final String PREF_NAME = "MainActivity";

    private static SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        fontSize = getFontSize(context);
        timeZoneOffset = getTimeOffset(context);
        keypadHeight = getKeypadHeight(context);
        onStartKeypad = getOnStartKeypad(context);
        themeNumber = getThemeColorNumber(context);
        dateFormat = getDateFormat(context);
        int currentVersion = getCurrentVersion(context);
        if (currentVersion < 109) {
            setCurrentVersion(context, 109);
            setDateFormat(context, "{YYYY}-{M}-{D} {wf}({DDD}){wt}");
        }
    }









    private static final String KEY_THEME_COLOR_NUMBER = "themeColorNumber";
    static int themeNumber;

    static int getThemeColorNumber(Context context) {
        return getPref(context).getInt(KEY_THEME_COLOR_NUMBER, 1);
    }

    static void toggleThemeColorNumber(Context context) {
        themeNumber = themeNumber == 0 ? 1 : 0;
        getPref(context).edit().putInt(KEY_THEME_COLOR_NUMBER, themeNumber).apply();
    }







    private static final String KEY_FONT_SIZE = "fontSize";
    public static float fontSize;

    private static float getFontSize(Context context) {
        return getPref(context).getFloat(KEY_FONT_SIZE, 15.0f);
    }

    public static void setFontSize(Context context, float fontSize) {
        getPref(context).edit().putFloat(KEY_FONT_SIZE, fontSize).apply();
        PrefUtil.fontSize = fontSize;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // version > 48

    private static final String KEY_TIME_OFFSET = "time_offset";
    static int timeZoneOffset;

    private static int getTimeOffset(Context context) {
        return getPref(context).getInt(KEY_TIME_OFFSET, getDefaultTimeOffset());
    }

    static void setTimeOffset(Context context, int timeZoneOffset) {
        getPref(context).edit().putInt(KEY_TIME_OFFSET, timeZoneOffset).apply();
        PrefUtil.timeZoneOffset = timeZoneOffset;
    }

    static int getDefaultTimeOffset() {
        return TimeZone.getDefault().getRawOffset() / 60000; // 한국 = +9 (540 min)
    }








    private static final String KEY_KEYPAD_HEIGHT = "keypad_height";
    static int keypadHeight;

    private static int getKeypadHeight(Context context) {
        return getPref(context).getInt(KEY_KEYPAD_HEIGHT, 0);
    }

    static void setKeypadHeight(Context context, int keypadHeight) {
        getPref(context).edit().putInt(KEY_KEYPAD_HEIGHT, keypadHeight).apply();
        PrefUtil.keypadHeight = keypadHeight;
    }






    private static final String KEY_ON_START_KEYPAD = "on_start_keypad";
    static boolean onStartKeypad;

    private static boolean getOnStartKeypad(Context context) {
        return getPref(context).getBoolean(KEY_ON_START_KEYPAD, true);
    }

    static void setOnStartKeypad(Context context, boolean onStartKeypad) {
        getPref(context).edit().putBoolean(KEY_ON_START_KEYPAD, onStartKeypad).apply();
        PrefUtil.onStartKeypad = onStartKeypad;
    }




    private static final String KEY_DATE_FORMAT = "date_format";
    static String dateFormat;

    private static String getDateFormat(Context context) {
        return getPref(context).getString(KEY_DATE_FORMAT, "{YYYY}-{M}-{D} {wf}({DDD}){wt}");
    }

    static void setDateFormat(Context context, String dateFormat) {
        getPref(context).edit().putString(KEY_DATE_FORMAT, dateFormat).apply();
        PrefUtil.dateFormat = dateFormat;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // version > 109

    // 109 버전에서 date format 초기화

    private static final String KEY_CURRENT_VERSION = "current_version";

    private static int getCurrentVersion(Context context) {
        return getPref(context).getInt(KEY_CURRENT_VERSION, 0);
    }

    private static void setCurrentVersion(Context context, int currentVersion) {
        getPref(context).edit().putInt(KEY_CURRENT_VERSION, currentVersion).apply();
    }














    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // for version <= 48

    private static final String KEY_ACCENT_SET = "accentSet";

    static HashSet<String> getAccentSet(Context context) {
        return (HashSet<String>) getPref(context).getStringSet(KEY_ACCENT_SET, new HashSet<String>());
    }

    static void clear48(Context context) {
        getPref(context).edit().remove(KEY_ACCENT_SET).apply();
    }







    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String KEY_IS_SCREEN_SECURE = "is_screen_secure";

    public static boolean getIsScreenSecure(Context context) {
        return getPref(context).getBoolean(KEY_IS_SCREEN_SECURE, false);
    }

    static void setScreenSecure(Context context, boolean isScreenSecure) {
        getPref(context).edit().putBoolean(KEY_IS_SCREEN_SECURE, isScreenSecure).apply();
    }




    private static final String KEY_LABEL_SEPARATOR_LEFT = "label_separator_left";

    public static String getLabelSeparatorLeft(Context context) {
        return getPref(context).getString(KEY_LABEL_SEPARATOR_LEFT, "(");
    }

    static void setLabelSeparatorLeft(Context context, String labelSeparatorLeft) {
        getPref(context).edit().putString(KEY_LABEL_SEPARATOR_LEFT, labelSeparatorLeft).apply();
    }


    private static final String KEY_LABEL_SEPARATOR_RIGHT = "label_separator_right";

    public static String getLabelSeparatorRight(Context context) {
        return getPref(context).getString(KEY_LABEL_SEPARATOR_RIGHT, ") ");
    }

    static void setLabelSeparatorRight(Context context, String labelSeparatorRight) {
        getPref(context).edit().putString(KEY_LABEL_SEPARATOR_RIGHT, labelSeparatorRight).apply();
    }





    private static final String KEY_HIDDEN_PASSWORD = "hidden_password";

    public static String getHiddenPassword(Context context) {
        return getPref(context).getString(KEY_HIDDEN_PASSWORD, "");
    }

    public static void setHiddenPassword(Context context, String password) {
        getPref(context).edit().putString(KEY_HIDDEN_PASSWORD, password).apply();
    }




    private static final String KEY_TEXT_PRESERVED = "editTextPreserved";

    public static String getTextPreserved(Context context) {
        return getPref(context).getString(KEY_TEXT_PRESERVED, "");
    }

    public static void setTextPreserved(Context context, String text) {
        getPref(context).edit().putString(KEY_TEXT_PRESERVED, text).apply();
    }

}
