package park.haneol.project.logger.item;

import android.text.Spannable;

import park.haneol.project.logger.util.TimeUtil;

public class DateItem extends BaseItem {

    private int time;

    public DateItem(int time) {
        this.time = time;
    }

    @Override
    public int getId() {
        return -TimeUtil.getLocalDays(time);
    }

    public Spannable getDateString() {
        return TimeUtil.getDateString(time);
    }

    public String getDateFormat() {
        return TimeUtil.getDefaultDateFormat(time);
    }

}
