package park.haneol.project.logger.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import park.haneol.project.logger.R;
import park.haneol.project.logger.recyclerview.RecView;
import park.haneol.project.logger.view.RootLayout;

public class ColorUtil {

    public static int colorBlink;

    static float currentInter;

    private static int black_colorBackground;
    private static int black_colorTime;
    private static int black_colorLog;
    private static int black_colorHighlight;
    private static int[] BLACK_WEEK_COLOR;

    private static int white_colorBackground;
    private static int white_colorTime;
    private static int white_colorLog;
    private static int white_colorHighlight;
    private static int[] WHITE_WEEK_COLOR = new int[7];

    private static int colorBackground;  // rootLayout
    private static int colorWB; // editText - text, imageButton - tint
    public static int colorBW; // log highlight - text
    public static int colorTime; // time, date - text
    public static int colorLog; // log - text
    public static int colorHighlight; // log highlight - background
    public static int[] WEEK_COLOR = new int[7];

    public static void init(Activity activity) {
        Resources res = activity.getResources();
        currentInter = (float) PrefUtil.getThemeColorNumber(activity);

        // res default = black theme
        black_colorBackground = res.getColor(R.color.colorBackground);
        black_colorTime = res.getColor(R.color.colorItemTime);
        black_colorLog = res.getColor(R.color.colorItemText);
        black_colorHighlight = res.getColor(R.color.colorHighlight);
        BLACK_WEEK_COLOR = res.getIntArray(R.array.week_color);

        white_colorBackground = res.getColor(R.color.w_colorBackground);
        white_colorTime = res.getColor(R.color.w_colorItemTime);
        white_colorLog = res.getColor(R.color.w_colorItemText);
        white_colorHighlight = res.getColor(R.color.w_colorHighlight);
        WHITE_WEEK_COLOR = res.getIntArray(R.array.w_week_color);

        if (PrefUtil.themeNumber == 1) {
            colorBackground = black_colorBackground;
            colorTime = black_colorTime;
            colorLog = black_colorLog;
            colorHighlight = black_colorHighlight;
            System.arraycopy(BLACK_WEEK_COLOR, 0, WEEK_COLOR, 0, 7);
            colorBlink = res.getColor(R.color.colorItemBlink);
            colorWB = Color.WHITE;
            colorBW = Color.BLACK;
        } else {
            colorBackground = white_colorBackground;
            colorTime = white_colorTime;
            colorLog = white_colorLog;
            colorHighlight = white_colorHighlight;
            System.arraycopy(WHITE_WEEK_COLOR, 0, WEEK_COLOR, 0, 7);
            colorBlink = res.getColor(R.color.w_colorItemBlink);
            colorWB = Color.BLACK;
            colorBW = Color.WHITE;
        }
    }

    static void themeToggled(Context context) {
        Resources res = context.getResources();
        if (PrefUtil.themeNumber == 1) {
            colorBlink = res.getColor(R.color.colorItemBlink);
        } else {
            colorBlink = res.getColor(R.color.w_colorItemBlink);
        }
    }

    static void applyInter(float inter) {
        currentInter = inter;
        colorBackground = getInterColor(white_colorBackground, black_colorBackground, inter);
        colorTime = getInterColor(white_colorTime, black_colorTime, inter);
        colorLog = getInterColor(white_colorLog, black_colorLog, inter);
        colorHighlight = getInterColor(white_colorHighlight, black_colorHighlight, inter);
        colorWB = getInterColor(Color.BLACK, Color.WHITE, inter);
        colorBW = getInterColor(Color.WHITE, Color.BLACK, inter);
        for (int i = 0; i < 7; i++) {
            WEEK_COLOR[i] = getInterColor(WHITE_WEEK_COLOR[i], BLACK_WEEK_COLOR[i], inter);
        }
    }

    public static void applyColor(RootLayout rootLayout, RecView recView, EditText editText,
                                  ImageButton menuButton, ImageButton themeButton, ImageButton undoButton) {
        rootLayout.setBackgroundColor(colorBackground);
        editText.setTextColor(colorWB);
        if (recView.getAdapter() != null) {
            recView.getAdapter().notifyDataSetChanged();
        }
        menuButton.setColorFilter(colorWB, android.graphics.PorterDuff.Mode.MULTIPLY);
        themeButton.setColorFilter(colorWB, android.graphics.PorterDuff.Mode.MULTIPLY);
        undoButton.setColorFilter(colorWB, android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private static int getInterColor(int colorWhite, int colorBlack, float inter) {
        int red = getInterValue(Color.red(colorWhite), Color.red(colorBlack), inter);
        int green = getInterValue(Color.green(colorWhite), Color.green(colorBlack), inter);
        int blue = getInterValue(Color.blue(colorWhite), Color.blue(colorBlack), inter);
        return Color.rgb(red, green, blue);
    }

    private static int getInterValue(int start, int target, float t) {
        return start + (int) (t * (target - start));
    }

    public static void setItemBackground(View itemView) {
        if (PrefUtil.themeNumber == 1) {
            itemView.setBackgroundResource(R.drawable.list_selector);
        } else {
            itemView.setBackgroundResource(R.drawable.w_list_selector);
        }
    }

}
