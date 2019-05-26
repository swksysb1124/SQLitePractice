# SQLitePractice
練習Android系統的 SQLite API

## 定義資料庫契約

定義一個資料庫契約，包含**表格名稱**以及**資料欄位**
```java
public final class NewsContract {

    // 避免此Contract被實體化，將建構函式私有化
    private NewsContract() {}

    // 定義Entry：定義表格名稱及資料欄位
    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }

}
```

## 使用 SQLiteOpenHelper 

利用 `SQLiteOpenHelper` 取得資料庫實體

```java
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
```


## 開關資料庫
### 取得資料庫
```java
// 取得SQLiteOpenHelper實體
NewsDbHelper mDbHelper = new NewsDbHelper(context);

// 取得可寫入資料庫
SQLiteDatabase database = mDbHelper.getWritableDatabase();

// 取得可讀取資料庫
SQLiteDatabase database = mDbHelper.getReadableDatabase();
```

### 資料庫關閉
當不需要使用資料庫時，記得要關閉資料庫 (比如說在調用 Activity 被毀滅之前)
```java
@Override
protected void onDestroy() {
    mDbHelper.close();
    super.onDestroy();
}
```

## 資料操作

### 新增
```java
public void put(final String title, final String subtitle, final OnDataBaseCallback callback) {
    // 取得可寫入資料庫
    SQLiteDatabase database = mDbHelper.getWritableDatabase();

    // 將寫入資料包裹到 ContentValues 物件
    ContentValues data = new ContentValues();
    data.put(NewsContract.Entry.COLUMN_NAME_TITLE, title);
    data.put(NewsContract.Entry.COLUMN_NAME_SUBTITLE, subtitle);

    // 插入
    long newRowId = database.insert(
            NewsContract.Entry.TABLE_NAME, // 表格名稱
            null, // 如果插入資料為空，不進行插入動作
            data); // 資料

    // 插入錯誤
    if(newRowId == -1) {
        callback.onFail("插入錯誤: "+title);
    }else {
        callback.onSuccess("插入成功: "+title);
    }
}
```

### 查詢
```java
public void queryByTitle(final String title, final OnDataBaseCallback callback) {

    // 取得可讀取資料庫
    SQLiteDatabase database = mDbHelper.getReadableDatabase();

    // 指定此次查詢資料需要回傳的欄位
    String[] projection = {
            BaseColumns._ID,
            NewsContract.Entry.COLUMN_NAME_TITLE,
            NewsContract.Entry.COLUMN_NAME_SUBTITLE
    };

    // 資料過濾器
    // WHERE Clause
    // "WHERE title = '{title}'"
    String whereClause = NewsContract.Entry.COLUMN_NAME_TITLE + " = ?";
    String[] whereArgs = { title };

    // 排序
    String sortOrder = NewsContract.Entry.COLUMN_NAME_SUBTITLE + " DESC"; // 依據 Subtitle做排序

    // 查詢
    Cursor cursor = database.query(
            NewsContract.Entry.TABLE_NAME, // 表格名稱
            projection, // 查詢資料的欄位(null 表示回傳全部欄位)
            whereClause, // WHERE 鎖定的欄位
            whereArgs, // WHERE 指定的值
            null,
            null,
            sortOrder //排序
        );

    if(cursor.getCount() == 0) {
        callback.onFail("查無此資料");
        return;
    }

    cursor.moveToFirst(); // 取第一筆資料

    String qTitle =
        cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_TITLE));

    String qSubtitle =
        cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_SUBTITLE));

    News news = new News(qTitle, qSubtitle);

    cursor.close();

    callback.onSuccess(news);
}
```
#### 查詢全部資料
```java
public void queryAll(final OnDataBaseCallback callback) {

    ...

    // 查詢
    Cursor cursor = database.query(
            NewsContract.Entry.TABLE_NAME, // 表格名稱
            projection, // 查詢資料的欄位(null 表示回傳全部欄位)
            null, // WHERE 鎖定的欄位
            null, // WHERE 指定的值
            null,
            null,
            sortOrder //
        );

    if(cursor.getCount() == 0) {
        callback.onFail("查無此資料");
        return;
    }

    List<News> newsList = new ArrayList<>();

    while(cursor.moveToNext()) {
        String qTitle =
            cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_TITLE));

        String qSubtitle =
            cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_SUBTITLE));

        newsList.add(new News(qTitle, qSubtitle));
    }

    cursor.close();

    callback.onSuccess(newsList);
}
```

剛回傳回來的`Cursor`物件的索引起始值為 `-1`，在提取資料前，一定要做 move的動作(`moveToFirst()`, `moveToNext()`, etc...)才能取到資料。

### 更新
```java
public void updateTitle(final String oldTitle, final String newTitle, final OnDataBaseCallback callback) {
    // 取得可寫入資料庫
    SQLiteDatabase database = mDbHelper.getWritableDatabase();

    // 更新的資料
    ContentValues newData = new ContentValues();
    newData.put(NewsContract.Entry.COLUMN_NAME_TITLE, newTitle); // 更新 Title 欄位
    
    // 資料過濾器
    String where = NewsContract.Entry.COLUMN_NAME_TITLE + " LIKE ?";
    String[] whereArgs = {oldTitle};

    // 更新資料
    int updateRows = database.update(
            NewsContract.Entry.TABLE_NAME,
            newData,
            where,
            whereArgs);
        
    if(updateRows > 0) {
        callback.onSuccess("更新完成: "+newTitle);
    }else {
        callback.onFail("更新失敗: "+newTitle);
    }
}
```

### 刪除

```java
public void deleteByTitle(final String title, final OnDataBaseCallback callback) {
    // 取得可讀取資料庫
    SQLiteDatabase database = mDbHelper.getReadableDatabase();

    // 資料過濾器
    String whereClause = NewsContract.Entry.COLUMN_NAME_TITLE + " LIKE ?";
    String[] whereArgs = {title};

    // 刪除
    int deleteRows = database.delete(
            NewsContract.Entry.TABLE_NAME,
            whereClause,
            whereArgs
        );

    if(deleteRows > 0) {
        callback.onSuccess("刪除成功: "+title);
    }else {
        callback.onFail("無資料可刪除: "+title);
    }
}
```
#### 刪除全部資料
```java
public void deleteAllData(final OnDataBaseCallback callback) {
    // 取得可讀取資料庫
    SQLiteDatabase database = mDbHelper.getReadableDatabase();

    // 刪除
    database.delete(
        NewsContract.Entry.TABLE_NAME,
        null,
        null);

    callback.onSuccess("資料已全數刪除");
}
```

示範程式碼可以至 [Github](https://github.com/swksysb1124/SQLitePractice) 下載。

## 參考
- [Save data using SQLite](https://developer.android.com/training/data-storage/sqlite)
