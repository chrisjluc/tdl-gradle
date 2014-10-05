package com.ac.tdl;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ac.tdl.adapter.DateArrayAdapter;
import com.ac.tdl.managers.TaskManager;
import com.ac.tdl.model.TdlDate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.OnWheelClickedListener;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

import static com.ac.tdl.GenericHelper.floorDateByDay;

/**
 * Created by aaronte on 2014-06-15.
 */
public class CalendarFragment extends Fragment implements OnWheelClickedListener, OnWheelChangedListener {

    // Calendar
    private static final String[] MONTH_NAME = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    private static final int DAY_COUNT = 364;
    private static final int YEAR_COUNT = 4;
    private AbstractWheel dateWheel, monthWheel, yearWheel;
    private DateArrayAdapter dateAdapter;
    private List<TdlDate> dates;
    private List<String> years;
    private TaskManager taskManager = TaskManager.getInstance();

    private DistinctDaysToHighlightChangeListener listener = new DistinctDaysToHighlightChangeListener() {
        @Override
        public void notifyChange(List<Long> timestamps) {
            setDatestoHighlight(timestamps);
        }
    };

    public interface DistinctDaysToHighlightChangeListener{
        public void notifyChange(List<Long> timestamps);
    }
    public CalendarFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        taskManager.setDistinctDaysToHighlightChangeListener(listener);
        setDates();
        setDatestoHighlight(taskManager.getDistinctTimestampsToHighlight());

        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        // Set Up Spinners
        dateWheel = (AbstractWheel) rootView.findViewById(R.id.whvCalendar);
        dateAdapter = new DateArrayAdapter(getActivity().getApplicationContext(), dates);
        dateWheel.setViewAdapter(dateAdapter);
        //Set to current date
        dateWheel.setCurrentItem(DAY_COUNT / 2);
        dateWheel.addClickingListener(this);
        dateWheel.addChangingListener(this);

        monthWheel = (AbstractWheel) rootView.findViewById(R.id.whvMonth);
        ArrayWheelAdapter<String> monthAdapter =
                new ArrayWheelAdapter<String>(getActivity().getApplicationContext(), MONTH_NAME);
        monthAdapter.setItemResource(R.layout.month_item);
        monthAdapter.setItemTextResource(R.id.tvSimpleItem);
        monthWheel.setViewAdapter(monthAdapter);
        monthWheel.setCurrentItem(dates.get(DAY_COUNT / 2).getMonth());
        monthWheel.setCyclic(true);
        monthWheel.setEnabled(false);

        yearWheel = (AbstractWheel) rootView.findViewById(R.id.whvYear);
        int currentYear = Calendar.getInstance(Locale.CANADA).get(Calendar.YEAR);
        String[] years = new String[YEAR_COUNT];
        for (int i = 0; i < YEAR_COUNT; i++) {
            years[i] = Integer.toString(currentYear - YEAR_COUNT / 2 + i);
        }
        this.years = Arrays.asList(years);
        ArrayWheelAdapter<String> yearAdapter =
                new ArrayWheelAdapter<String>(getActivity().getApplicationContext(), years);
        yearAdapter.setItemResource(R.layout.year_item);
        yearAdapter.setItemTextResource(R.id.tvSimpleItem);
        yearWheel.setViewAdapter(yearAdapter);
        yearWheel.setCurrentItem(YEAR_COUNT / 2);
        yearWheel.setEnabled(false);

        return rootView;
    }

    private void setDatestoHighlight(List<Long> timestamps) {
        for(TdlDate date: dates){
            if(timestamps.contains(date.getCurrentDayTimestamp()))
                date.setHighlighted(true);
            else
                date.setHighlighted(false);
        }
        if(dateAdapter != null)
            dateAdapter.notifyDataChanged();
    }


    private void setDates() {
        List<TdlDate> dates = new ArrayList<TdlDate>();
        Calendar calendar = Calendar.getInstance(Locale.CANADA);
        calendar.add(Calendar.DATE, -DAY_COUNT / 2);
        for (int i = 0; i < DAY_COUNT; i++) {
            TdlDate date = new TdlDate();
            DateFormat format = new SimpleDateFormat("EEEE");
            date.setDayName(format.format(calendar.getTime()).toUpperCase());
            format = new SimpleDateFormat("dd");
            date.setDayNumber(format.format(calendar.getTime()));
            date.setMonth(calendar.get(Calendar.MONTH));
            date.setYear(calendar.get(Calendar.YEAR));
            date.setCurrentDate(floorDateByDay(calendar));
            dates.add(date);
            calendar.add(Calendar.DATE, 1);
        }
        this.dates = dates;
    }

    @Override
    public void onItemClicked(AbstractWheel wheel, int itemIndex) {
        wheel.setCurrentItem(itemIndex, true);
    }

    @Override
    public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
        switch (wheel.getId()) {
            case R.id.whvCalendar:
                TdlDate date = dates.get(newValue);
                monthWheel.setCurrentItem(date.getMonth(), true);
                int index = years.indexOf(Integer.toString(date.getYear()));
                yearWheel.setCurrentItem(index, true);
                break;
            case R.id.whvMonth:
                break;
            case R.id.whvYear:
                break;
        }
    }
}
