package park.haneol.project.logger.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashSet;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.item.DateItem;
import park.haneol.project.logger.item.ItemList;
import park.haneol.project.logger.item.LogItem;

// 마지막 앱 버전 48
// 마지막 데이터베이스 버전 30 추정

public class Database extends SQLiteOpenHelper {

    private Context context;

    public static final String DATABASE_NAME  = "logger.db";
    public static final String DATABASE_NAME_HIDDEN  = "logger_hidden.db";
    private static final int DATABASE_VERSION  = 43;

    private static final String TABLE_LOG_LIST = "log_list";
    private static final String COL_LOG_ID     = "log_id";
    private static final String COL_TIME       = "time";
    private static final String COL_LOG        = "log";
    private static final String COL_FLAG       = "flag";

    public Database(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LOG_LIST + "(" +
                COL_LOG_ID + " INTEGER PRIMARY KEY," +
                COL_TIME   + " INTEGER," +
                COL_LOG    + " TEXT," +
                COL_FLAG   + " INTEGER DEFAULT 0)");
        if (getDatabaseName().equals(DATABASE_NAME_HIDDEN)) {
            insertHelp(db, context.getResources().getStringArray(R.array.db_help_hidden));
        } else {
            insertHelp(db, context.getResources().getStringArray(R.array.db_help));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 40) {
            db.execSQL("ALTER TABLE " + TABLE_LOG_LIST +
                    " ADD COLUMN " + COL_FLAG + " INTEGER DEFAULT 0");
            clearPrefData48(db);
        }
        if (oldVersion < DATABASE_VERSION && getDatabaseName().equals(DATABASE_NAME)) {
            insertNote(db, context.getString(R.string.patch_note));
        }
    }

    private void insertHelp(SQLiteDatabase db, String[] stringArray) {
        ContentValues values = new ContentValues();
        values.put(COL_TIME, TimeUtil.getCurrentTime());
        for (String text: stringArray) {
            values.put(COL_LOG, text);
            db.insert(TABLE_LOG_LIST, null, values);
        }
    }

    private void insertNote(SQLiteDatabase db, String string) {
        ContentValues values = new ContentValues();
        values.put(COL_TIME, TimeUtil.getCurrentTime());
        values.put(COL_LOG, string);
        db.insert(TABLE_LOG_LIST, null, values);
    }

    private void clearPrefData48(SQLiteDatabase db) {
        HashSet<String> accentSet = PrefUtil.getAccentSet(context);
        if (accentSet != null) {
            ContentValues values = new ContentValues();
            values.put(COL_FLAG, 1);
            for (String s: accentSet) {
                int logId = Integer.valueOf(s);
                db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(logId));
            }
        }
        PrefUtil.clear48(context);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public ItemList load() {
        SQLiteDatabase db = getWritableDatabase();
        ItemList itemList = new ItemList();
        itemList.add(new BaseItem()); // 맨 윗쪽 패딩용
        int days;
        int daysBefore = -1;
        Cursor cursor = db.query(TABLE_LOG_LIST, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            LogItem logItem = new LogItem(
                    getInt(cursor, COL_LOG_ID),
                    getInt(cursor, COL_TIME),
                    getString(cursor, COL_LOG),
                    getInt(cursor, COL_FLAG)
            );
            days = logItem.getDays();
            if (days != daysBefore) {
                itemList.add(new DateItem(logItem.getTime()));
                daysBefore = days;
            }
            itemList.add(logItem);
        }
        cursor.close();
        return itemList;
    }

    public LogItem insert(String text) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        int time = TimeUtil.getCurrentTime();
        values.put(COL_TIME, time);
        values.put(COL_LOG, text);
        int logId = (int) db.insert(TABLE_LOG_LIST, null, values);
        return new LogItem(logId, time, text);
    }

    void insertOfItem(LogItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LOG_ID, item.getId());
        values.put(COL_TIME, item.getTime());
        values.put(COL_LOG, item.getText());
        values.put(COL_FLAG, item.getFlag());
        db.insert(TABLE_LOG_LIST, null, values);
    }

    void updateText(int logId, String text) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LOG, text);
        db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(logId));
    }

    void updateFlag(int logId, int flag) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FLAG, flag);
        db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(logId));
    }

    void delete(int logId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOG_LIST, COL_LOG_ID + "=?", iArg(logId));
    }

    void deleteItems(ArrayList<Integer> idList) {
        SQLiteDatabase db = getWritableDatabase();
        for (Integer logId: idList) {
            db.delete(TABLE_LOG_LIST, COL_LOG_ID + "=?", iArg(logId));
        }
    }

    boolean existId(int logId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_LOG_LIST, null, COL_LOG_ID + "=?", iArg(logId), null, null, null);
        boolean isExist = cursor.getCount() != 0;
        cursor.close();
        return isExist;
    }

    void changeItemTime(int itemId, int afterTime, int afterId, int pushUntilId) {
        SQLiteDatabase db = getWritableDatabase();
        if (pushUntilId == -1) {
            ContentValues values = new ContentValues();
            values.put(COL_LOG_ID, afterId); // itemId -> afterId 로 변환
            values.put(COL_TIME, afterTime); // itemId:time -> afterTime 으로 변환
            db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(itemId));
        } else {
            db.beginTransaction();
            try {
                // 1
                // afterId ~ pushUntilId-1    =>    afterId+1 ~ pushUntilId
                ContentValues values = new ContentValues();
                for (int i = pushUntilId - 1; i >= afterId; i--) {
                    values.put(COL_LOG_ID, i + 1); // id++
                    db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(i));
                }

                values.clear();
                // 2
                // itemId -> afterId 로 변환
                // itemId:time -> afterTime 으로 변환
                values.put(COL_LOG_ID, afterId); // itemId -> afterId 로 변환
                values.put(COL_TIME, afterTime); // itemId:time -> afterTime 으로 변환
                db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(itemId));

                // commit
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static int getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndex(column));
    }

    private static String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndex(column));
    }

    private static String[] iArg(int i) {
        return new String[] {String.valueOf(i)};
    }

    private static String[] iArgs(int... iArr) {
        String[] args = new String[iArr.length];
        for (int i = 0; i < iArr.length; i++)
            args[i] = String.valueOf(iArr[i]);
        return args;
    }

    private static String[] sArgs(String... sArr) {
        return sArr;
    }
}
