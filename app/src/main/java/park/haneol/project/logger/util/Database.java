package park.haneol.project.logger.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
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

    // Resource 가져오기용
    private Context context;

    //%% database name, version
    public static final String DATABASE_NAME  = "logger.db";
    public static final String DATABASE_NAME_HIDDEN  = "logger_hidden.db";
    private static final int DATABASE_VERSION  = 45;

    //%% table, column name
    private static final String TABLE_LOG_LIST = "log_list";
    private static final String COL_LOG_ID     = "log_id";
    private static final String COL_TIME       = "time";
    private static final String COL_LOG        = "log";
    private static final String COL_FLAG       = "flag";

    // 생성자
    public Database(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);

        // Resource 가져오기용
        this.context = context;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Database
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LOG_LIST + "(" +
                COL_LOG_ID + " INTEGER PRIMARY KEY," +
                COL_TIME   + " INTEGER," +
                COL_LOG    + " TEXT," +
                COL_FLAG   + " INTEGER DEFAULT 0)");

        // help
        Resources res = context.getResources();
        if (getDatabaseName().equals(DATABASE_NAME_HIDDEN)) {
            for (String text: res.getStringArray(R.array.db_help_hidden)) {
                append(db, text);
            }
        } else {
            for (String text: res.getStringArray(R.array.db_help)) {
                append(db, text);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 40) {
            db.execSQL("ALTER TABLE " + TABLE_LOG_LIST +
                       " ADD COLUMN " + COL_FLAG + " INTEGER DEFAULT 0");

            // 옛날버전 청소
            clearPrefData48(db);
        }

        // patch note
        if (getDatabaseName().equals(DATABASE_NAME)) {
            if (oldVersion < 43) {
                append(db, context.getString(R.string.patch_note_43)); // 시간 변경
            }
            if (oldVersion < 44) {
                append(db, context.getString(R.string.patch_note_44)); // 링크 열기
            }
            if (oldVersion < 45) {
                append(db, context.getString(R.string.patch_note_45)); // 시간 저장 시점
            }
        }
    }

    // 옛날버전 청소
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
    // Public 1
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

    public LogItem append(String text) {
        SQLiteDatabase db = getWritableDatabase();
        return append(db, text);
    }

    public LogItem append(String text, int time) {
        SQLiteDatabase db = getWritableDatabase();
        return append(db, text, time);
    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOG_LIST, COL_LOG_ID + "=?", iArg(id));
    }

    public void delete(ArrayList<Integer> ids) {
        SQLiteDatabase db = getWritableDatabase();
        for (int id: ids) {
            db.delete(TABLE_LOG_LIST, COL_LOG_ID + "=?", iArg(id));
        }
    }

    public boolean existId(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return existId(db, id);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Public 2
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // false -> push
    boolean insertItem(LogItem item) {
        SQLiteDatabase db = getWritableDatabase();
        if (existId(db, item.getId())) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COL_LOG_ID, item.getId());
        values.put(COL_TIME, item.getTime());
        values.put(COL_LOG, item.getText());
        values.put(COL_FLAG, item.getFlag());
        db.insert(TABLE_LOG_LIST, null, values);
        return true;
    }

    // id 부터 count 개 행의 id 를 1씩 올림 (id, id+1, ..., id+count-1 -> id+1, id+2, ..., id+count)
    // false -> id+count 부분 처리해서 다시
    // 이 함수를 실행하기 이전에 조작할 아이템은 아이디를 -1로 조정하고 조작한 후 새 아이디로 변경해야 됨
    boolean pushId(int controlId, int toId, int fromId, int count) {
        SQLiteDatabase db = getWritableDatabase();
        if (!existId(db, fromId + count)) {
            return false;
        }

        db.beginTransaction();
        try {
            updateId(db, controlId, -1);

            // push
            for (int i = fromId + count - 1; i >= fromId; i--) {
                updateId(db, i, i + 1);
            }

            updateId(db, -1, toId);

            // commit
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    boolean pushId(int fromId, int count) {
        SQLiteDatabase db = getWritableDatabase();
        if (!existId(db, fromId + count)) {
            return false;
        }

        db.beginTransaction();
        try {
            // push
            for (int i = fromId + count - 1; i >= fromId; i--) {
                updateId(db, i, i + 1);
            }

            // commit
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UPDATE
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void updateId(int id, int toId) {
        SQLiteDatabase db = getWritableDatabase();
        updateId(db, id, toId);
    }

    void updateTime(int id, int time) {
        SQLiteDatabase db = getWritableDatabase();
        updateTime(db, id, time);
    }

    void updateText(int id, String text) {
        SQLiteDatabase db = getWritableDatabase();
        updateText(db, id, text);
    }

    void updateFlag(int id, int flag) {
        SQLiteDatabase db = getWritableDatabase();
        updateFlag(db, id, flag);
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // Database
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    private static boolean existId(SQLiteDatabase db, int id) {
        Cursor cursor = db.query(TABLE_LOG_LIST, null, COL_LOG_ID + "=?", iArg(id), null, null, null);
        boolean isExist = cursor.getCount() != 0;
        cursor.close();
        return isExist;
    }

    private static LogItem append(SQLiteDatabase db, String text) {
        int time = TimeUtil.getCurrentTime();
        return append(db, text, time);
    }

    private static LogItem append(SQLiteDatabase db, String text, int time) {
        ContentValues values = new ContentValues();
        values.put(COL_TIME, time);
        values.put(COL_LOG, text);
        int id = (int) db.insert(TABLE_LOG_LIST, null, values);
        return new LogItem(id, time, text);
    }

    private static void updateId(SQLiteDatabase db, int id, int toId) {
        ContentValues values = new ContentValues();
        values.put(COL_LOG_ID, toId);
        db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(id));
    }

    private static void updateTime(SQLiteDatabase db, int id, int time) {
        ContentValues values = new ContentValues();
        values.put(COL_TIME, time);
        db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(id));
    }

    private static void updateText(SQLiteDatabase db, int id, String text) {
        ContentValues values = new ContentValues();
        values.put(COL_LOG, text);
        db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(id));
    }

    private static void updateFlag(SQLiteDatabase db, int id, int flag) {
        ContentValues values = new ContentValues();
        values.put(COL_FLAG, flag);
        db.update(TABLE_LOG_LIST, values, COL_LOG_ID + "=?", iArg(id));
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // Cursor
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    private static String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndex(column));
    }

    private static int getInt(Cursor cursor, String column) {
        return cursor.getInt(cursor.getColumnIndex(column));
    }

    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // Argument
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
