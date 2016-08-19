package com.brokepal.searchdemo;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.brokepal.searchdemo.entity.Vocabulary;
import com.brokepal.searchdemo.utils.DatabaseTable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenchao on 16/8/18.
 */
public class SearchResultsActivity extends AppCompatActivity {
    private DatabaseTable db = new DatabaseTable(this);
    private TextView textView;
    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result);
        textView=(TextView)findViewById(R.id.textView);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //通过某种方法，根据请求检索你的数据
            Log.i(AppConstants.DEBUG_TAG,"-------输入的搜索关键字为："+query);

            Vocabulary vocabulary=db.getWord(query);
            if (vocabulary.getWord()==null){
                textView.setText("没有匹配的数据...");
            }else {
                textView.setText(vocabulary.getWord()+":"+vocabulary.getDefinition());
            }
        }
    }
}
