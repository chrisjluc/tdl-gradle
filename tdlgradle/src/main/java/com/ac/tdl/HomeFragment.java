package com.ac.tdl;

/**
 * Created by aaronte on 2014-05-25.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ac.tdl.adapter.ExpandableTaskAdapter;
import com.ac.tdl.managers.TaskManager;
import com.ac.tdl.managers.helpers.TaskListFilter;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;

public class HomeFragment extends Fragment implements View.OnClickListener {

    public static final int EDIT_ACTIVITY = 1;

    private EditText etTaskTitle;
    private ImageButton bAdd;
    private ListView lvTasks;
    private TaskManager taskManager = TaskManager.getInstance();

    ExpandableTaskAdapter adapter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setUpViews(rootView);
        loadTasks();
        return rootView;
    }

    /**
     * Called first to set up the layout
     */
    private void setUpViews(View parentView) {

        etTaskTitle = (EditText) parentView.findViewById(R.id.etTaskTitle);
        etTaskTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addTask();
                }
                return false;
            }
        });
        bAdd = (ImageButton) parentView.findViewById(R.id.bAdd);
        bAdd.setOnClickListener(this);
        lvTasks = (ListView) parentView.findViewById(R.id.lvTasks);
    }

    public void loadTasks() {
        taskManager.setFilter(new TaskListFilter(null));
        if(adapter == null) {
            adapter = new ExpandableTaskAdapter(getActivity(), taskManager.getOrderedHeaderList());
            setAdapter(lvTasks, adapter);
        } else{
            adapter.notifyDataSetChanged();
        }
    }

    public void loadTasks(String hashtag) {
        taskManager.setFilter(new TaskListFilter(hashtag));
        adapter.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }

    private void setAdapter(ListView listview, ExpandableTaskAdapter adapter) {
        listview.setAdapter(null);
        adapter.setAbsListView(listview);
        listview.setAdapter(adapter);
        adapter.expandAll();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAdd:
                addTask();
                break;
        }
    }

    private void addTask() {
        String taskTitle = etTaskTitle.getText().toString().trim();
        if (taskTitle.isEmpty()) return;
        Task task = new TaskBuilder()
                .withTaskTitle(taskTitle)
                .build();
        taskManager.save(task);
        etTaskTitle.setText("");
        etTaskTitle.setSelected(false);
        hideKeyboard(etTaskTitle);

        adapter.notifyDataSetChanged();
    }
    @SuppressWarnings("static-access")
    private void hideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}