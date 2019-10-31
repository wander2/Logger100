package park.haneol.project.logger.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

public class ShortcutEditText extends AppCompatEditText {

    private Listener listener;

    public ShortcutEditText(Context context) {
        super(context);
    }

    public ShortcutEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShortcutEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onBackPressed();
    }

    public String getString() {
        return getText() != null ? getText().toString() : "";
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            listener.onBackPressed();
        }
        return super.onKeyPreIme(keyCode, event);
    }

}
