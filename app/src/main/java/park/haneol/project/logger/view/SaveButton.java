package park.haneol.project.logger.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

public class SaveButton extends AppCompatButton {

    public SaveButton(Context context) {
        super(context);
    }

    public SaveButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SaveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View.OnClickListener mOnClickListener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mOnClickListener = l;
    }

    public OnClickListener getOnClickListener() {
        return mOnClickListener;
    }


    public View.OnLongClickListener mOnLongClickListener;

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        super.setOnLongClickListener(l);
        mOnLongClickListener = l;
    }

    public OnLongClickListener getOnLongClickListener() {
        return mOnLongClickListener;
    }

}
