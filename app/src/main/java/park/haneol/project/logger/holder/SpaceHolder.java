package park.haneol.project.logger.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.util.UIUtil;

public class SpaceHolder extends BaseHolder {

    public SpaceHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(0, UIUtil.statusHeight));
    }

    @Override
    public void setItem(BaseItem item) {}

    @Override
    public void applyFontSize() {}

    @Override
    public void applyColor() {}

}
