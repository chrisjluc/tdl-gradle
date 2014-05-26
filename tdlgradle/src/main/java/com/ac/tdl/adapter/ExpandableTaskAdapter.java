package com.ac.tdl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ac.tdl.R;
import com.ac.tdl.model.Task;
import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chrisjluc on 2014-05-25.
 */
public class ExpandableTaskAdapter extends ExpandableListItemAdapter<String> {

    private Context context;
    private HashMap<String, ContextualUndoAdapter> adapterHashmap;

    public ExpandableTaskAdapter(Context context,List<String> headerList,HashMap<String, ContextualUndoAdapter> adapterHashmap) {
        super(context, R.layout.expandable_item, R.id.header_layout, R.id.content_layout,headerList);
        this.context = context;
        this.adapterHashmap = adapterHashmap;
        //Only one item is expanded at a time
        //setLimit(1);

    }

    @Override
    public View getTitleView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if(view == null){
            LayoutInflater vi = LayoutInflater.from(context);
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
        if(view == null){
            LayoutInflater vi = LayoutInflater.from(context);
            view = vi.inflate(R.layout.content_item, null);
        }
        ListView listview = (ListView) view.findViewById(R.id.lvNestedTasks);
        ContextualUndoAdapter adapter= adapterHashmap.get(getItem(i));
        adapter.setAbsListView(listview);
        listview.setAdapter(adapter);

        return view;
    }
}
