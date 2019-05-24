package park.haneol.project.logger.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import park.haneol.project.logger.R;
import park.haneol.project.logger.recyclerview.RecView;
import park.haneol.project.logger.recyclerview.ScaleListener;
import park.haneol.project.logger.util.UIUtil;

public class RootLayout extends ConstraintLayout {

    private ScaleGestureDetector scaleDetector;

    public RootLayout(Context context) {
        super(context);
        init();
    }

    public RootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= 19) {
            setFitsSystemWindows(true);
        }

        post(new Runnable() {
            @Override
            public void run() {
                // 스케일 디텍터 적용
                scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener((RecView) findViewById(R.id.rec_view)));

                // 메뉴 마진 적용
                if (Build.VERSION.SDK_INT >= 19) {
                    View menuButton = findViewById(R.id.menu_button);
                    LayoutParams params = (LayoutParams) menuButton.getLayoutParams();
                    params.topMargin += UIUtil.statusHeight;
                    menuButton.setLayoutParams(params);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (scaleDetector != null) {
            scaleDetector.onTouchEvent(e);
            if (scaleDetector.isInProgress()) {
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    protected final boolean fitSystemWindows(Rect insets) {
        UIUtil.fitSystemWindows(this, insets);
        return super.fitSystemWindows(insets);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        UIUtil.onSizeChanged(h, oldh);
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
