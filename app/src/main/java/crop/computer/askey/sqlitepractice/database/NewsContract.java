package crop.computer.askey.sqlitepractice.database;

import android.provider.BaseColumns;


/**
 * 定義一個資料庫Contract，包含表格以及資料欄位`
 */
public final class NewsContract {

    // 避免此Contract被實體化，將建構函式私有化
    private NewsContract() {}

    // 定義表格欄位Entry
    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }

}
