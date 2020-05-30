package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StampsActivity extends ListActivity implements ActionBar.TabListener {

    private static final String VOL_5 = "0.5";
    private static final String VOL_7 = "0.7";
    private static final String LETTER_5 = "Ю";
    private static final String LETTER_7 = "Я";

    private FileUtils db;
    private String curVol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stamps);
        curVol = VOL_5;
        db = FileUtils.getInstance();

        Intent intent = getIntent();
        List<String> days = intent.getStringArrayListExtra("days");

        if (days != null) {
            ActionBar bar = getActionBar();
            if (bar != null) {
                bar.setDisplayHomeAsUpEnabled(true);
                bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);

                for (String day : days) {
                    ActionBar.Tab tab = bar.newTab();
                    tab.setTag(day);
                    bar.addTab(tab.setText(day + "." + db.MONTH).setTabListener(this), false);
                }
            }

            setList(days.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stamps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_vol_stamps:
                if (curVol.equals(VOL_5)) {
                    item.setTitle(R.string.volume5);
                    curVol = VOL_7;
                } else {
                    item.setTitle(R.string.volume7);
                    curVol = VOL_5;
                }
                ActionBar bar = getActionBar();
                if (bar != null) {
                    ActionBar.Tab tab = bar.getTabAt(0);
                    tab.select();
                    setList((String) tab.getTag());
                }
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        setList((String) tab.getTag());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @SuppressLint("DefaultLocale")
    private void setList(String day) {
        List<Integer[]> numbers = new ArrayList<>();
        String startStamp;
        int current = 0;
        String chr;
        numbers.add(new Integer[]{0, current});
        if (curVol.equals(VOL_5)) {
            chr = LETTER_5;
            startStamp = db.stamps05.get(current);
        } else {
            chr = LETTER_7;
            startStamp = db.stamps07.get(current);
        }
        int startRange = toInt(startStamp, 0, 3);
        int startNumber = toInt(startStamp, 9) % 500;
        int atBegin = (startRange / 30 * 30 + 30 - startRange) * 500 + 500 - (startNumber % 500) + 1;
        int sum = 0;

        for (String[] data : db.dataBase) {
            if (data[2].equals(curVol)) {
                if (data[0].equals(day)) {
                    sum += Integer.decode(data[4]);
                    if (atBegin - Integer.decode(data[4]) > 0) {
                        numbers.get(numbers.size() - 1)[0] += Integer.decode(data[4]);
                        numbers.get(numbers.size() - 1)[1] = current;
                    } else {
                        numbers.get(numbers.size() - 1)[0] += atBegin;
                        numbers.get(numbers.size() - 1)[1] = current;
                        numbers.add(new Integer[]{Integer.decode(data[4]) - atBegin, current + 1});
                    }
                } else if (Integer.decode(data[0]) > Integer.decode(day)) {
                    break;
                }
                atBegin -= Integer.decode(data[4]);
                if (atBegin < 0) {
                    atBegin += 15000;
                    current++;
                }
            }
        }

        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> map;
        map = new HashMap<>();
        map.put("sum", "");
        map.put("start", "");
        map.put("end", "");
        map.put("total", "Акцизная марка " + curVol);
        list.add(map);
        map = new HashMap<>();
        map.put("sum", "");
        map.put("start", "");
        map.put("end", "");
        map.put("total", "_____________________");
        list.add(map);

        if (sum > 0) {
            for (int i = 0; i < numbers.size(); i++) {
                String s;
                if (curVol.equals(VOL_5)) {
                    s = db.stamps05.get(numbers.get(i)[1]);
                } else {
                    s = db.stamps07.get(numbers.get(i)[1]);
                }
                String rangeStart;
                String numStart;
                String rangeEnd;
                String numEnd;
                int num1 = toInt(s, 3, 9);
                int num2start = 500 - (atBegin + sum) % 500 + 1;
                int num2end = 500 - atBegin % 500;
                if (num2start == 500 || num2end == 500) {
                    num1++;
                }
                if (toInt(s, 9) > 500) {
                    num2start += 500;
                    num2end += 500;
                }
                String numEnd1 = String.format("%06d", num1) + String.format("%03d", num2end);
                String numStart1 = String.format("%06d", num1) + String.format("%03d", num2start);
                if (numbers.size() == 1) {
                    rangeStart = String.format("%03d", toInt(s, 0, 3) / 30 * 30 + 30 - (atBegin + sum) / 500);
                    rangeEnd = String.format("%03d", toInt(s, 0, 3) / 30 * 30 + 30 - atBegin / 500);
                    numStart = numStart1;
                    numEnd = numEnd1;
                } else if (i == 0) {
                    rangeStart = String.format("%03d", toInt(s, 0, 3) / 30 * 30 + 30 - (numbers.get(i)[0] / 500));
                    numStart = numStart1;
                    rangeEnd = String.format("%03d", toInt(s, 0, 3) / 30 * 30 + 30);
                    if (toInt(s, 9) < 500) {
                        numEnd = s.substring(3, 9) + "500";
                    } else {
                        numEnd = String.format("%06d", toInt(s, 3, 9) + 1) + "000";
                    }
                } else if (i < numbers.size() - 1) {
                    rangeStart = s.substring(0, 3);
                    numStart = s.substring(3);
                    rangeEnd = String.format("%03d", toInt(s, 0, 3) / 30 * 30 + 30);
                    if (toInt(s, 9) < 500) {
                        numEnd = s.substring(3, 9) + "500";
                    } else {
                        numEnd = String.format("%06d", toInt(s, 3, 9) + 1) + "000";
                    }
                } else {
                    rangeStart = s.substring(0, 3);
                    numStart = s.substring(3);
                    rangeEnd = String.format("%03d", toInt(s, 0, 3) / 30 * 30 + 30 - (atBegin / 500));
                    numEnd = numEnd1;
                }

                map = new HashMap<>();
                map.put("sum", String.valueOf(numbers.get(i)[0]));
                map.put("start", chr + rangeStart + " " + numStart);
                map.put("end", chr + rangeEnd + " " + numEnd);
                map.put("total", "");
                list.add(map);
            }

            map = new HashMap<>();
            map.put("sum", numbers.size() > 1 ? String.valueOf(sum) : "");
            map.put("start", "");
            map.put("end", "");
            map.put("total", String.valueOf(atBegin));
            list.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.stamps_list_item,
                new String[]{"sum", "start", "end", "total"},
                new int[]{R.id.text_sum, R.id.text_start, R.id.text_end, R.id.text_rest});
        setListAdapter(adapter);
    }

    private void setList1(String day) {
    	StampsCounter counter = new StampsCounter();
    	List<String[]> stamps = counter.getDayStamps(day);
        List<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> map;
        map = new HashMap<>();
        map.put("sum", "");
        map.put("start", "");
        map.put("end", "");
        map.put("total", "Акцизная марка " + curVol);
        list.add(map);
        map = new HashMap<>();
        map.put("sum", "");
        map.put("start", "");
        map.put("end", "");
        map.put("total", "_____________________");
        list.add(map);




		map = new HashMap<>();
		//map.put("sum", numbers.size() > 1 ? String.valueOf(sum) : "");
		map.put("start", "");
		map.put("end", "");
		//map.put("total", String.valueOf(atBegin));

		list.add(map);
    }

    private int toInt(String str, int start, int end) {
        return Integer.decode(str.substring(start, end).replaceAll("^0*", ""));
    }

    @SuppressWarnings("SameParameterValue")
    private int toInt(String str, int start) {
        return Integer.decode(str.substring(start).replaceAll("^0*", ""));
    }
}
