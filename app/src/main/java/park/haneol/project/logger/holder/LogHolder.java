package park.haneol.project.logger.holder;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActionManager;
import park.haneol.project.logger.util.ColorUtil;
import park.haneol.project.logger.util.PrefUtil;

public class LogHolder extends BaseHolder {

    private TextView timeView;
    private TextView logView;
    private View anchorView;

    public LogHolder(@NonNull View itemView, final ActionManager actionManager) {
        super(itemView);
        timeView = itemView.findViewById(R.id.textView_time);
        logView = itemView.findViewById(R.id.textView_log);
        anchorView = itemView.findViewById(R.id.view_anchor);
        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManager.onClickTime(getAdapterPosition());
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManager.onClickLogItem(getAdapterPosition(), anchorView);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                actionManager.onLongClickLogItem(getAdapterPosition(), anchorView);
                return true;
            }
        });
    }

    @Override
    public void setItem(BaseItem item) {
        LogItem logItem = (LogItem) item;
        timeView.setText(logItem.getTimeString());
        if (logItem.getFlag() == 1) {
            SpannableString spanText = new SpannableString(logItem.getText());
            spanText.setSpan(new ForegroundColorSpan(ColorUtil.colorBW), 0, spanText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new BackgroundColorSpan(ColorUtil.colorHighlight), 0, spanText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new StyleSpan(Typeface.BOLD), 0, spanText.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            logView.setText(spanText);
        } else {
            logView.setText(logItem.getText());
        }
    }

    @Override
    public void applyFontSize() {
        setPadding(timeView, PrefUtil.fontSize, 1, 1);
        setPadding(logView, PrefUtil.fontSize, 1, 1);
        setPadding(anchorView, PrefUtil.fontSize, 1, 1);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtil.fontSize);
        logView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, PrefUtil.fontSize);
    }

    @Override
    public void applyColor() {
        timeView.setTextColor(ColorUtil.colorTime);
        logView.setTextColor(ColorUtil.colorLog);
        ColorUtil.setItemBackground(itemView);
    }

    public void setItemInSearchMode(LogItem item, String searchString) {
        if (!item.contains(searchString)) {
            return;
        }
        SpannableString spanText = SpannableString.valueOf(logView.getText());
        ArrayList<Integer> indexList = findWord(spanText.toString().toLowerCase(), searchString);
        if (indexList.size() != 0) {
            int length = searchString.length();
            for (int i = 0; i < indexList.size(); i++) {
                int index = indexList.get(i);
                spanText.setSpan(new BackgroundColorSpan(ColorUtil.colorSearch), index, index + length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            logView.setText(spanText);
        }
    }

    private static ArrayList<Integer> findWord(String fromString, String searchString) {
        ArrayList<Integer> indexList = new ArrayList<>();
        int i = 0;
        while (i != -1) {
            i = fromString.indexOf(searchString, i);
            if (i != -1) {
                indexList.add(i);
                i++;
            }
        }
        return indexList;
    }

}
