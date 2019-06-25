package park.haneol.project.logger.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class UIUtil {

    public static int statusHeight = 0;
    public static boolean keypadShown;
    public static int fitCount;
    private static int insetsBottomBefore;
    public static boolean isPopupEditing;

    // activity.getWindow()
    public static void setKeypadShown(Window window, boolean shown) {
        if (shown) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        }
    }

    static void hideSoftInput(EditText editText) {
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void fitSystemWindows(View view, Rect insets) {
        if (Build.VERSION.SDK_INT >= 19) {
            // 스테이터스 바 높이
            if (statusHeight == 0 && insets.top != 0) {
                statusHeight = insets.top;
            }
            insets.top = 0;

            if (keypadShown && PrefUtil.onStartKeypad && !isPopupEditing && !isHardwareKeyboard(view.getContext())) {
                if (fitCount == 0) {
                    predictMargin(view, true);
                    insetsBottomBefore = PrefUtil.keypadHeight;
                    fitCount++;
                } else if (fitCount == 1) {
                    if (insets.bottom > 0) {
                        if (PrefUtil.keypadHeight != insets.bottom) {
                            PrefUtil.setKeypadHeight(view.getContext(), insets.bottom);
                        }
                        predictMargin(view, false);
                        fitCount++;
                    }
                }
            }

            // 1 단계 이후
            if (fitCount > 1) {
                trackKeypadState(insets.bottom);
            }
        }
    }

    public static void predictMargin(View view, boolean marginOn) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (marginOn && PrefUtil.keypadHeight != 0) {
            params.bottomMargin = PrefUtil.keypadHeight;
            view.setLayoutParams(params);
        } else if (params.bottomMargin != 0) {
            params.bottomMargin = 0;
            view.setLayoutParams(params);
        }
    }

    private static void trackKeypadState(int insetsBottomNew) {
        if (keypadShown && insetsBottomNew < insetsBottomBefore) {
            keypadShown = false;
        } else if (!keypadShown && insetsBottomNew > insetsBottomBefore) {
            keypadShown = true;
        }
        insetsBottomBefore = insetsBottomNew;
    }

    public static void onSizeChanged(int h, int oldh) {
        if (Build.VERSION.SDK_INT < 19) {
            if (keypadShown && h > oldh) {
                keypadShown = false;
            } else if (!keypadShown && h < oldh) {
                keypadShown = true;
            }
        }
    }

    private static boolean isHardwareKeyboard(Context context) {
        return context.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
    }

}
