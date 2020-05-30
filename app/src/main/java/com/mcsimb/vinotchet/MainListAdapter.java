package com.mcsimb.vinotchet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressWarnings("RedundantCast")
class MainListAdapter extends ArrayAdapter<Product> {

    private final Context context;
    private final ArrayList<Product> products;

    MainListAdapter(Context context, ArrayList<Product> products) {
        super(context, R.layout.main_list_item, products);
        this.context = context;
        this.products = products;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressWarnings("ConstantConditions") @SuppressLint("ViewHolder")
        View itemView = inflater.inflate(R.layout.main_list_item, parent, false);
        TextView sortItem = (TextView) itemView.findViewById(R.id.text_main_sort);
        TextView countItem1 = (TextView) itemView.findViewById(R.id.text_main_counter1);
        TextView countItem2 = (TextView) itemView.findViewById(R.id.text_main_counter2);
        TextView stampsItem1 = (TextView) itemView.findViewById(R.id.text_main_stamps1);
        TextView stampsItem2 = (TextView) itemView.findViewById(R.id.text_main_stamps2);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.ic_main);
        String s = products.get(position).wine;
        if (s.endsWith("0.7")) {
            itemView.setBackgroundColor(context.getResources().getColor(R.color.col_main_item07));
            sortItem.setTypeface(null, Typeface.BOLD);
        }
        sortItem.setText(products.get(position).wine.replace(".", ","));
        countItem1.setText(products.get(position).counter1);
        countItem2.setText(products.get(position).counter2);
        stampsItem1.setText(products.get(position).stamps1);
        stampsItem2.setText(products.get(position).stamps2);
        imageView.setImageBitmap(products.get(position).label);
        return itemView;
    }

}
