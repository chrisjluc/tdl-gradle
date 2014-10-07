package com.ac.tdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ac.tdl.R;
import com.ac.tdl.model.Task;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;

	public TaskAdapter(Context context,List<Task> taskList) {
		super(context, R.id.tvTaskTitle,taskList);
        this.context = context;
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            LayoutInflater vi = LayoutInflater.from(context);
            view = vi.inflate(R.layout.task_title_item, null);
        }
        TextView tvTaskTitle = (TextView) view.findViewById(R.id.tvTaskTitle);
        final Task task = getItem(position);
        tvTaskTitle.setText(task.getTaskTitle());
        if(task.isPriority())
            tvTaskTitle.setTextColor(Color.RED);
        else
            tvTaskTitle.setTextColor(Color.BLACK);
        return view;
    }
}
