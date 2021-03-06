package com.example.cs496_tab_tutorial;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.icu.util.Calendar;
import android.location.LocationManager;
import android.os.Build;

import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import static android.content.Context.LOCATION_SERVICE;

public class ThirdFragment extends Fragment {

    Button NotifButton;
    TextView textView;
    static final int REQUEST = 0;


    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds

    public double longitude, latitude;

    protected LocationManager locationManager;

    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup containter, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_fragment3, null);

        textView = (TextView) view.findViewById(R.id.textView);
        NotifButton = (Button) view.findViewById(R.id.NotifButton);
        ImageView iv = (ImageView)view.findViewById(R.id.imageView1);
        final AnimationDrawable drawable =  (AnimationDrawable) iv.getBackground();
        drawable.start();

        Button NotifButton = (Button) view.findViewById(R.id.NotifButton);

        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean onoff = sharedPref.getBoolean("onoff", false);

        int hour = sharedPref.getInt("hour", 0);
        int min = sharedPref.getInt("min",0);
        String time = String.format("%02d:%02d", hour, min);
        System.out.println(time);
        textView.setText(time);

        LabeledSwitch labeledSwitch = view.findViewById(R.id.switch_sample);

        labeledSwitch.setOn(onoff);
        if(!onoff){
            textView.setVisibility(View.INVISIBLE);
        }
        labeledSwitch.setOnToggledListener(new OnToggledListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean onoff) {
                if(onoff){
                    // check gps permission and request
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST);
                    }
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("onoff", onoff);
                    editor.commit();
                    textView.setVisibility(View.VISIBLE);
                    setAlarm();
                }
                else{
                    SharedPreferences.Editor editor = sharedPref.edit();
                    onoff = false;
                    editor.putBoolean("onoff", onoff);
                    editor.commit();
                    textView.setVisibility(View.INVISIBLE);

                }

            }

        });

        createNotificationChannel();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST);
        }

        NotifButton.setOnClickListener(new Button.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view){
                TimePickerDialog timedialog = new TimePickerDialog(
                        getActivity(),
                        timelistener,
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        false
                );
                timedialog.show();
//                DatePickerDialog datedialog = new DatePickerDialog(
//                        getActivity(),
//                        datelistener,
//                        Calendar.getInstance().get(Calendar.YEAR),
//                        Calendar.getInstance().get(Calendar.MONTH),
//                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
//                );
//                datedialog.show();

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                boolean onoff = sharedPref.getBoolean("onoff", true);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("onoff", true);
                editor.commit();
            }
        });
        
        return view;
    }


    private TimePickerDialog.OnTimeSetListener timelistener = new TimePickerDialog.OnTimeSetListener() {
        int hour, min;
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int min) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("hour", hour);
            editor.putInt("min", min);
            editor.commit();
            String time = String.format("%02d:%02d", hour, min);
            textView.setText(time);
            setAlarm();
        }
    };

//    private DatePickerDialog.OnDateSetListener datelistener = new DatePickerDialog.OnDateSetListener(){
//        @Override
//        public void onDateSet(DatePicker view, int year, int month, int day){
//            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putInt("year", year);
//            editor.putInt("month", month);
//            editor.putInt("day", day);
//            editor.commit();
//        }
//    };

    public class AlarmHATT {
        private Context context;

        public AlarmHATT(Context context) {
            this.context=context;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void Alarm() {
            AlarmManager am = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), BroadcastD.class);

            PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            //알람시간 calendar에 set해주기

            int year, month, day, hour, min;
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            year = sharedPref.getInt("year", calendar.get(Calendar.YEAR));
            month = sharedPref.getInt("month", calendar.get(Calendar.MONTH));
            day = sharedPref.getInt("day", calendar.get(Calendar.DAY_OF_MONTH));
            hour = sharedPref.getInt("hour", calendar.get(Calendar.HOUR_OF_DAY));
            min = sharedPref.getInt("min", calendar.get(Calendar.MINUTE));

//            System.out.println(Integer.toString(year) + Integer.toString(month) + Integer.toString(day) + Integer.toString(hour) + Integer.toString(min));
            calendar.set(year, month, day, hour, min, 0);

            //알람 예약
            long diff = calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
//            System.out.println("time diff: " + diff);
            if(diff > 0)
                am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY ,sender);
        }
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String CHANNEL_ID = getString(R.string.channel_id);
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setAlarm() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean onoff = sharedPref.getBoolean("onoff", true);
        if (onoff) {
            new AlarmHATT(getActivity()).Alarm();
//                }
        }
    }
}

