package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings({"RedundantCast", "FieldCanBeLocal"})
public class LogActivity extends Activity implements View.OnClickListener {

    private final int INFO_ABOUT_WORKSHOP = 0;
    private final int REPORT_MOVEMENT_PRODUCTS = 1;
    private final int ALCOHOL_ACCOUNTING = 2;
    private final int JOB_BOTTLING_ACCOUNTING = 3;
    private final int MONTH_SUMMARY = 4;
    private final int REWORK_ACTS = 5;
    private final int REQUIREMENTS = 6;
    private final String ACTS_ORDER = "acts_order";
    private final String REQ_ORDER = "req_order";
    private final String REGEX = ", ";

    private FileUtils db;
    private boolean firstInitSpinner = true;
    private int logNumber;
    private int curInd;
    private ArrayList<String> days;
    private String[] sortsList;
    private String[] titleList;
    private String[] actsOrder;
    private String[] reqOrder;
    private LogControl control;
    private Button btnPrev;
    private Button btnNext;
    private Spinner sortSelectSpinner;
    private LinearLayout linLay1;
    private LinearLayout linLay2;
    private TableLayout tableLayout;
    private TextView sort;
    private TextView date;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private TextView blendNum;
    private Resources res;
    private SharedPreferences prefs;

    @Override
    public void onClick(View v) {
        int next = 1;
        int prev = -1;
        switch (v.getId()) {
            case R.id.btn_log_prev:
                control.move(prev);
                showView();
                break;
            case R.id.btn_log_next:
                control.move(next);
                showView();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log);
        db = FileUtils.getInstance();
        tableLayout = (TableLayout) findViewById(R.id.table_layout_log);
        btnPrev = (Button) findViewById(R.id.btn_log_prev);
        btnNext = (Button) findViewById(R.id.btn_log_next);
        sortSelectSpinner = (Spinner) findViewById(R.id.spin_log_sort_select);
        linLay1 = (LinearLayout) findViewById(R.id.linear_layout_log_1);
        linLay2 = (LinearLayout) findViewById(R.id.linear_layout_log_2);
        sort = (TextView) findViewById(R.id.text_log_sort);
        date = (TextView) findViewById(R.id.text_log_date);
        tv1 = (TextView) findViewById(R.id.text_log_1);
        tv2 = (TextView) findViewById(R.id.text_log_2);
        tv3 = (TextView) findViewById(R.id.text_log_3);
        tv4 = (TextView) findViewById(R.id.text_log_4);
        blendNum = (TextView) findViewById(R.id.text_log_blend);
        res = getResources();
        Intent intent = getIntent();
        logNumber = intent.getIntExtra("log_number", -1);
        int posInDay = intent.getIntExtra("pos_in_day", -1);
        String startDay = intent.getStringExtra("day");
        days = intent.getStringArrayListExtra("days");
        boolean forDay = intent.getBooleanExtra("for_day", false);
        for (int i = 0; i < db.dataBase.size(); i++) {
            if (db.dataBase.get(i)[0].equals(startDay)) {
                curInd = i + posInDay;
                break;
            }
        }

        StringBuilder def = new StringBuilder();
        for (int i = 0; i < db.getSorts(true).length; i++) {
            def.append(String.valueOf(i)).append(i == db.getSorts(true).length - 1 ? "" : REGEX);
        }
        prefs = getPreferences(MODE_PRIVATE);
        actsOrder = prefs.getString(ACTS_ORDER, def.toString()).split(REGEX);
        reqOrder = prefs.getString(REQ_ORDER, def.toString()).split(REGEX);

        switch (logNumber) {
            case INFO_ABOUT_WORKSHOP:
                info();
                break;
            case REPORT_MOVEMENT_PRODUCTS:
                report();
                break;
            case ALCOHOL_ACCOUNTING:
                alcohol();
                break;
            case JOB_BOTTLING_ACCOUNTING:
                job(forDay);
                break;
            case MONTH_SUMMARY:
                monthSummary();
                break;
            case REWORK_ACTS:
                reworkActs();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_menu, menu);
        if (logNumber == REWORK_ACTS) {
            menu.findItem(R.id.menu_require_log).setVisible(true);
            menu.findItem(R.id.menu_settings_log).setVisible(true);
        } else if (logNumber == MONTH_SUMMARY) {
            menu.findItem(R.id.menu_export_log).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_require_log:
                if (logNumber == REWORK_ACTS) {
                    item.setTitle(R.string.acts);
                    logNumber = REQUIREMENTS;
                    requirements();
                } else if (logNumber == REQUIREMENTS) {
                    item.setTitle(R.string.require);
                    logNumber = REWORK_ACTS;
                    reworkActs();
                }
                break;
            case R.id.menu_export_log:
                if (logNumber == MONTH_SUMMARY) {
                    control.export();
                    Toast.makeText(this, getString(R.string.export_complete), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_settings_log:
                settings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void info() {
        this.setTitle(res.getStringArray(R.array.logs_titles)[0]);
        control = new InfoLogControl(curInd);
        titleList = res.getStringArray(R.array.info_title_list);
        initSpinner();
    }

    private void report() {
        this.setTitle(res.getStringArray(R.array.logs_titles)[1]);
        control = new ReportLogControl(curInd);
        titleList = res.getStringArray(R.array.report_title_list);
        initSpinner();
    }

    private void alcohol() {
        this.setTitle(res.getStringArray(R.array.logs_titles)[2]);
        control = new AlcoholLogControl(curInd);
        titleList = res.getStringArray(R.array.alcohol_title_list);
        sortSelectSpinner.setVisibility(View.INVISIBLE);
        blendNum.setVisibility(View.INVISIBLE);
        date.setVisibility(View.VISIBLE);
        sort.setVisibility(View.VISIBLE);
        showView();
    }

    private void job(boolean forDay) {
        this.setTitle(res.getStringArray(R.array.logs_titles)[3]);
        control = new JobLogControl(curInd, forDay);
        titleList = res.getStringArray(R.array.job_title_list);
        sortSelectSpinner.setVisibility(View.INVISIBLE);
        blendNum.setVisibility(View.INVISIBLE);
        date.setVisibility(View.VISIBLE);
        sort.setVisibility(View.VISIBLE);
        showView();
    }

    private void monthSummary() {
        this.setTitle(R.string.month_summary);
        //control = new MonthSummaryControl((String) db.winesList.keySet().toArray()[0]);
        control = new MonthSummaryControl(db.getSorts(false)[0]);
        String[] iList = res.getStringArray(R.array.info_title_list);
        String[] rList = res.getStringArray(R.array.report_title_list);
        titleList = new String[8];
        titleList[0] = iList[1];
        titleList[1] = iList[3];
        titleList[2] = iList[5];
        titleList[3] = iList[8];
        titleList[5] = rList[2];
        titleList[6] = rList[4];
        titleList[7] = rList[7];
        linLay1.setVisibility(View.VISIBLE);
        linLay2.setVisibility(View.VISIBLE);
        initSpinner();
    }

    private void reworkActs() {
        this.setTitle(R.string.rework_acts_title);
        control = new ReworkActsControl(days.toArray(new String[days.size()]));
        sortSelectSpinner.setVisibility(View.INVISIBLE);
        blendNum.setVisibility(View.INVISIBLE);
        sort.setVisibility(View.VISIBLE);
        titleList = new String[8];
        reorderTitle(titleList, actsOrder);
        showView();
    }

    private void requirements() {
        this.setTitle(R.string.require_title);
        control = new RequirementsControl(days.toArray(new String[days.size()]));
        titleList = new String[8];
        reorderTitle(titleList, reqOrder);
        titleList[db.getSorts(true).length] = res.getString(R.string.sum);
        showView();
    }

    private void reorderTitle(String[] data, String[] order) {
        for (int i = 0; i < db.getSorts(true).length; i++) {
            data[i] = db.getSorts(true)[Integer.decode(order[i])];
        }
    }

    private String[] reorderData(String[] data, String[] order) {
        String[] result = new String[8];
        for (int i = 0; i < db.getSorts(true).length; i++) {
            result[i] = data[Integer.decode(order[i])];
        }
        result[db.getSorts(true).length] = data[db.getSorts(true).length];
        return result;
    }

    private void settings() {
        LayoutInflater factory = LayoutInflater.from(this);
        @SuppressLint("InflateParams")
        View dialogView = factory.inflate(R.layout.dialog_settings, null);
        final EditText value = (EditText) dialogView.findViewById(R.id.edit_dialog_settings);
        StringBuilder str = new StringBuilder();
        if (logNumber == REWORK_ACTS) {
            for (int i = 0; i < actsOrder.length; i++) {
                str.append(String.valueOf(actsOrder[i])).append(i == actsOrder.length - 1 ? "" : REGEX);
            }
        } else if (logNumber == REQUIREMENTS) {
            for (int i = 0; i < reqOrder.length; i++) {
                str.append(String.valueOf(reqOrder[i])).append(i == reqOrder.length - 1 ? "" : REGEX);
            }
        }

        value.setText(str.toString());
        ListView list = (ListView) dialogView.findViewById(R.id.list_dialog_settings);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                db.getSorts(true));
        list.setAdapter(adapter);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                //.setTitle(getString(R.string.add_blend_title))
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface di, int whichButton) {
                        di.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface di, int whichButton) {
                        if (logNumber == REWORK_ACTS) {
                            actsOrder = value.getText().toString().split(REGEX);
                            prefs.edit()
                                    .putString(ACTS_ORDER, value.getText().toString())
                                    .apply();
                            reworkActs();
                        } else if (logNumber == REQUIREMENTS) {
                            reqOrder = value.getText().toString().split(REGEX);
                            prefs.edit()
                                    .putString(REQ_ORDER, value.getText().toString())
                                    .apply();
                            requirements();
                        }
                    }
                })
                .create();
        dialog.show();
    }

