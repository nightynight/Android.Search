package com.brokepal.searchdemo;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;

import com.brokepal.searchdemo.entity.Vocabulary;
import com.brokepal.searchdemo.utils.DatabaseTable;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseTable db = new DatabaseTable(this);
    SearchView searchView;
    ListView listView;
    String[] words;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=(ListView)findViewById(R.id.list);
        //给ListView设置监听器，点击每个item时把item的值作为搜索关键字
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String word=words[position];
                searchView.setQuery(word,true);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // 关联检索配置和SearchView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
        }

        //给SearchView设置监听器，以便更新ListView的内容
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { //搜索时会触发这个事件搜索即可
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()!=0){
                    //当searchView的内容改变时，更新ListView
                    updateListView(newText);
                }
                return false;
            }
        });
        return true;
    }

    public void updateListView(String newText){
        List<Vocabulary> vocabularies=db.getWordsMatches(newText);
        words=new String[vocabularies.size()];
        for (int i=0;i<vocabularies.size();i++){
            words[i]=vocabularies.get(i).getWord();
        }
        ArrayAdapter<String> aadapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, words);
        listView.setAdapter(aadapter);
    }
}
