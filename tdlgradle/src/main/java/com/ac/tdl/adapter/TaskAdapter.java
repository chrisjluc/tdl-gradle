package com.ac.tdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ac.tdl.R;
import com.ac.tdl.model.Task;
import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;

import java.util.List;

public class TaskAdapter extends ExpandableListItemAdapter<Task> {
    private Context context;
	public TaskAdapter(Context context,List<Task> taskList) {
		super(context, R.layout.task_expandable_list_item, R.id.task_title, R.id.task_content,taskList);
        this.context = context;

	}

    @Override
    public View getTitleView(int i, View view, ViewGroup viewGroup) {
        TextView textView = (TextView) view;
        if (textView == null) {
            textView = new TextView(context);
        }
        Task task = getItem(i);
        textView.setText(task.getTaskTitle());
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    @Override
    public View getContentView(int i, View view, ViewGroup viewGroup) {
        TextView textView = (TextView) view;
        if (textView == null) {
            textView = new TextView(context);
        }
        Task task = getItem(i);
        textView.setText(task.getTaskDetails());
        textView.setTextColor(Color.BLACK);
        return textView;
    }

}
