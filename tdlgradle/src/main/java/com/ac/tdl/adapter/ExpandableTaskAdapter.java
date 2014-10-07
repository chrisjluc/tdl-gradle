package com.ac.tdl.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ac.tdl.EditActivity;
import com.ac.tdl.HomeFragment;
import com.ac.tdl.MainActivity;
import com.ac.tdl.R;
import com.ac.tdl.model.Task;
import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chrisjluc on 2014-05-25.
 */
public class ExpandableTaskAdapter extends ExpandableListItemAdapter<String> implements AdapterView.OnItemClickListener,
        ContextualUndoAdapter.DeleteItemCallback {

    private MainActivity homeActivity;
    private HashMap<String, ContextualUndoAdapter> adapterByHeader;
    private HashMap<String, List<Task>> tasksByHeader;
    private List<String> headerList;

    public ExpandableTaskAdapter(Activity activity, List<String> headerList, HashMap<String, List<Task>> tasksByHeader) {

        super(activity, R.layout.expandable_item, R.id.header_layout, R.id.content_layout, headerList);

        this.homeActivity = (MainActivity) activity;
        this.tasksByHeader = tasksByHeader;
        this.adapterByHeader = new HashMap<String, ContextualUndoAdapter>();
        this.headerList = headerList;

        for (String header : headerList) {
            TaskAdapter taskAdapter = new TaskAdapter(activity, this.tasksByHeader.get(header));
            ContextualUndoAdapter adapter = new ContextualUndoAdapter(taskAdapter,
                    R.layout.undo_row, R.id.undo_row_undobutton, 2000, this);
            adapterByHeader.put(header, adapter);
        }
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
        ListView listview = (ListView) view.findViewById(R.id.lvNestedTasks);
        ContextualUndoAdapter adapter = adapterByHeader.get(getItem(i));
        if (adapter == null || listview == null)
            return view;
        adapter.setAbsListView(listview);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(this);
        setListViewHeightBasedOnChildren(listview, adapter);
        listview.setId(i);
        return view;
    }

    private void setListViewHeightBasedOnChildren(ListView listView, ContextualUndoAdapter adapter) {

        //All elements have same height
        View childView = adapter.getView(0, null, listView);
        childView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int height = childView.getMeasuredHeight() * adapter.getCount();

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height
                + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int parentId = parent.getId();
        List<Task> associatedTasks = tasksByHeader.get(getItem(parentId));
        Task task = associatedTasks.get(position);
        Intent intent = new Intent(homeActivity, EditActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        homeActivity.startActivityForResult(intent, HomeFragment.EDIT_ACTIVITY);
    }

    @Override
    public void deleteItem(int position, View view) {
        View parent = (View) view.getParent();
        int parentId = parent.getId();
        List<Task> associatedTasks = tasksByHeader.get(getItem(parentId));
        Task task = associatedTasks.get(position);
        associatedTasks.remove(position);
        task.updateArchived(true);

        homeActivity.getHomeFragment().loadTasks();
        homeActivity.updateDrawerList();
    }

    public void expandAll() {
        for (int i = 0; i < tasksByHeader.size(); i++)
            this.expand(i);
    }
}
