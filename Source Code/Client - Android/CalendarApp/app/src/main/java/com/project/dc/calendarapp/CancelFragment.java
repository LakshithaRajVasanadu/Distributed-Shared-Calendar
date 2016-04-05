package com.project.dc.calendarapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class CancelFragment extends Fragment {
     MainActivity activity;

    public CancelFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cancel, container, false);

        List<String> scheduledItems = activity.schedule;
        ListView listView = (ListView) view.findViewById(R.id.scheduleList);
        activity.cancelAdapter = new CancelAdapter(activity.getBaseContext(), R.layout.cancel_list_row, scheduledItems, activity);
        listView.setAdapter(activity.cancelAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.cancelAdapter.notifyDataSetChanged();

    }
}
