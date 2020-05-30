package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    public final static String USED_SORTS = "used_sorts";
    public final static String ARG_DAY = "day";
    public final static String ARG_MONTH = "month";
    public final static String ARG_YEAR = "year";

    public static String[] MONTHS;
    public static String YEAR;

    private final String REGEX = ", ";

    private MainControl control;
    private FileUtils db;
    private Menu mainMenu;
    private TextView dal;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = getPreferences(MODE_PRIVATE);
        dal = findViewById(R.id.text_main_dal);
        MONTHS = getResources().getStringArray(R.array.months);
        control = new MainControl();
        db = FileUtils.getInstance();

        if (db.verifyStoragePermissions(this)) {
            start();
        }
    }

    private void start() {
        if (db.mediaMounted()) {
            if (db.pathExists()) {
                YEAR = String.format("%ty", Calendar.getInstance());
                String[] exMonths = db.existingMonths();
                if (exMonths.length != 0) {
                    String month = exMonths[exMonths.length - 1].length() == 1 ?
                            "0" + exMonths[exMonths.length - 1] : exMonths[exMonths.length - 1];
                    tabsCreate(month);
                } else
                    Toast.makeText(this, getString(R.string.no_data), Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(this, getString(R.string.no_dir), Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, getString(R.string.no_sd), Toast.LENGTH_LONG).show();

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == FileUtils.REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                start();
            }
        }
    }

    @Override
    public void onClick(View v) {
        LayoutInflater factory = LayoutInflater.from(this);
        switch (v.getId()) {
            case R.id.btn_main_stat:
                @SuppressLint("InflateParams")
                View dialogStatView = factory.inflate(R.layout.dialog_statistic, null);
                TextView made = dialogStatView.findViewById(R.id.text_dialog_made);
                TextView rest = dialogStatView.findViewById(R.id.text_dialog_rest);
                String[] stat = control.statistic();
                made.setText(stat[0]);
                rest.setText(stat[1]);
                AlertDialog dialogStat = new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(R.string.statistic)
                        .setView(dialogStatView)
                        .create();
                dialogStat.show();
                break;
            case R.id.btn_main_mat:
                @SuppressLint("InflateParams")
                View dialogMatView = factory.inflate(R.layout.dialog_materials, null);

                final NumberPicker numPick1 =
                        dialogMatView.findViewById(R.id.nmb_pick_dialog_mat_1);
                final NumberPicker numPick2 =
                        dialogMatView.findViewById(R.id.nmb_pick_dialog_mat_2);

                if (!control.getDays().isEmpty()) {
                    int min = Integer.decode(control.getDays().get(0));
                    int max = Integer.decode(control.getDays().get(control.getDays().size() - 1));
                    numPick1.setMinValue(min);
                    numPick1.setMaxValue(max);
                    numPick2.setMinValue(min);
                    numPick2.setMaxValue(max);

                    ActionBar bar = getActionBar();
                    if (bar != null) {
                        numPick1.setValue(Integer.decode((String) bar.getSelectedTab().getTag()));
                        numPick2.setValue(Integer.decode((String) bar.getSelectedTab().getTag()));
                    }

                    AlertDialog dialogMat = new AlertDialog.Builder(this)
                            .setTitle(R.string.select_period)
                            .setView(dialogMatView)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface p1, int p2) {
                                }
                            })
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface p1, int p2) {
                                    int start = numPick1.getValue();
                                    int end = numPick2.getValue();
                                    if (start <= end) {
                                        String[] data = control.material(start, end);
                                        Intent intent =
                                                new Intent(MainActivity.this, MaterialActivity.class);
                                        intent.putExtra("data", data);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(MainActivity.this,
                                                R.string.bad_data, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .create();
                    dialogMat.show();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mainMenu = menu;
        if (control != null && control.getDays().isEmpty()) {
            mainMenu.findItem(R.id.menu_add_main).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (db.dataBase != null) {
            boolean visibility = !db.dataBase.isEmpty();
            menu.findItem(R.id.menu_month_summary_main).setVisible(visibility);
            menu.findItem(R.id.menu_new_month_main).setVisible(visibility);
            menu.findItem(R.id.menu_rework_acts_main).setVisible(visibility);
            menu.findItem(R.id.menu_doc_stamps_main).setVisible(visibility);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_main:
                Intent intent = new Intent(this, AddActivity.class);
                if (!db.dataBase.isEmpty()) {
                    intent.putExtra(ARG_DAY, db.dataBase.get(db.dataBase.size() - 1)[0]);
                } else
                    intent.putExtra(ARG_DAY, "1");
                intent.putExtra(ARG_MONTH, db.MONTH);
                intent.putExtra(ARG_YEAR, YEAR);
                intent.putExtra("sort_list", db.getSorts(false));
                startActivityForResult(intent, 1);
                return true;
            case R.id.menu_other_month_main:
                selectMonth();
                return true;
            case R.id.menu_new_month_main:
                String nMonth = control.newMonth();
                if (nMonth.equals("00")) {
                    Toast.makeText(this, getString(R.string.month_exist), Toast.LENGTH_LONG).show();
                } else {
                    tabsCreate(nMonth);
                    String m = getResources().getStringArray(R.array.months)[Integer.decode(
                            nMonth.startsWith("0") ? nMonth.substring(1) : nMonth) - 1];
                    Toast.makeText(this, m + " " + getString(R.string.create), Toast.LENGTH_LONG)
                            .show();
                }
                return true;
            case R.id.menu_month_summary_main:
                Intent intent2 = new Intent(MainActivity.this, LogActivity.class);
                intent2.putExtra("log_number", 4);
                startActivity(intent2);
                return true;
            case R.id.menu_rework_acts_main:
                Intent intent3 = new Intent(MainActivity.this, LogActivity.class);
                intent3.putExtra("log_number", 5);
                intent3.putExtra("days", control.getDays());
                startActivity(intent3);
                return true;
            case R.id.menu_doc_stamps_main:
                Intent intent4 = new Intent(MainActivity.this, StampsActivity.class);
                intent4.putExtra("days", control.getDays());
                startActivity(intent4);
                return true;
            case R.id.menu_settings_main:
                settings();
                break;
            case R.id.menu_exit_main:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String day = data.getStringExtra(ARG_DAY);
            final String sort = data.getStringExtra("sort");
            final String vol = data.getStringExtra("vol");
            String counter1 = data.getStringExtra("counter1");
            String counter2 = data.getStringExtra("counter2");
            ActionBar bar = getActionBar();
            if (bar != null) {
                if (control.addData(day, sort, vol, counter1, counter2)) {
                    LayoutInflater factory = LayoutInflater.from(this);
                    @SuppressLint("InflateParams")
                    View dialogView = factory.inflate(R.layout.dialog_input, null);
                    final EditText value = dialogView.findViewById(R.id.edit_dialog_blend);
                    TextView blendName = dialogView.findViewById(R.id.text_dialog_blend);
                    blendName.setText(sort + ", дал");
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.add_blend_title))
                            .setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    control.addBlend(sort, value.getText().toString());
                                }
                            })
                            .create();
                    dialog.show();
                }
                if (vol != null && control.checkStamps(vol)) {
                    LayoutInflater factory = LayoutInflater.from(this);
                    @SuppressLint("InflateParams")
                    View dialogView = factory.inflate(R.layout.dialog_stamps, null);
                    TextView letter = dialogView.findViewById(R.id.text_stamps_letter);
                    final EditText range = dialogView.findViewById(R.id.edit_range);
                    final EditText number = dialogView.findViewById(R.id.edit_number);
                    letter.setText(vol.equals("0.5") ? "Ю" : "Я");
                    String stamp = vol.equals("0.5") ?
                            db.stamps05.get(db.stamps05.size() - 1) :
                            db.stamps07.get(db.stamps07.size() - 1);
                    int rng = Integer.decode(
                            stamp.substring(0, 3).replaceAll("^0*", "")) / 30 * 30 + 30;
                    range.setText(rng == 120 ? "001" : String.format("%03d", rng + 1));
                    int nmb = Integer.decode(stamp.substring(9).replaceAll("^0*", ""));
                    number.setText(nmb > 500 ?
                            stamp.substring(3, 9) + "501" : stamp.substring(3, 9) + "001");
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.add_stamps_title))
                            .setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    control.addStampsRange(range.getText().toString(),
                                            number.getText().toString(), vol);
                                }
                            })
                            .create();
                    dialog.show();
                }
                if (db.dataBase.size() == 1) {
                    addTabOfDay(day, bar);
                } else {
                    ListView list = findViewById(R.id.list_view_main);
                    MainListAdapter adapter = (MainListAdapter) list.getAdapter();
                    if (day != null) {
                        if (day.equals(bar.getSelectedTab().getTag())) {
                            StampsCounter counter = new StampsCounter();
                            List<String[]> lst = counter.getDayStamps(day);
                            adapter.add(new Product(sort + "  " + vol, counter1, counter2,
                                    lst.get(lst.size() - 1)[0], lst.get(lst.size() - 1)[1],
                                    db.sortsIcons.get(sort)));
                        } else {
                            addTabOfDay(day, bar);
                        }
                    }
                }
                showDal(day);
            }
        }
    }

    private void tabsCreate(String month) {
        ActionBar bar = getActionBar();
        if (bar != null) {
            control.initControl(month);

            StringBuilder def = new StringBuilder();
            for (int i = 0; i < db.getSorts(true).length; i++) {
                def.append("1").append(i == db.getSorts(true).length - 1 ? "" : REGEX);
            }
            String used = prefs.getString(USED_SORTS, def.toString());
            db.setUsedSorts(used.split(REGEX));

            this.setTitle(R.string.main_title);
            bar.removeAllTabs();
            for (String day : control.getDays()) {
                addTabOfDay(day, bar);
                if (control.getDays().indexOf(day) == control.getDays().size() - 1) {
                    showDal(day);
                }
            }
        }
    }

    private void addTabOfDay(String day, ActionBar bar) {
        ActionBar.Tab tab = bar.newTab();
        tab.setTag(day);
        bar.addTab(tab.setText(day + "." + db.MONTH + "." + YEAR)
                .setTabListener(new TabListener(MainFragment.newInstance(day))), true);

    }

    private void selectMonth() {
        String[] exMonths = db.existingMonths();
        final String[] dMonths = new String[exMonths.length];
        for (int i = 0; i < exMonths.length; i++) {
            dMonths[i] = MONTHS[Integer.decode(exMonths[i]) - 1];
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.month_select_title))
                .setItems(dMonths, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface di, int item) {
                        String month = Integer.toString(
                                Arrays.asList(MONTHS).indexOf(dMonths[item]) + 1);
                        month = month.length() == 1 ? "0" + month : month;
                        if (!month.equals(db.MONTH)) {
                            tabsCreate(month);
                        }
                    }
                })
                .create();
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showDal(String day) {
        float dal05 = 0;
        float dal07 = 0;
        for (String[] data : db.dataBase) {
            if (data[0].equals(day)) {
                if (data[2].equals("0.5")) {
                    dal05 += Integer.decode(data[4]) * 0.05f;
                } else {
                    dal07 += Integer.decode(data[4]) * 0.07f;
                }
            }
        }
        dal.setText(Math.round((dal05 + dal07) * 100) / 100f + " дал");
    }

    private void settings() {
        LayoutInflater factory = LayoutInflater.from(this);
        View dialogView = factory.inflate(R.layout.dialog_sorts, null);
        final ListView list = dialogView.findViewById(R.id.list_dialog_sorts);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                db.getSorts(true));
        list.setAdapter(adapter);

        StringBuilder def = new StringBuilder();
        for (int i = 0; i < adapter.getCount(); i++) {
            def.append("1").append(i == adapter.getCount() - 1 ? "" : REGEX);
        }
        String[] used = prefs.getString(USED_SORTS, def.toString()).split(REGEX);
        for (int i = 0; i < adapter.getCount(); i++) {
            list.setItemChecked(i, used[i].equals("1"));
        }
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.used_sorts))
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
                        StringBuilder result = new StringBuilder();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            result.append(list.isItemChecked(i) ? "1" : "0")
                                    .append(i == adapter.getCount() - 1 ? "" : REGEX);
                        }
                        prefs.edit()
                                .putString(USED_SORTS, result.toString())
                                .apply();

                        db.setUsedSorts(result.toString().split(REGEX));
                    }
                })
                .create();
        dialog.show();
    }

    public Menu getMainMenu() {
        return mainMenu;
    }

    public MainControl getControl() {
        return control;
    }

    public class TabListener implements ActionBar.TabListener {

        private final MainFragment mFragment;

        TabListener(MainFragment fragment) {
            mFragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.add(R.id.frame_main, mFragment, mFragment.getDay());
            ActionBar bar = getActionBar();
            if ((bar != null) && (mainMenu != null)) {
                if ((db.dataBase.isEmpty()) ||
                        (tab.getTag().equals(control.getDays().get(control.getDays().size() - 1)))) {
                    mainMenu.findItem(R.id.menu_add_main).setVisible(true);
                } else
                    mainMenu.findItem(R.id.menu_add_main).setVisible(false);
                if (!db.dataBase.isEmpty()) {
                    showDal((String) tab.getTag());
                }
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(mFragment);
        }

        @Override
        public void onTabReselected(final ActionBar.Tab tab, FragmentTransaction ft) {
            PopupMenu popup = new PopupMenu(MainActivity.this, findViewById(R.id.frame_main));
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.popup_menu_main, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.popup_menu_main_info:
                            Intent intent = new Intent(MainActivity.this, LogActivity.class);
                            intent.putExtra("log_number", 3);
                            intent.putExtra("pos_in_day", 0);
                            intent.putExtra(ARG_DAY, (String) tab.getTag());
                            intent.putExtra("for_day", true);
                            startActivity(intent);
                            break;
                        case R.id.popup_menu_main_materials:
                            int day = Integer.decode((String) tab.getTag());
                            String[] data = control.material(day, day);
                            Intent intent2 =
                                    new Intent(MainActivity.this, MaterialActivity.class);
                            intent2.putExtra("data", data);
                            startActivity(intent2);
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }
    }
}
