package com.example.todomanager001;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

public class CustomAdapter2 extends ArrayAdapter<realtimedata> {

    private Activity context;
    private List<realtimedata> data;

    public CustomAdapter2(Activity context, List<realtimedata> data) {
        super(context, R.layout.samplelayout, data);
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=context.getLayoutInflater();
        View view=layoutInflater.inflate(R.layout.samplelayout2, null, true);

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

        return view;
    }
}

