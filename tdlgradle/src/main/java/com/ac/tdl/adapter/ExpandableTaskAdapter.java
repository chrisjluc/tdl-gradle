package com.ac.tdl.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ac.tdl.EditActivity;
import com.ac.tdl.HomeFragment;
import com.ac.tdl.MainActivity;
import com.ac.tdl.R;
import com.ac.tdl.managers.TaskManager;
import com.ac.tdl.model.Task;
import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chrisjluc on 2014-05-25.
 */
public class ExpandableTaskAdapter extends ExpandableListItemAdapter<String> implements AdapterView.OnItemClickListener,
        ContextualUndoAdapter.DeleteItemCallback {

    private MainActivity homeActivity;
    private HashMap<String, ContextualUndoAdapter> adapterByHeader;
    private TaskManager taskManager = TaskManager.getInstance();

    public ExpandableTaskAdapter(Activity activity, List<String> headerList) {
        super(activity, R.layout.expandable_item, R.id.header_layout, R.id.content_layout, headerList);
        this.homeActivity = (MainActivity) activity;
        this.adapterByHeader = new HashMap<String, ContextualUndoAdapter>();

        for (String header : headerList) {
            createContextualUndoAdapter(header);
        }
    }

    private void createContextualUndoAdapter(String header) {
        TaskAdapter taskAdapter = new TaskAdapter(homeActivity, taskManager.getTasksToDisplayByHeader().get(header));
        ContextualUndoAdapter adapter = new ContextualUndoAdapter(taskAdapter,
                R.layout.undo_row, R.id.undo_row_undobutton, 1500, this);
        adapterByHeader.put(header, adapter);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        List<String>  headersToRemove = new ArrayList<String>();
        for (String header: adapterByHeader.keySet()){
            if(!taskManager.getTasksToDisplayByHeader().containsKey(header)){
                headersToRemove.add(header);
                continue;
            }
        }

        for(String header: headersToRemove)
            adapterByHeader.remove(header);

        for (String header: taskManager.getOrderedHeaderList()) {
            if (!adapterByHeader.containsKey(header)) {
                createContextualUndoAdapter(header);
            }
        }
        expandAll();
    }

    @Override
    public View getTitleView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(homeActivity);
            view = vi.inflate(R.layout.header_item, null);
        }
        TextView tvHeader = (TextView) view.findViewById(R.id.tvHeader);
        String header = getItem(i);
        tvHeader.setText(header);
        return view;
    }

    @Override
    public View getContentView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(homeActivity);
            view = vi.inflate(R.layout.content_item, null);
        }
        LinearLayout ll = (LinearLayout) view;
        ListView listview = (ListView) ll.getChildAt(0);

        ContextualUndoAdapter adapter = adapterByHeader.get(getItem(i));
        adapter.setAbsListView(listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
        setListViewHeightBasedOnChildren(listview, adapter);
        listview.setId(i);
        return view;
    }

    private void setListViewHeightBasedOnChildren(ListView listView, ContextualUndoAdapter adapter) {

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        //All elements have same height
        View childView = adapter.getView(0, null, listView);
        childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = childView.getMeasuredHeight() * adapter.getCount();
        params.height = height
                + (listView.getDividerHeight() * (adapter.getCount() - 1));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        List<Task> associatedTasks = taskManager.getTasksToDisplayByHeader().get(getItem(parentId));
        Task task = associatedTasks.get(position);
        Intent intent = new Intent(homeActivity, EditActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        homeActivity.startActivityForResult(intent, HomeFragment.EDIT_ACTIVITY);
    }

    @Override
    public void deleteItem(int position, View view) {
        View parent = (View) view.getParent();
        int parentId = parent.getId();
        List<Task> associatedTasks = taskManager.getTasksToDisplayByHeader().get(getItem(parentId));
        Task task = associatedTasks.get(position);
        task.setArchived(true);
        taskManager.save(task);
        this.notifyDataSetChanged();
    }

    public void expandAll() {
        for (int i = 0; i < taskManager.getTasksToDisplayByHeader().size(); i++)
            if(!this.isExpanded(i))
                this.expand(i);
    }
}
