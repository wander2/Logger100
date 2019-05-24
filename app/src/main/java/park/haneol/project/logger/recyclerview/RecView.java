package park.haneol.project.logger.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import park.haneol.project.logger.util.ColorUtil;

public class RecView extends RecyclerView {

    private DataAdapter adapter;
    private DataLayoutManager layoutManager;
    private BlinkAnimation blinkAnimation = new BlinkAnimation();

    public RecView(@NonNull Context context) {
        super(context);
        init(null, 0);
    }

    public RecView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RecView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(@Nullable AttributeSet attrs, int defStyle) {
        setHasFixedSize(true);

        layoutManager = new DataLayoutManager(getContext(), attrs, defStyle);
        setLayoutManager(layoutManager);

        adapter = new DataAdapter(getContext());
        setAdapter(adapter);
    }

    public void scrollDown() {
        int lastPosition = adapter.getItemCount() - 1;
        if (canScrollVertically(1)) {
            smoothScrollToPosition(lastPosition);
        } else {
            scrollToPosition(lastPosition);
        }
    }

    public void scrollToItemPosition(final int position) {
        int extent = computeVerticalScrollExtent();
        if (extent == 0) {
            // 어떠한 이유로 인해 extent == 0일 경우 (예: 뷰 생성시)
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToItemPosition(position);
                }
            }, 100);
        } else {
            layoutManager.scrollToPositionWithOffset(position, extent/2);
            startBlinkAnimation(position);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!canScrollVertically(1)) {
            scrollDown();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }










    private class DataLayoutManager extends LinearLayoutManager {

        DataLayoutManager(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle, 0);
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return true;
        }

    }









    public void startBlinkAnimation(final int position) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                blinkAnimation.start(position);
            }
        }, blinkAnimation.DELAY);
    }






    private class BlinkAnimation extends Animation implements Animation.AnimationListener {

        int DELAY = 250;
        int DURATION = 1750;

        int mPosition;
        View mItemView;

        BlinkAnimation() {
            setDuration(DURATION);
            setInterpolator(new LinearInterpolator());
            setAnimationListener(this);
        }

        void start(int position) {
            mPosition = position;

            // 기존
            if (mItemView != null) {
                mItemView.clearAnimation();
                ColorUtil.setItemBackground(mItemView);
            }

            ViewHolder holder = findViewHolderForLayoutPosition(position);
            if (holder != null) {
                mItemView = holder.itemView;
                mItemView.startAnimation(this);
            } else {
                mItemView = null;
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mPosition = -1;
            if (mItemView != null) {
                mItemView.clearAnimation();
                ColorUtil.setItemBackground(mItemView);
            }
        }

        @Override
        protected void applyTransformation(float t, Transformation transformation) {
            if (mItemView != null) {
                int color = ColorUtil.colorBlink;
                int alpha = Math.round(255 * (1 - t));
                int timeColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
                mItemView.setBackgroundColor(timeColor);
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {}
        @Override
        public void onAnimationRepeat(Animation animation) {}
    }

}
