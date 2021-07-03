package com.example.todomanager001;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

//finishing

public class MainActivity extends AppCompatActivity {

    ListView listView;
    private CustomAdapter adapter;
    DatabaseReference databaseReference, databaseReference2;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;

    //for alert dialog
    private AlertDialog.Builder dialogbuilder;
    private AlertDialog dialog;
    EditText heading, details;
    TextView date, time;
    Button cancel, save;
    private String mail;
    private  String okmail="";
    List<realtimedata> data=new ArrayList<>();

    //for time picker
    TimePickerDialog timePickerDialog;
    Calendar calendar;
    int currentHour;
    int currentMinute;
    String amPm;

    //for date picker
    DatePickerDialog datePickerDialog;

    //for database key
    String keyDate, keyTime, warningtext="( ! )";

    //swipe down to refresh
    SwipeRefreshLayout refresh;

    //for auto refresh
    Handler mHandler;

    //for editing or deleting task
    TextView edit, delete, markasdone;

    //for progressbar
    ProgressBar progressBar;

    //for notification
    private setNotification mSetNotification;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //for auto refreshing and sending notifications
        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,30000);

        //for notification
        mSetNotification=new setNotification(this);

        //get the mail from login/register page and edit it for the key
        mAuth=FirebaseAuth.getInstance();
        mail = getIntent().getStringExtra("key");
        databaseReference = FirebaseDatabase.getInstance().getReference("currentData");
        databaseReference2 = FirebaseDatabase.getInstance().getReference("previousData");
        for(int i=0;i<mail.length();i++)
        {
            if(mail.charAt(i)=='.') okmail+='@';
            else okmail+=mail.charAt(i);
        }//the key does not support '.' in the string, so covered it up with '@'


        //long tap to edit or delete item-
        listView=findViewById(R.id.listid);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                realtimedata item=(realtimedata) parent.getItemAtPosition(position);
                editOrDeleteData(item);
                return false;
            }
        });

        //swipe down to refresh
        refresh=findViewById(R.id.layout);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshlist();
            }
        });
    }

    @Override
    protected void onStart() {
        loadData();
        super.onStart();
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing ToDoManager")
                .setMessage("Are you sure you want to close the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.item, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.instructionsid){
            showInstructionDialog();
        }
        else if(item.getItemId()==R.id.shareid){
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            String aboutapp="Only you can manage your time and we are here to help you. Organize you life and never feel short of time ever again!";
            intent.putExtra(Intent.EXTRA_TEXT, aboutapp);

            startActivity(Intent.createChooser(intent, "share with"));
            return true;
        }
        else if(item.getItemId()==R.id.aboutusid){
            showAboutMeDialog();
            return true;
        }
        else if(item.getItemId()==R.id.addid){
            addNewTaskDialogBuilder();
        }
        else if(item.getItemId()==R.id.previousid){
            Intent intent=new Intent(MainActivity.this, PreviousData.class);
            intent.putExtra("key", okmail);
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.logoutid){
            mAuth.signOut();
            Intent intent=new Intent(MainActivity.this, loginPage.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void showInstructionDialog(){
        dialogbuilder=new AlertDialog.Builder(MainActivity.this);
        final View popupView=getLayoutInflater().inflate(R.layout.instructionpopup, null);
        dialogbuilder.setView(popupView);
        dialog=dialogbuilder.create();
        Button backButton=popupView.findViewById(R.id.instructionBackId);

        dialog.show();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public void showAboutMeDialog(){
        dialogbuilder=new AlertDialog.Builder(MainActivity.this);
        final View popupView=getLayoutInflater().inflate(R.layout.aboutmeopup, null);
        dialogbuilder.setView(popupView);
        dialog=dialogbuilder.create();
        Button backButton=popupView.findViewById(R.id.aboutmeBackid);

        dialog.show();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void loadData() {
        databaseReference.child(okmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                data.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    realtimedata realtimedata2=snapshot1.getValue(realtimedata.class);
                    data.add(realtimedata2);
                }
                CustomAdapter adapter=new CustomAdapter(MainActivity.this, data);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        checkNotifications();
    }

    public void addNewTaskDialogBuilder(){
        dialogbuilder=new AlertDialog.Builder(MainActivity.this);
        final View popupView=getLayoutInflater().inflate(R.layout.popup, null);

        heading=(EditText)popupView.findViewById(R.id.headingid);
        details=(EditText)popupView.findViewById(R.id.detailsid);


        //fixing input time format
        time=(TextView)popupView.findViewById(R.id.timeid);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                            hourOfDay -= 12;
                        }
                        else amPm = "AM";

                        keyTime=amPm;
                        if(hourOfDay>9) keyTime+=""+hourOfDay;
                        else if(hourOfDay>0) keyTime+="0"+hourOfDay;
                        else keyTime+="00";

                        if(minutes>9) keyTime+=""+minutes;
                        else if(minutes>0) keyTime+="0"+minutes;
                        else keyTime+="00";

                        if(hourOfDay==0) hourOfDay=12;
                        time.setText(String.format("%02d:%02d ", hourOfDay, minutes) + amPm);
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.show();
            }
        });

        //fixing input date format
        date=(TextView)popupView.findViewById(R.id.dateid);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();
                int year=calendar.get(Calendar.YEAR);
                int month=calendar.get(Calendar.MONTH);
                int day=calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        if(month>9 && dayOfMonth>9){
                            date.setText(dayOfMonth+"/"+month+"/"+year);
                            keyDate=year+""+month+""+dayOfMonth;
                        }
                        else if(dayOfMonth>9 && month<9){
                            date.setText(dayOfMonth+"/0"+month+"/"+year);
                            keyDate=year+"0"+month+""+dayOfMonth;
                        }
                        else if(dayOfMonth<9 && month>9){
                            date.setText("0"+dayOfMonth+"/"+month+"/"+year);
                            keyDate=year+""+month+"0"+dayOfMonth;
                        }
                        else{
                            date.setText("0"+dayOfMonth+"/0"+month+"/"+year);
                            keyDate=year+"0"+month+"0"+dayOfMonth;
                        }
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });


        save=(Button)popupView.findViewById(R.id.saveid);
        cancel=(Button)popupView.findViewById(R.id.cancelid);

        dialogbuilder.setView(popupView);
        dialog=dialogbuilder.create();

        dialog.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task=heading.getText().toString().trim();
                String taskdetails=details.getText().toString().trim();
                String dates=date.getText().toString().trim();
                String times=time.getText().toString().trim();
                try{
                    saveData(task, taskdetails, dates, times);
                }catch (Exception e){

                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void editOrDeleteData(realtimedata item){
        dialogbuilder=new AlertDialog.Builder(MainActivity.this);
        final View popupView=getLayoutInflater().inflate(R.layout.popup2, null);

        edit=(TextView)popupView.findViewById(R.id.edittaskid);
        delete=(TextView)popupView.findViewById(R.id.deletetaskid);
        markasdone=(TextView)popupView.findViewById(R.id.markasdoneid);
        cancel=(Button)popupView.findViewById(R.id.cancelfrompopup2id);

        dialogbuilder.setView(popupView);
        dialog=dialogbuilder.create();

        dialog.show();

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                editData(item);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData(item.getKey());
                loadData();
                Toast.makeText(MainActivity.this, "Deleted Successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        markasdone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveAsHistory(item);
                databaseReference2.child(okmail).child(item.getKey()).setValue(item);
                deleteData(item.getKey());
                loadData();

                Toast.makeText(MainActivity.this, "Task Completed!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //set on click listener to the texts
    }
    public void editData(realtimedata item){
        dialogbuilder=new AlertDialog.Builder(MainActivity.this);
        final View popupView=getLayoutInflater().inflate(R.layout.popup, null);

        TextView textView=popupView.findViewById(R.id.textView);
        textView.setText("Edit Task");

        heading=(EditText)popupView.findViewById(R.id.headingid);
        heading.setText(item.getHeading());

        details=(EditText)popupView.findViewById(R.id.detailsid);
        details.setText(item.getDetails());

        //set initial keytime-
        keyTime="";
        String hours="", minutes="", ampm="";
        String previousTime=item.getTime();
        int i;
        for(i=0;i<previousTime.length();i++){
            if(previousTime.charAt(i)==':') break;
            else hours+=previousTime.charAt(i);
        }
        if(hours.charAt(0)=='1' && hours.charAt(1)=='2') hours="00";
        for (i=i+1;i<previousTime.length();i++){
            if(previousTime.charAt(i)==' ') break;
            else minutes+=previousTime.charAt(i);
        }
        for (i=i+1;i<previousTime.length();i++){
            if(previousTime.charAt(i)==' ') break;
            else ampm+=previousTime.charAt(i);
        }
        keyTime=ampm+""+hours+""+minutes;

        //fixing input time format
        time=(TextView)popupView.findViewById(R.id.timeid);
        time.setText(item.getTime());
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);

                timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                            hourOfDay -= 12;
                        }
                        else amPm = "AM";

                        keyTime=amPm;
                        if(hourOfDay>9) keyTime+=""+hourOfDay;
                        else if(hourOfDay>0) keyTime+="0"+hourOfDay;
                        else keyTime+="00";

                        if(minutes>9) keyTime+=""+minutes;
                        else if(minutes>0) keyTime+="0"+minutes;
                        else keyTime+="00";

                        if(hourOfDay==0) hourOfDay=12;
                        time.setText(String.format("%02d:%02d ", hourOfDay, minutes) + amPm);
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.show();
            }
        });


        //set initial keydate-
        keyDate="";
        String years="", months="", days="";
        String previousDate=item.getDate();
        for(i=0;i<previousDate.length();i++){
            if(previousDate.charAt(i)=='/') break;
            else days+=previousDate.charAt(i);
        }
        for(i=i+1;i<previousDate.length();i++){
            if(previousDate.charAt(i)=='/') break;
            else months+=previousDate.charAt(i);
        }
        for(i=i+1;i<previousDate.length();i++){
            if(previousDate.charAt(i)=='/') break;
            else years+=previousDate.charAt(i);
        }
        keyDate=years+""+months+""+days;
        //fixing input date format
        date=(TextView)popupView.findViewById(R.id.dateid);
        date.setText(item.getDate());
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar=Calendar.getInstance();
                int year=calendar.get(Calendar.YEAR);
                int month=calendar.get(Calendar.MONTH);
                int day=calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        if(month>9 && dayOfMonth>9){
                            date.setText(dayOfMonth+"/"+month+"/"+year);
                            keyDate=year+""+month+""+dayOfMonth;
                        }
                        else if(dayOfMonth>9 && month<9){
                            date.setText(dayOfMonth+"/0"+month+"/"+year);
                            keyDate=year+"0"+month+""+dayOfMonth;
                        }
                        else if(dayOfMonth<9 && month>9){
                            date.setText("0"+dayOfMonth+"/"+month+"/"+year);
                            keyDate=year+""+month+"0"+dayOfMonth;
                        }
                        else{
                            date.setText("0"+dayOfMonth+"/0"+month+"/"+year);
                            keyDate=year+"0"+month+"0"+dayOfMonth;
                        }
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        save=(Button)popupView.findViewById(R.id.saveid);
        cancel=(Button)popupView.findViewById(R.id.cancelid);

        dialogbuilder.setView(popupView);
        dialog=dialogbuilder.create();

        dialog.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task=heading.getText().toString().trim();
                String taskdetails=details.getText().toString().trim();
                String dates=date.getText().toString().trim();
                String times=time.getText().toString().trim();

                try {
                    deleteData(item.getKey());
                }catch (Exception e){

                }
                try {
                    saveData(task, taskdetails, dates, times);
                }catch (Exception e){

                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void saveData(String task, String taskdetails, String dates, String times){

        if(task.length()==0){
            heading.setError("Heading cannot be empty!");
            heading.requestFocus();
            return;
        }
        else if(dates.equals("Click to add Date")){
            date.setError("Add a Date!");
            date.requestFocus();
            return;
        }
        else if(times.equals("Click to add Time")){
            time.setError("Add a Time!");
            time.requestFocus();
            return;
        }
        String key=okmail.concat(keyDate);
        key=key.concat(keyTime);

        //adding task to the key
        for(int i=0;i<task.length();i++)
        {
            if((task.charAt(i)>='A' && task.charAt(i)<='Z') || (task.charAt(i)>='a' && task.charAt(i)<='z')) key+=task.charAt(i);
            else key+='@';
        }//the key does not support '.' in the string, so covered it up with '@'

        //adding taskdetails to the key
        for(int i=0;i<taskdetails.length();i++)
        {
            if((taskdetails.charAt(i)>='A' && taskdetails.charAt(i)<='Z') || (taskdetails.charAt(i)>='a' && taskdetails.charAt(i)<='z')) key+=taskdetails.charAt(i);
            else key+='@';
        }//the key does not support '.' in the string, so covered it up with '@'

        realtimedata newtask=new realtimedata(key, task, taskdetails, times, dates, warningtext);
        databaseReference.child(okmail).child(key).setValue(newtask).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                loadData();
                Toast.makeText(MainActivity.this, "Data Saved Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.dismiss();
    }

    public void deleteData(String key){
        databaseReference.child(okmail).child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                loadData();
            }
        });
    }

    public void refreshlist(){
        loadData();
        refresh.setRefreshing(false);
    }

    //checking notification
    public void checkNotifications(){
        NotificationManagerCompat.from(MainActivity.this).cancelAll();
        if(isAppForground(MainActivity.this)){
            return;
        }
        for(int i=0;i<data.size();i++){
            realtimedata item=data.get(i);
            if(timeisover(item.getDate(), item.getTime())){
                sendNotification(item.getHeading(), item.getDetails(), i);
            }
        }
    }
    //don't send notification if app is running
    public static boolean isAppForground(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : l) {
            if (info.uid == context.getApplicationInfo().uid && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
    public void sendNotification(String task, String details, int id){
        NotificationCompat.Builder nb=mSetNotification.getNotification(task+" ( Times up! )", details);
        mSetNotification.getManager().notify(id, nb.build());
    }
    private boolean timeisover(String date, String time) {
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
        if(ampm.charAt(0)=='P') hour+=12;
        if(hour>currentHour) return false;
        else if(hour<currentHour) return true;

        Integer minute = Integer.valueOf(minutes);
        if(minute>currentMinute) return false;
        else return true;
    }

    //for auto refresh
    private final Runnable m_Runnable = new Runnable() {
        public void run() {
            loadData();
            MainActivity.this.mHandler.postDelayed(m_Runnable, 30000);
        }
    };
}