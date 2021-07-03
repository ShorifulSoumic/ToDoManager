package com.example.todomanager001;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.Settings.System.getString;

public class CustomAdapter extends ArrayAdapter<realtimedata> {

    private Activity context;
    private List<realtimedata> data;

    public CustomAdapter(Activity context, List<realtimedata> data) {
        super(context, R.layout.samplelayout, data);
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=context.getLayoutInflater();
        View view=layoutInflater.inflate(R.layout.samplelayout, null, true);

        realtimedata realtimedata=data.get(position);
        TextView heading=view.findViewById(R.id.heading_id);
        TextView details=view.findViewById(R.id.details_id);
        TextView time=view.findViewById(R.id.time_id);
        TextView date=view.findViewById(R.id.date_id);
        TextView warning=view.findViewById(R.id.warningid);

        heading.setText(realtimedata.getHeading());
        details.setText(realtimedata.getDetails());
        time.setText(realtimedata.getTime());
        date.setText(realtimedata.getDate());
        if(timeisup(realtimedata.getDate(), realtimedata.getTime())) {
            warning.setText(realtimedata.getWarning());
            warning.setTextColor(Color.parseColor("#FF0000"));
            warning.setBackgroundColor(Color.parseColor("#FFE500"));
        }
        return view;
    }

    public Boolean timeisup(String date, String time){
        //add conditions ot see if time is up

        Calendar calendar=Calendar.getInstance();
        int currentYear=calendar.get(Calendar.YEAR);
        int currentMonth=calendar.get(Calendar.MONTH);
        int currentDay=calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        int i=0;
        String days="", months="", years="", hours="", minutes="", ampm="";
        for(i=0;i<date.length();i++){
            if(date.charAt(i)=='/') break;
            days+=date.charAt(i);
        }
        for(i=i+1;i<date.length();i++){
            if(date.charAt(i)=='/') break;
            months+=date.charAt(i);
        }
        for(i=i+1;i<date.length();i++){
            if(date.charAt(i)=='/') break;
            years+=date.charAt(i);
        }

        Integer year = Integer.valueOf(years);
        if(year>currentYear) return false;
        else if(year<currentYear) return true;

        Integer month = Integer.valueOf(months);
        if(month>currentMonth+1) return false;
        else if(month<currentMonth+1) return true;

        Integer day = Integer.valueOf(days);
        if(day>currentDay) return false;
        else if(day<currentDay) return true;

        for(i=0;i<time.length();i++){
            if(time.charAt(i)==':') break;
            hours+=time.charAt(i);
        }
        for(i=i+1;i<time.length();i++){
            if(time.charAt(i)==' ') break;
            minutes+=time.charAt(i);
        }
        for(i=i+1;i<time.length();i++){
            ampm+=time.charAt(i);
        }
        Integer hour = Integer.valueOf(hours);
        if(ampm.charAt(0)=='P' && hour!=12) hour+=12;
        else if (ampm.charAt(0)=='A' && hour==12) hour=0;
        if(hour>currentHour) return false;
        else if(hour<currentHour) return true;

        Integer minute = Integer.valueOf(minutes);
        if(minute>currentMinute) return false;
        else return true;
    }
}
