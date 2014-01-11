package com.ac.tdl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        Task task = getItem(i);
        TextView tvHashtag = (TextView) view.findViewById(R.id.tvHashtag);
        String hashtagString = createHashtagString(task.getHashtagArray());
        tvHashtag.setText(hashtagString);
        TextView tvDetails = (TextView) view.findViewById(R.id.tvDetails);
        tvDetails.setText(task.getTaskDetails());
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
