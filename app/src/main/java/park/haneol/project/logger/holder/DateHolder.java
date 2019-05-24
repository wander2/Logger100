package park.haneol.project.logger.holder;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.item.DateItem;
import park.haneol.project.logger.util.ActionManager;
import park.haneol.project.logger.util.ColorUtil;
import park.haneol.project.logger.util.PrefUtil;

public class DateHolder extends BaseHolder {

    private TextView timeView;
    private TextView logView;
    private View anchorView;

    private int week = -1;

    public DateHolder(@NonNull View itemView, final ActionManager actionManager) {
        super(itemView);
        timeView = itemView.findViewById(R.id.textView_time);
        logView = itemView.findViewById(R.id.textView_log);
        anchorView = itemView.findViewById(R.id.view_anchor);
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                actionManager.onLongClickDateItem(getAdapterPosition(), anchorView);
                return true;
            }
        });
    }

    @Override
    public void setItem(BaseItem item) {
        String dateString = "     " + ((DateItem) item).getDateString();
        timeView.setText(dateString);

        week = ((DateItem) item).getWeek();
        timeView.setTextColor(ColorUtil.WEEK_COLOR[week]);
    }

    @Override
    public void applyFontSize() {
        setPadding(timeView, PrefUtil.fontSize, 8, 1);
        setPadding(logView, PrefUtil.fontSize, 8, 1);
        setPadding(anchorView, PrefUtil.fontSize, 8, 1);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtil.fontSize);
        logView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtil.fontSize);
    }

    @Override
    public void applyColor() {
        if (week != -1) {
            timeView.setTextColor(ColorUtil.WEEK_COLOR[week]);
        }
        ColorUtil.setItemBackground(itemView);
    }



}
