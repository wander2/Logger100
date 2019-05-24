package park.haneol.project.logger.recyclerview;

import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import park.haneol.project.logger.util.PrefUtil;

public class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;

    private int scalePosition;
    private int scaleOffset;

    public ScaleListener(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.adapter = recyclerView.getAdapter();
        this.layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        scalePosition = layoutManager.findLastVisibleItemPosition();
        View view = layoutManager.findViewByPosition(scalePosition);
        scaleOffset = view == null ? 0 : view.getTop();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        PrefUtil.fontSize = Math.max(10.0f, Math.min(PrefUtil.fontSize * scaleFactor, 22.5f));
        adapter.notifyDataSetChanged();
        layoutManager.scrollToPositionWithOffset(scalePosition, scaleOffset);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        PrefUtil.setFontSize(recyclerView.getContext(), PrefUtil.fontSize);
        layoutManager.scrollToPositionWithOffset(scalePosition, scaleOffset);
    }

}
