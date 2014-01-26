package com.ac.tdl.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ac.tdl.EditActivity;
import com.ac.tdl.R;
import com.ac.tdl.model.Hashtag;
import com.ac.tdl.model.Task;
import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;

import java.util.List;

public class TaskAdapter extends ExpandableListItemAdapter<Task> {
    private Context context;
	public TaskAdapter(Context context,List<Task> taskList) {
		super(context, R.layout.task_expandable_list_item, R.id.task_title, R.id.task_content,taskList);
        this.context = context;
        //Only one item is expanded at a time
        setLimit(1);

	}

    @Override
    public View getTitleView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if(view == null){
              LayoutInflater vi = LayoutInflater.from(context);
            view = vi.inflate(R.layout.task_title_item, null);
        }
        TextView tvTaskTitle = (TextView) view.findViewById(R.id.tvTaskTitle);
        Task task = getItem(i);
        tvTaskTitle.setText(task.getTaskTitle());
        return view;
    }

    @Override
    public View getContentView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if(view == null){
            LayoutInflater vi = LayoutInflater.from(context);
            view = vi.inflate(R.layout.task_content_item, null);
        }
        final Task task = getItem(i);
        TextView tvHashtag = (TextView) view.findViewById(R.id.tvHashtag);
        String hashtagString = createHashtagString(task.getHashtagArray());
        tvHashtag.setText(hashtagString);
        TextView tvDetails = (TextView) view.findViewById(R.id.tvDetails);
        tvDetails.setText(task.getTaskDetails());

        ImageButton bEdit = (ImageButton) view.findViewById(R.id.bEdit);
        bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("taskId",task.getTaskId());
                context.startActivity(intent);
            }
        });
        return view;
    }

    private String createHashtagString(Hashtag[] array){
        StringBuilder hashtagBuilder = new StringBuilder();

        if (array == null) {
            return "";
        }

        for (Hashtag hashtag : array){
            hashtagBuilder.append(" #" + hashtag.getLabel());
        }
        return hashtagBuilder.toString();
    }

}
