package com.example.android.se_bt_on_off;

import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{
    final static String TAG = "MainActivity";
    Button mStartTimeButton;
    Button mEndTimeButton;
    Button mStartButton;

    EditText mBTAdressTV;
    Calendar mStartTime;
    Calendar mEndTime;
    int mIntervalDuration = 5; // in seconds.
    int mFoundCounter = 0;

    BluetoothAdapter mBluetoothAdapter;

    SimpleDateFormat sf = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mStartTimeButton = (Button) findViewById(R.id.btn_start_time);
        mEndTimeButton = (Button) findViewById(R.id.btn_end_time);
        mStartButton = (Button) findViewById(R.id.btn_start);
        mBTAdressTV = (EditText) findViewById(R.id.tv_bt_address);

        mStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mcurrentTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        mcurrentTime.set(Calendar.MINUTE, selectedMinute);
                        mcurrentTime.set(Calendar.SECOND, 0);
                        mStartTime = mcurrentTime;
                        mStartTimeButton.setText(sf.format(mStartTime.getTime()));
                    }
                }, hour, minute, true);

                timePicker.setTitle("Select start time");
                timePicker.show();
            }
        });

        mEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog timePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mcurrentTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                        mcurrentTime.set(Calendar.MINUTE, selectedMinute);
                        mcurrentTime.set(Calendar.SECOND, 0);
                        mEndTime = mcurrentTime;
                        mEndTimeButton.setText(sf.format(mEndTime.getTime()));
                    }
                }, hour, minute, true);

                timePicker.setTitle("Select end time");
                timePicker.show();
            }
        });

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.cancelDiscovery();
                        mBluetoothAdapter.startDiscovery();
                        Log.i(TAG, "Starting BT discovery");
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(BluetoothDevice.ACTION_FOUND);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                        BroadcastReceiver mReceiver = new BroadcastReceiver() {
                            public void onReceive(Context context, Intent intent) {
                                Calendar currentTime = Calendar.getInstance();
                                if (currentTime.compareTo(mStartTime) < 0)
                                    return;
                                if (currentTime.compareTo(mEndTime) > 0) {
                                    timer.cancel();
                                    return;
                                }

                                String action = intent.getAction();

                                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                                    Log.i(TAG, "Discover really started");
                                }

                                //Finding devices
                                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                    String addressOfDevice = device.getAddress();

                                    Log.i(TAG, "Found some BT device with address: " + addressOfDevice);
                                    if (addressOfDevice.equals(mBTAdressTV.getText().toString())) {
                                        mFoundCounter += 1;
                                        Log.i(TAG, "Desired BT device found");
                                        Calendar c = Calendar.getInstance();
                                        NotificationCompat.Builder mBuilder =
                                                new NotificationCompat.Builder(getApplicationContext())
                                                        .setSmallIcon(R.drawable.ic_launcher_background)
                                                        .setContentTitle("Found desired BT device: ")
                                                        .setContentText(addressOfDevice + " for " + mFoundCounter + " times");

                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                                        mNotificationManager.notify(001, mBuilder.build());
                                        mBluetoothAdapter.cancelDiscovery();
                                    }
                                }

                                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                                    Log.i(TAG, "Discover really stopped");
                                }
                            }
                        };

                        registerReceiver(mReceiver, filter);
                    }
                }, 0, 7000);

//
//                while (mStartTime.compareTo(mEndTime) < 0) {
//
//                    mStartTime.add(Calendar.SECOND, mIntervalDuration);
//                }
            }
        });
    }
}
