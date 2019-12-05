package park.haneol.project.logger.item;

import park.haneol.project.logger.util.TimeUtil;

public class LogItem extends BaseItem {

    private int id;
    private int time;
    private String text;
    private int flag;

    public LogItem() {

    }

    public LogItem(int id, int time, String text) {
        this(id, time, text, 0);
    }

    public LogItem(int id, int time, String text, int flag) {
        this.id = id;
        this.time = time;
        this.text = text;
        this.flag = flag;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public int getFlag() {
        return flag;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getDays() {
        return TimeUtil.getLocalDays(time);
    }

    public boolean contains(String string) {
        return text.toLowerCase().contains(string);
    }

    public String getTimeString() {
        return TimeUtil.getTimeString(time);
    }
}
