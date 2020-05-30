package com.mcsimb.vinotchet;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.mcsimb.vinotchet.MainActivity.ARG_DAY;

@SuppressWarnings("RedundantCast")
public class MainFragment extends Fragment {

    private MainListAdapter adapter;
    private String day;

    public MainFragment() {
    }

    public static MainFragment newInstance(String day) {
        MainFragment fragment = new MainFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_DAY, day);
        fragment.setArguments(arguments);
        return fragment;
    }

    public String getDay() {
        return day;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        day = arguments.getString(ARG_DAY);
        View fragmentView = inflater.inflate(R.layout.main_list_view, container, false);
        ListView listView = (ListView) fragmentView.findViewById(R.id.list_view_main);
        this.registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick
                    (AdapterView<?> parent, View view, final int position, long id) {
                final String[] LOG_TITLES = getResources().getStringArray(R.array.logs_titles);
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.log_select_title))
                        .setItems(LOG_TITLES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface di, int item) {
                                Intent intent = new Intent(getActivity(), LogActivity.class);
                                intent.putExtra("log_number", item);
                                intent.putExtra("pos_in_day", position);
                                intent.putExtra(ARG_DAY, day);
                                intent.putExtra("for_day", false);
                                startActivity(intent);
                            }
                        })
                        .create();
                dialog.show();
            }
        });
        MainActivity activity = (MainActivity) getActivity();
        ArrayList<Product> products = activity.getControl().dayProducts(day);
        adapter = new MainListAdapter(getActivity(), products);
        listView.setAdapter(adapter);
        return fragmentView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.main_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ctx_menu_edit_main:
                return true;
            case R.id.ctx_menu_remove_main:
                @SuppressWarnings("ConstantConditions")
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_delete)
                        .setTitle(getString(R.string.remove))
                        .setMessage("Удалить запись " + adapter.getItem(menuInfo.position).wine + "?")
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                MainActivity activity = (MainActivity) getActivity();
                                activity.getControl().removeData(day, menuInfo.position);
                                adapter.remove(adapter.getItem(menuInfo.position));
                                if (adapter.isEmpty()) {
                                    ActionBar bar = getActivity().getActionBar();
                                    if (bar != null) {
                                        bar.removeTab(bar.getSelectedTab());
                                        if (bar.getTabCount() != 0 && bar.getSelectedTab().getPosition() == bar.getTabCount() - 1) {
                                            activity.getMainMenu().findItem(R.id.menu_add_main).setVisible(true);
                                        }
                                    }
                                }
                            }
                        })
                        .create();
                dialog.show();
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
