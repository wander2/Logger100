package park.haneol.project.logger.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import park.haneol.project.logger.R;
import park.haneol.project.logger.holder.BaseHolder;
import park.haneol.project.logger.holder.DateHolder;
import park.haneol.project.logger.holder.LogHolder;
import park.haneol.project.logger.holder.SpaceHolder;
import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.item.DateItem;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActionManager;

public class DataAdapter extends RecyclerView.Adapter<BaseHolder> {

    private static final int TYPE_LOG = 1;
    private static final int TYPE_DATE = 2;
    private static final int TYPE_SPACE = 3;

    private Context context;
    private ArrayList<BaseItem> itemList;
    private ActionManager actionManager;

    DataAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
    }

    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    public void setItemList(ArrayList<BaseItem> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    public BaseItem getItemAt(int position) {
        if (position >= 0 && position < itemList.size()) {
            return itemList.get(position);
        }
        return new BaseItem();
    }

    @Override
    public int getItemCount() {
        if (itemList != null) {
            return itemList.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        BaseItem item = getItemAt(position);
        if (item instanceof LogItem) {
            return TYPE_LOG;
        } else if (item instanceof DateItem) {
            return TYPE_DATE;
        }
        return TYPE_SPACE;
    }

    private View getItemView(ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
    }

    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOG) {
            return new LogHolder(getItemView(parent), actionManager);
        } else if (viewType == TYPE_DATE) {
            return new DateHolder(getItemView(parent), actionManager);
        }
        return new SpaceHolder(new View(context));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, int position) {
        holder.setItem(getItemAt(position));
        holder.applyFontSize();
        holder.applyColor();
    }

    @Override
    public long getItemId(int position) {
        return getItemAt(position).getId();
    }

    public void addItem(LogItem item) {
        BaseItem lastItem = getItemAt(itemList.size() - 1);
        int position = itemList.size();
        if (!(lastItem instanceof LogItem && item.getDays() == ((LogItem) lastItem).getDays())) {
            itemList.add(new DateItem(item.getTime()));
            itemList.add(item);
            notifyItemRangeInserted(position, 2);
        } else {
            itemList.add(item);
            notifyItemInserted(position);
        }
    }



    public void addItemAt(LogItem item, int position, boolean dateRemoved) {
        if (dateRemoved) {
            itemList.add(position - 1, new DateItem(item.getTime()));
            itemList.add(position, item);
            notifyItemRangeInserted(position - 1, 2);
        } else {
            itemList.add(position, item);
            notifyItemInserted(position);
        }
    }



    // return: date removed
    public boolean removeItem(int position) {
        BaseItem above = getItemAt(position - 1);
        BaseItem below = getItemAt(position + 1);
        if (above instanceof DateItem && !(below instanceof LogItem)) {
            itemList.remove(position);
            itemList.remove(position - 1);
            notifyItemRangeRemoved(position - 1, 2);
            return true;
        } else {
            itemList.remove(position);
            notifyItemRemoved(position);
            return false;
        }
    }


    public ArrayList<Integer> removeDate(int position) {
        if (!(getItemAt(position) instanceof DateItem)) {
            return null;
        }

        itemList.remove(position);
        BaseItem item;
        ArrayList<Integer> idList = new ArrayList<>();
        int count = 1;
        while (true) {
            item = getItemAt(position);
            if (!(item instanceof LogItem)) {
                break;
            }
            idList.add(itemList.remove(position).getId());
            count++;
        }
        notifyItemRangeRemoved(position, count);

        return idList;
    }

}
