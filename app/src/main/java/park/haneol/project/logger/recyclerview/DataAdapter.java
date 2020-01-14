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
import park.haneol.project.logger.item.ItemList;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActionManager;

public class DataAdapter extends RecyclerView.Adapter<BaseHolder> {

    private static final int TYPE_LOG = 1;
    private static final int TYPE_DATE = 2;
    private static final int TYPE_SPACE = 3;

    private Context context;
    private ItemList bItemList;
    private ItemList itemList;
    private ActionManager actionManager;

    private String searchString;
    private int focusPosition;

    DataAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
    }

    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    public void setItemList(ItemList itemList) {
        this.bItemList = itemList;
    }

    // 표시용 리스트를 어떻게 처리할지 결정, onCreate, 시간대 재설정시 호출됨
    public void update(boolean isSearchMode) {
        if (isSearchMode) {
            // 검색 모드일 때 (onCreate 에서 처음 호출될 때는 searchString == null 이다. 즉 아무것도 안함)
            if (searchString != null) {
                search(searchString); // 시간대 재설정시 한 번 더 검색을 실행한다.
            }
        } else {
            // 일반 모드일 때
            itemList = bItemList;
            notifyDataSetChanged();
        }
    }

    // 검색모드 진입할 때 (onCreate -> search), 인풋이 바뀔 때 실행됨
    public void search(String searchString) {
        if (bItemList == null) {
            return;
        }

        this.searchString = searchString.toLowerCase();
        // 검색 내용이 ""일 경우 일반모드와 동일
        if (searchString.length() == 0) {
            itemList = bItemList;
            notifyDataSetChanged();
            return;
        }

        // 아이템 리스트 초기화 null: 처음, bItemList: search() 이전 상태가 전체 포커스인 경우
        // itemList 가 bItemList 와 다를 경우 : 유효 검색이 실행중인 상황
        if (itemList == null || itemList == bItemList) {
            itemList = new ItemList();
        } else {
            itemList.clear();
        }

        // 필터링
        itemList.filterFrom(bItemList, searchString);

        // 변경사항 적용
        notifyDataSetChanged();
    }

    public int searchNext() {
        if (itemList != bItemList) {
            int lastId = itemList.getLastItem().getId();
            focusPosition = bItemList.getPositionById(lastId);
            itemList = bItemList;
            return focusPosition;
        }
        focusPosition = itemList.getNextSearchPosition(focusPosition, searchString);
        return focusPosition;
    }

    public int getPositionById(int id) {
        return itemList.getPositionById(id);
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
        BaseItem item = itemList.getItemAt(position);
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
        BaseItem item = itemList.getItemAt(position);
        holder.setItem(item);
        holder.applyFontSize();
        holder.applyColor();

        // itemList != bItemList 는 다음 검색 기능 때 작용하지 않기 때문에 다른 방법 사용
        if (searchString != null && searchString.length() != 0) {
            if (item instanceof LogItem && holder instanceof LogHolder) {
                ((LogHolder) holder).setItemInSearchMode((LogItem) item, searchString);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return itemList.getItemAt(position).getId();
    }

    public void addItem(LogItem item) {
        int position = itemList.size();
        boolean isDateAdded = itemList.addItem(item);
        if (isDateAdded) {
            notifyItemRangeInserted(position, 2);
        } else {
            notifyItemInserted(position);
        }
    }

    // return : blink position
    public int restoreItem(LogItem item) {
        int position;
        int rPosI = itemList.getRestorePosition(item);
        if (rPosI >= itemList.size()) {
            addItem(item);
            position = itemList.size() - 1;
        } else {
            boolean isDateAdded = itemList.restoreItem(rPosI, item);
            // 날짜가 추가되면 rPosI++
            if (isDateAdded) {
                notifyItemRangeInserted(rPosI, 2);
                position = rPosI + 1;
            } else {
                notifyItemInserted(rPosI);
                position = rPosI;
            }
        }

        // 검색 상황일 때 원본 처리
        if (itemList != bItemList) {
            int rPosB = bItemList.getRestorePosition(item);
            if (rPosB >= bItemList.size()) {
                bItemList.addItem(item);
            } else {
                bItemList.restoreItem(rPosB, item);
            }
        }

        // 강조할 위치 리턴
        return position;
    }

    public void removeItem(int position) {
        LogItem item = (LogItem) itemList.getItemAt(position);

        // itemList 의 해당 position 위치 삭제 (위에 날짜 아이템 삭제해야 되면 그것도 삭제)
        boolean isDateRemoved = itemList.removeItem(position);

        // 날짜 아이템 삭제 여부 받아서 노티파이
        if (isDateRemoved) {
            notifyItemRangeRemoved(position - 1, 2);
        } else {
            notifyItemRemoved(position);
        }

        // 검색 상황일 때 원본 처리 (item1 활용)
        if (itemList != bItemList) {
            bItemList.removeItem(bItemList.getPositionById(item.getId()));
        }
    }

    public ArrayList<Integer> getIdsInDate(int position) {
        ArrayList<Integer> idList = new ArrayList<>();
        position++;
        while (itemList.getItemAt(position) instanceof LogItem) {
            idList.add(itemList.getItemAt(position).getId());
            position++;
        }
        return idList;
    }

    public void removeItems(ArrayList<Integer> idList) {
        for (int id: idList) {
            removeItem(itemList.getPositionById(id));
        }
    }

    public BaseItem getItemAt(int position) {
        return itemList.getItemAt(position);
    }

    // return : betweenId1, betweenId2
    // 0위치: [0, id]
    // n위치: [id, id+2]
    public int[] timeInsertFindBetween(int time) {
        // t[n-1] <= t 이면 n위치
        LogItem item = bItemList.getLastItem();
        if (item.getTime() <= time) {
            return new int[]{item.getId(), item.getId() + 2};
        }

        // for n-1 ~ 3
        // t[i-1] <= t 이면 i위치
        // 0:BaseItem, 1:DateItem, 2:LogItem, 3:LogItem
        LogItem itemUpper;
        for (int i = bItemList.size() - 1; i >= 3; i--) {
            if (bItemList.getItemAt(i) instanceof LogItem) {
                item = (LogItem) bItemList.getItemAt(i);
                itemUpper = bItemList.getLogItemAtOrUpper(i - 1);
                if (itemUpper.getTime() <= time) {
                    return new int[]{itemUpper.getId(), item.getId()};
                }
            }
        }

        // t < t[0] 이면 0위치
        item = bItemList.getFirstItem();
        if (time < item.getTime()) {
            return new int[]{0, item.getId()};
        }

        return null;
    }

    // return: no push => -1
    //            push => until id (between[1] ~ until id - 1) -> (between[1] + 1 ~ until id)
    //                    between[1] = between[0] + 1
    // insert id = between[0] + 1
    public int timeInsertFindPushUntil(int[] betweenIds) {
        if (betweenIds[1] - betweenIds[0] == 1) {
            int id = betweenIds[1];
            int p = bItemList.getPositionById(id);
            for (p += 1; p < bItemList.size(); p++) {
                if (bItemList.getItemAt(p) instanceof LogItem) {
                    id += 1;
                    if (bItemList.getItemAt(p).getId() > id) {
                        return id;
                    }
                }
            }
            id += 1;
            return id;
        }
        return -1;
    }

}
