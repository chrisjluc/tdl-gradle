package com.ac.tdl.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ac.tdl.R;
import com.ac.tdl.model.TdlDate;

import java.util.List;

import antistatic.spinnerwheel.adapters.AbstractWheelTextAdapter;

/**
 * Created by chrisjluc on 1/1/2014.
 */
public class DateArrayAdapter extends AbstractWheelTextAdapter {

    private List<TdlDate> datesList;

    public DateArrayAdapter(Context context, List<TdlDate> datesList) {
        super(context, R.layout.horizontal_calendar_item, 0);
        this.datesList = datesList;
    }

    @Override
    public int getItemsCount() {
        return datesList.size();
    }

    @Override
    protected CharSequence getItemText(int index) {
        return "";
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {

        View view = super.getItem(index, convertView, parent);
        TextView dayName = (TextView) view.findViewById(R.id.tvDayName);
        TextView dayNumber = (TextView) view.findViewById(R.id.tvDayNumber);

        dayName.setText(datesList.get(index).getDayName().substring(0, 3));
        dayNumber.setText(datesList.get(index).getDayNumber());

        return view;
    }
}
