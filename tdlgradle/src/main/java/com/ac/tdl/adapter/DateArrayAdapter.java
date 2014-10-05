package com.ac.tdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ac.tdl.GenericHelper;
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

        TdlDate date = datesList.get(index);
        dayName.setText(date.getDayName().substring(0, 3));
        dayNumber.setText(date.getDayNumber());

        if(date.isHighlighted() && date.getCurrentDayTimestamp() < GenericHelper.getFlooredCurrentDate()){
            dayName.setTextColor(Color.RED);
            dayNumber.setTextColor(Color.RED);
        }else if (date.isHighlighted()) {
            dayName.setTextColor(Color.BLUE);
            dayNumber.setTextColor(Color.BLUE);
        }else {
            dayName.setTextColor(Color.BLACK);
            dayNumber.setTextColor(Color.BLACK);
        }

        return view;
    }

    public void notifyDataChanged(){
        notifyDataChangedEvent();
    }
}
