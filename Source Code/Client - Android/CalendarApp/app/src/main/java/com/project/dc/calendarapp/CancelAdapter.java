package com.project.dc.calendarapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class CancelAdapter extends ArrayAdapter<String> {
    private final List<String> scheduledItems;

    private Button cancelButton;
    private MainActivity mainActivity;

    public CancelAdapter(Context context, int resource, List<String> scheduledItems, MainActivity mainActivity) {
        super(context, resource, scheduledItems);
        this.scheduledItems = scheduledItems;
        this.mainActivity = mainActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String scheduledItem = scheduledItems.get(position);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.cancel_list_row, null);
        cancelButton = (Button) row.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callToCancel(scheduledItem);
                notifyDataSetChanged();
            }
        });

        TextView nameTextView = (TextView) row.findViewById(R.id.schedule);
        nameTextView.setText(scheduledItem);
        return row;
    }

    public void callToCancel(String scheduledItem) {
        for(String item: mainActivity.actualSchedule) {
            System.out.println("Item is:" + item);
        }

        for(String item: mainActivity.schedule) {
            System.out.println("Item is:" + item);
        }
        int position = Collections.binarySearch(mainActivity.schedule, scheduledItem);
        String messageToServer = mainActivity.actualSchedule.get(position);
        messageToServer = messageToServer.replace("schedule", "cancel");
        String serverFromMessage = "";

        Thread t = new Thread(new ClientThread(messageToServer, serverFromMessage, mainActivity));
        t.start();
        try {
            t.join();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}



