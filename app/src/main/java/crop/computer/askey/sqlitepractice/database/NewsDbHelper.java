package crop.computer.askey.sqlitepractice.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * 利用SQLiteOpenHelper取得資料庫實體
 */
public class NewsDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "News.db";

    private static final String SQL_CREATE_ENTRIES
            = "CREATE TABLE "+ NewsContract.Entry.TABLE_NAME +
                "(" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE + " TEXT,"+
                    NewsContract.Entry.COLUMN_NAME_SUBTITLE + " TEXT"+
                ")";

    private static final String SQL_DELETE_ENTRIES
            = "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME;


    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 當資料庫在應用程式中第一次建立完成時回調
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    // 當資料庫需要被升級時被調用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    // 當資料庫需要被降級時調用
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
