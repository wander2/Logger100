package park.haneol.project.logger.item;

import java.util.ArrayList;

public class ItemList extends ArrayList<BaseItem> {

    public BaseItem getItemAt(int position) {
        if (position >= 0 && position < size()) {
            return get(position);
        }
        return new BaseItem();
    }







    public int getPositionById(int id) {
        for (int i = 0; i < size(); i++) {
            if (getItemAt(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    private boolean isFirstItemOfDay(int position) {
        BaseItem baseItem = getItemAt(position);
        BaseItem upperItem = getItemAt(position - 1);
        return baseItem instanceof LogItem && upperItem instanceof DateItem;
    }

    public LogItem getLastItem() {
        BaseItem lastItem = getItemAt(size() - 1);
        if (lastItem instanceof LogItem) {
            return (LogItem) lastItem;
        }
        return new LogItem();
    }

    public int getNextSearchPosition(int focusPosition, String searchString) {
        int position = -1;
        BaseItem item;
        for (int i = focusPosition - 1; i > 0; i--) {
            item = getItemAt(i);
            if (item instanceof LogItem) {
                if (((LogItem) item).getText().contains(searchString)) {
                    position = i;
                    break;
                }
            }
        }
        if (position == -1) {
            for (int j = size() - 1; j >= focusPosition; j--) {
                item = getItemAt(j);
                if (item instanceof LogItem) {
                    if (((LogItem) item).getText().contains(searchString)) {
                        position = j;
                        break;
                    }
                }
            }
        }
        return position;
    }







    // return : 날짜 추가 여부
    public boolean addItem(LogItem item) {
        LogItem lastItem = getLastItem();
        if (lastItem == null || item.getDays() != lastItem.getDays()) {
            add(new DateItem(item.getTime()));
            add(item);
            return true;
        }
        add(item);
        return false;
    }

    // return : 아이템, 날짜 순서대로 삽입될 기준 위치
    public int getRestorePosition(LogItem item) {
        LogItem lastItem = getLastItem();
        // 없거나 마지막 아이템의 아이디보다 큰 경우 -> 마지막에 삽입
        if (lastItem == null || lastItem.getId() < item.getId()) {
            return size();
        }
        // 마지막 아이템의 아이디보다 작은 경우 -> 중간에 삽입
        int i;
        for (i = 0; i < size(); i++) {
            BaseItem baseItem = getItemAt(i);
            if (baseItem instanceof LogItem) {
                // LogItem 인 경우 id 를 비교 (LogItem 의 id 가 파라미터 id 보다 크다)
                if (baseItem.getId() > item.getId()) {
                    // 첫 번째 아이템 && 다른날짜일 경우 위의 날짜 부분이 삽입 위치
                    if (isFirstItemOfDay(i) && ((LogItem) baseItem).getDays() != item.getDays()) {
                        return i - 1;
                    }
                    return i;
                }
            }
        }
        return -1;
    }

    // return : 날짜 추가 여부
    public boolean restoreItem(int position, LogItem item) {
        // 마지막에 삽입되는 경우는 adapter 에서 제외됨
        // position - 1 의 아이템이 LogItem 이면서 item 의 days 와 다를 경우 날짜를 추가한다.
        BaseItem baseItem = getItemAt(position - 1);
        if (baseItem instanceof LogItem) {
            if (((LogItem) baseItem).getDays() != item.getDays()) {
                add(position, item);
                add(position, new DateItem(item.getTime()));
                return true;
            }
        }
        add(position, item);
        return false;
    }

    // return : 날짜 제거 여부
    public boolean removeItem(int position) {
        BaseItem above = getItemAt(position - 1);
        BaseItem below = getItemAt(position + 1);
        if (above instanceof DateItem && !(below instanceof LogItem)) {
            remove(position);
            remove(position - 1);
            return true;
        }
        remove(position);
        return false;
    }









    // 검색 기능
    public void filterFrom(ItemList itemList, String searchString) {
        ArrayList<Integer> indexList = new ArrayList<>();
        boolean showDate = false; // 거꾸로 탐색하면서 발견하면 해당 날짜 추가
        if (itemList.size() != 0) {
            for (int i = itemList.size() - 1; i >= 0; i--) {
                BaseItem baseItem = itemList.getItemAt(i);
                if (baseItem instanceof LogItem) {
                    // 문자열이 해당 문자열에 포함되기만 하면 해당 아이템 포함
                    if (((LogItem) baseItem).contains(searchString)) {
                        indexList.add(i);
                        showDate = true;
                    }
                } else if (baseItem instanceof DateItem) {
                    if (showDate) {
                        indexList.add(i);
                        showDate = false;
                    }
                } else {
                    indexList.add(i);
                }
            }
        }

        // 역순으로 추가했기 때문에 순서를 거꾸로 뒤집음
        if (indexList.size() != 0) {
            for (int j = indexList.size() - 1; j >= 0; j--) {
                add(itemList.getItemAt(indexList.get(j)));
            }
        }
    }
}
