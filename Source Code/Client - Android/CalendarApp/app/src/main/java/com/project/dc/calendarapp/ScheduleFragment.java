package com.project.dc.calendarapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment implements MultiSpinner.OnMultipleItemsSelectedListener {

    private Spinner userSpinner;
    private Spinner daySpinner;
    private TextView participants;
    private static final String[] users = {"Jack0", "Bob1", "John2", "Andrew3", "Charlie4", "Dan5", "Eric6"};
    private static final String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    String text = "";
    String dayText = "";
    EditText startTimeEditText;
    EditText endTimeEditText;
    View view;
    String serverFromMessage = "";
    String messageToServer = "schedule ";
    EditText scheduleName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_schedule, container, false);
        final MainActivity activity = (MainActivity) getActivity();
        participants = (TextView) view.findViewById(R.id.participants);
        userSpinner = (Spinner)view.findViewById(R.id.users);
        scheduleName = (EditText) view.findViewById(R.id.scheduleName);
        startTimeEditText = (EditText)view.findViewById(R.id.startTime);
        endTimeEditText = (EditText)view.findViewById(R.id.endTime);

        daySpinner = (Spinner)view.findViewById(R.id.days);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity().getBaseContext(),
                android.R.layout.simple_spinner_item,days);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter1);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dayText = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                dayText = "";
            }
        });

        MultiSpinner multiSelectionSpinner = (MultiSpinner) view.findViewById(R.id.users);
        multiSelectionSpinner.setItems(users);
        multiSelectionSpinner.setListener(this);
        activity.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameText = scheduleName.getText().toString();
                messageToServer += scheduleName.getText() + " ";
                String modifiedParticipants = participants.getText().toString().replace(" ", "");
                String participantText ="(" + modifiedParticipants + ") ";
                messageToServer += participantText;
                String startTime = startTimeEditText.getText().toString();
                String endTime = endTimeEditText.getText().toString();
                String timeText = "(" +startTime + "," + endTime + ") ";
                messageToServer += timeText;
                messageToServer += dayText;
                System.out.println("Message to Server...");
                System.out.println(messageToServer);
                if(nameText == null || nameText.length() <= 0 || startTime == null || startTime.length() <=0
                        || endTime == null || endTime.length() <= 0 || participants.getText() == null || participants.getText().length() <=0 ||  dayText == null || dayText.length() <= 0) {
                    Toast.makeText(activity.getBaseContext(), "Enter details", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessageToServer(messageToServer);
                    messageToServer = "schedule ";
                }
            }
        });
        return view;
    }


    @Override
    public void selectedIndices(List<Integer> indices) {

    }

    @Override
    public void selectedStrings(List<String> strings) {
        participants.setText(strings.toString().substring(1, strings.toString().length()-1));
    }

    public void sendMessageToServer(String messageToServer) {
        Thread t = new Thread(new ClientThread(messageToServer, serverFromMessage, (MainActivity)getActivity()));
        t.start();

        try {
            t.join();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
