package com.project.dc.calendarapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

class ClientThread implements  Runnable {

    String serverFromMessage;
    String inputMessage;
    MainActivity activity;

    ArrayList<String> serverIPList = new ArrayList<>();
    static int counter = 0;

    public ClientThread(String inputMessage, String serverFromMessage, MainActivity activity){
        this.serverFromMessage = serverFromMessage;
        this.inputMessage = inputMessage;
        this.activity = activity;

        serverIPList.add("52.33.62.204");
        serverIPList.add("52.34.38.68");
        serverIPList.add("52.32.28.254");
        serverIPList.add("52.32.159.167");
        serverIPList.add("52.37.123.228");
    }

    private Socket socket;
    private static final int SERVERPORT = 8600;
    private static final String SERVER_IP = "172.20.66.164";

    @Override
    public void run() {
        String str = "";
        try {
            System.out.println("Starting client thread...");

            //***** R-R for Server...
            boolean gotIp = false;
            while(gotIp == false) {
                InetAddress serverAddr = InetAddress.getByName(serverIPList.get(counter % serverIPList.size()));

                try {
                    socket = new Socket(serverAddr, SERVERPORT);
                    gotIp = true;
                    break;
                } catch (Exception ex) {
                    counter ++;
                }

            }

            System.out.println("Connected to server IP:" + socket.getInetAddress().getHostName());

            str = this.inputMessage;
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(str);
            out.flush();
            System.out.println("Sent msg to server...");
            // Thread.sleep(1100);
            InputStream ios = socket.getInputStream();
            String message = null;
            Scanner scan = new Scanner(ios);
            if(scan.hasNext()) {
                message = scan.nextLine();
                if (message != null) {
                    serverFromMessage = message;
                }
            }
            socket.close();
            String message1 = "";
            if(serverFromMessage.equals("success")) {
                if(str.startsWith("schedule")) {
                    message1 = "Your appointment has been scheduled successfully";
                    String[] tokens = str.split(" ");
                    activity.schedule.add(new String(tokens[1]));
                    activity.actualSchedule.add(new String(str));
                }
                else if(str.startsWith("cancel")) {
                    message1 = "Your appointment has been cancelled successfully";
                    String[] tokens = str.split(" ");
                    activity.schedule.remove(new String(tokens[1]));
                    str = str.replace("cancel", "schedule");
                    activity.actualSchedule.remove(new String(str));
                }
            }
            else {
                if(str.startsWith("schedule"))
                    message1 = "Your appointment could not be scheduled. Please try again";
                else if(str.startsWith("cancel"))
                    message1 = "Your appointment could not be cancelled. Please try again";
            }
            showNotification(message1);
            counter ++;

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        finally {
            try {
                socket.close();
                str = "";
                this.inputMessage = "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showNotification( String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity);
        mBuilder.setSmallIcon(R.drawable.icon).setColor(Color.rgb(80, 147, 28));
        mBuilder.setVibrate(new long[] {100,250}).setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setContentTitle("Notification Alert, Click Me!");
        mBuilder.setContentText(message);
        Intent resultIntent = new Intent(activity, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) activity.getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(3, mBuilder.build());
    }
}
