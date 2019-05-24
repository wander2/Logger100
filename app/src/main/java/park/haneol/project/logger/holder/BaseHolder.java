package park.haneol.project.logger.holder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import park.haneol.project.logger.item.BaseItem;

public abstract class BaseHolder extends RecyclerView.ViewHolder {

    BaseHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void setItem(BaseItem item);

    public abstract void applyFontSize();

    public abstract void applyColor();

    Context getContext() {
        return itemView.getContext();
    }

    private int dpToPx(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }

    void setPadding(View view, float fontSize, int nTop, int nBottom) {
        int padding = dpToPx(fontSize / 6);
        view.setPadding(0, nTop * padding, 0, nBottom * padding);
    }

}
