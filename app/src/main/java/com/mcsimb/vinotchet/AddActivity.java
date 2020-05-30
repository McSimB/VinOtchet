package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("RedundantCast")
public class AddActivity extends Activity implements View.OnClickListener {

    private EditText editedDay;
    private String receivedDay;
    private String sort;
    private String vol;
    private EditText counter1;
    private EditText counter2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstantState) {
        super.onCreate(savedInstantState);
        setContentView(R.layout.add);
        Intent intent = getIntent();
        final String[] sortList = intent.getStringArrayExtra("sort_list");
        final String[] volList = {"0,5", "0,7"};
        counter1 = (EditText) findViewById(R.id.add_counter1);
        counter2 = (EditText) findViewById(R.id.add_counter2);
        editedDay = (EditText) findViewById(R.id.set_date);
        receivedDay = intent.getStringExtra("day");
        TextView monthYear = (TextView) findViewById(R.id.month_year);
        editedDay.setText(receivedDay);
        //TODO : string concat
        monthYear.setText("." + intent.getStringExtra("month") + "." + intent.getStringExtra("year"));
        ArrayAdapter<String> adapterSort = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, sortList);
        Spinner spinnerSort = (Spinner) findViewById(R.id.set_sort);
        spinnerSort.setAdapter(adapterSort);
        spinnerSort.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                sort = sortList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        ArrayAdapter<String> adapterVol = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, volList);
        Spinner spinnerVol = (Spinner) findViewById(R.id.set_vol);
        spinnerVol.setAdapter(adapterVol);
        spinnerVol.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                vol = volList[position].replace(",", ".");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.add_ok:
                if (Integer.decode(editedDay.getText().toString()) < Integer.decode(receivedDay)) {
                    Toast.makeText(AddActivity.this, R.string.bad_day, Toast.LENGTH_SHORT).show();
                    break;
                }
                if ((counter1.getText().toString().equals("")) ||
                        (counter2.getText().toString().equals(""))) {
                    Toast.makeText(AddActivity.this, R.string.bad_counter, Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.putExtra("day", editedDay.getText().toString());
                intent.putExtra("sort", sort);
                intent.putExtra("vol", vol);
                intent.putExtra("counter1", counter1.getText().toString());
                intent.putExtra("counter2", counter2.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.add_cancel:
                finish();
        }
    }
}
