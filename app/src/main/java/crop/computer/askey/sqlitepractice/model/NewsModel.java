package crop.computer.askey.sqlitepractice.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import crop.computer.askey.sqlitepractice.database.NewsContract;
import crop.computer.askey.sqlitepractice.database.NewsDbHelper;

/**
 * 定義資料存取
 */

public class NewsModel {

    private NewsDbHelper mDbHelper;

    public NewsModel(Context context) {
        this.mDbHelper = new NewsDbHelper(context);
    }

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

    public void updateTitle(final String oldTitle, final String newTitle, final OnDataBaseCallback callback) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        ContentValues newData = new ContentValues();
        newData.put(NewsContract.Entry.COLUMN_NAME_TITLE, newTitle);

        String where = NewsContract.Entry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] whereArgs = {oldTitle};

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
        // WHERE "title" = '{title}'
        String whereClause = NewsContract.Entry.COLUMN_NAME_TITLE + " = ?";
        String[] whereArgs = { title };

        // 排序
        String sortOrder = NewsContract.Entry.COLUMN_NAME_SUBTITLE + " DESC";

        // 查詢
        Cursor cursor = database.query(
                NewsContract.Entry.TABLE_NAME, // 表格名稱
                projection, // 查詢資料的欄位(null 表示回傳全部欄位)
                whereClause, // WHERE 鎖定的欄位
                whereArgs, // WHERE 指定的值
                null,
                null,
                sortOrder //
            );

        if(cursor.getCount() == 0) {
            callback.onFail("查無此資料");
            return;
        }

        cursor.moveToFirst();

        String newsTitle =
                cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_TITLE));

        String newsSubtitle =
                cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_SUBTITLE));

        News news = new News(newsTitle, newsSubtitle);

        cursor.close();

        callback.onSuccess(news);
    }

    public void queryAll(final OnDataBaseCallback callback) {

        // 取得可讀取資料庫
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // 指定此次查詢資料需要回傳的欄位
        String[] projection = {
                BaseColumns._ID,
                NewsContract.Entry.COLUMN_NAME_TITLE,
                NewsContract.Entry.COLUMN_NAME_SUBTITLE
        };

        // ORDER
        String sortOrder = NewsContract.Entry.COLUMN_NAME_TITLE + " DESC";

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
            String newsTitle =
                    cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_TITLE));

            String newsSubtitle =
                    cursor.getString(cursor.getColumnIndexOrThrow(NewsContract.Entry.COLUMN_NAME_SUBTITLE));

            newsList.add(new News(newsTitle, newsSubtitle));
        }

        cursor.close();

        callback.onSuccess(newsList);
    }

    public void deleteByTitle(final String title, final OnDataBaseCallback callback) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        String whereClause = NewsContract.Entry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] whereArgs = {title};

        int deleteRows = database.delete(
                NewsContract.Entry.TABLE_NAME,
                whereClause,
                whereArgs);

        if(deleteRows > 0) {
            callback.onSuccess("刪除成功: "+title);
        }else {
            callback.onFail("無資料可刪除: "+title);
        }
    }

    public void deleteAllData(final OnDataBaseCallback callback) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        database.delete(
                NewsContract.Entry.TABLE_NAME,
                null,
                null);

        callback.onSuccess("資料已全數刪除");
    }

    public void close() {
        mDbHelper.close();
    }

    public interface OnDataBaseCallback {
        void onSuccess(Object data);
        void onFail(String error);
    }
}
