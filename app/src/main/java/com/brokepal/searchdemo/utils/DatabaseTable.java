package com.brokepal.searchdemo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.brokepal.searchdemo.R;
import com.brokepal.searchdemo.entity.Vocabulary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchao on 16/8/18.
 * 该工具类通过解析res/raw下的txt文件，创建一个虚拟表，并提供查询方法
 * 虚拟表与SQLite表的运行方式类似，但虚拟表是通过回调来向内存中的对象进行读取和写入
 * 提供两个查询方法，分别返回一个Vocabulary对象列表和一个Vocabulary对象
 */
public class DatabaseTable {
    private static final String TAG = "DictionaryDatabase";

    //字典的表中将要包含的列名
    public static final String COL_WORD = "WORD";
    public static final String COL_DEFINITION = "DEFINITION";

    private static final String DATABASE_NAME = "DICTIONARY";   //数据库名
    private static final String FTS_VIRTUAL_TABLE = "FTS";  //表名
    private static final int DATABASE_VERSION = 1;

    private final DatabaseOpenHelper mDatabaseOpenHelper;

    public DatabaseTable(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context);
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        //创建虚拟表的SQL语句
        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_WORD + ", " +
                        COL_DEFINITION + ")";

        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadDictionary();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }


        //下面的代码会向你展示如何读取一个内容为单词和解释的文本文件(位于res/raw/definitions.txt)，如何解析文件与如何将文件中的数据按行插入虚拟表中。
        //为防止UI锁死这些操作会在另一条线程中执行。
        private void loadDictionary() {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        loadWords();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        /**
         * 解析txt文件中的内容，并将解析后的单词记录添加到虚拟表中
         * @throws IOException
         */
        private void loadWords() throws IOException {
            final Resources resources = mHelperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.definitions);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "-");
                    long id = addWord(strings[0].trim(), strings[1].trim());
                    if (id < 0) {
                        Log.e(TAG, "unable to add word: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
        }

        /**
         * 添加单词到虚拟表中
         * @param word
         * @param definition
         * @return
         */
        private long addWord(String word, String definition) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_WORD, word);
            initialValues.put(COL_DEFINITION, definition);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }

    /**
     * 模糊查询，使用头匹配，用来提供一个list,最多只取10条数据
     * @param query 查询关键字
     * @return List<Vocabulary>
     */
    public List<Vocabulary> getWordsMatches(String query) {
        List<Vocabulary> list=new ArrayList<Vocabulary>();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String selection = COL_WORD + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};
        String[] columns=new String[]{COL_WORD,COL_DEFINITION};

        Cursor cursor = builder.query(db, columns, selection, selectionArgs, null, null, null);
        Vocabulary vocabulary;
        int count=0;
        //最多只取10条数据
        while(cursor.moveToNext() && count < 10){
            String word = cursor.getString(cursor.getColumnIndex("WORD"));
            String definition = cursor.getString(cursor.getColumnIndex("DEFINITION"));
            vocabulary=new Vocabulary(word,definition);
            list.add(vocabulary);
            count++;
        }
        return list;
    }

    /**
     * 精确查询，根据查询条件返回一个Vocabulary对象
     * @param query 查询关键字
     * @return Vocabulary
     */
    public Vocabulary getWord(String query) {
        Vocabulary vocabulary = new Vocabulary();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        String selection = COL_WORD + " = ?";
        String[] selectionArgs = new String[] {query};
        String[] columns=new String[]{COL_WORD,COL_DEFINITION};

        Cursor cursor = builder.query(db, columns, selection, selectionArgs, null, null, null);
        while(cursor.moveToNext()){
            String word = cursor.getString(cursor.getColumnIndex("WORD"));
            String definition = cursor.getString(cursor.getColumnIndex("DEFINITION"));
            vocabulary=new Vocabulary(word,definition);
        }
        return vocabulary;
    }
}
