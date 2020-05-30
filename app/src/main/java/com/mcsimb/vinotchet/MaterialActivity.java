package com.mcsimb.vinotchet;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material);
        FileUtils db = FileUtils.getInstance();
        Intent intent = getIntent();
        String[] dataList = intent.getStringArrayExtra("data");
        String[] tList = getResources().getStringArray(R.array.material_title_list);
        ArrayList<String> titleList = new ArrayList<String>();
        Collections.addAll(titleList, tList);
        for (String s : db.winesList.keySet()) {
            titleList.add(s + " 0,5");
            titleList.add(s + " 0,7");
        }
        Map<String, String> map;
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i = 0; i < dataList.length; i++) {
            map = new HashMap<String, String>();
            map.put("title", titleList.get(i));
            map.put("data", dataList[i]);
            data.add(map);
        }
        ListAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.material_list_item,
                new String[]{"title", "data"},
                new int[]{R.id.text_mat_title, R.id.text_mat_data}
        );
        setListAdapter(adapter);
    }
}