    private void initSpinner() {
        sortsList = db.getSorts(false);
        ArrayAdapter<String> adapterSort = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, sortsList);
        sortSelectSpinner.setAdapter(adapterSort);
        if (logNumber != MONTH_SUMMARY) {
            sortSelectSpinner.setSelection(Arrays.binarySearch(sortsList, db.dataBase.get(control.curIndex)[1]));
        }
        sortSelectSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstInitSpinner) {
                    firstInitSpinner = false;
                } else {
                    control.initSnap(sortsList[position]);
                    if (logNumber < MONTH_SUMMARY && !control.indexList.isEmpty()) {
                        control.setSnapIndex(control.indexList.size() - 1);
                    }
                    showView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        showView();
    }

    @SuppressLint("SetTextI18n")
    private void showView() {
        String[] dataList = control.getDataList();
        btnPrev.setVisibility(control.btnPrevVisibility() ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(control.btnNextVisibility() ? View.VISIBLE : View.INVISIBLE);
        if (logNumber == REWORK_ACTS) {
            dataList = reorderData(dataList, actsOrder);
            sort.setText(control.days[control.curDay] + "." + db.MONTH);
        } else if (logNumber == REQUIREMENTS) {
            dataList = reorderData(dataList, reqOrder);
            sort.setText(control.days[control.curDay] + "." + db.MONTH + "     № " + (control.curDay + 1));
        } else if (logNumber == MONTH_SUMMARY) {
            blendNum.setText(String.valueOf(control.curBlend + 1) + " куп.");
            tv1.setText("0,5 - " + String.valueOf(control.count05));
            tv2.setText("0,7 - " + String.valueOf(control.count07));
            tv3.setText("0,5 - " + control.noZero2(control.count05 * 0.05));
            tv4.setText("0,7 - " + control.noZero2(control.count07 * 0.07));
        } else if (logNumber == ALCOHOL_ACCOUNTING) {
            if (control.total) {
                date.setText("Всего");
            } else {
                date.setText(db.dataBase.get(control.curIndex)[0] + "." + db.MONTH);
            }
            sort.setText(db.dataBase.get(control.curIndex)[1] + " " + db.dataBase.get(control.curIndex)[2].replace(".", ","));
        } else if (logNumber == JOB_BOTTLING_ACCOUNTING) {
            date.setText(db.dataBase.get(control.curIndex)[0] + "." + db.MONTH);
            sort.setText(db.dataBase.get(control.curIndex)[1]);
        } else {
            if (!control.divBlendFlag) {
                blendNum.setText(String.valueOf(control.blend + 1) + " куп.");
            } else
                blendNum.setText(String.valueOf(control.blend) + "/" +
                        String.valueOf(control.blend + 1) + " куп.");
        }
        tableLayout.removeAllViews();
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                1.0f);
        for (int i = 0; i < dataList.length; i++) {
            TableRow tableRow = new TableRow(this);
            TextView textViewTitle = new TextView(this);
            TextView textViewData = new TextView(this);
            tableRow.setLayoutParams(params);
            textViewTitle.setTextSize(16);
            textViewTitle.setTextColor(0xFF666666);
            textViewTitle.setPadding(10, 0, 0, 0);
            textViewTitle.setText(titleList[i]);
            textViewData.setTextSize(30);
            if (i == 11) {
                textViewData.setTextSize(20);
            }
            textViewData.setText(dataList[i]);
            tableRow.addView(textViewTitle);
            tableRow.addView(textViewData);
            if (logNumber < MONTH_SUMMARY) {
                if (i % 2 == 0) {
                    tableRow.setBackgroundResource(R.drawable.rect_frame);
                } else {
                    tableRow.setBackgroundResource(R.drawable.rect_solid);
                }
            } else if (logNumber == MONTH_SUMMARY) {
                if (i != 4) {
                    tableRow.setBackgroundResource(R.drawable.rect_solid);
                }
            } else {
                tableRow.setBackgroundResource(R.drawable.rect_frame);
                if (logNumber == REQUIREMENTS && !(titleList[i] == null) &&
                        titleList[i].equals(res.getString(R.string.sum))) {
                    tableRow.setBackgroundResource(R.drawable.rect_solid);
                }
            }
            tableLayout.addView(tableRow);
        }
    }
}
