package com.example.todomanager001;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PreviousData extends AppCompatActivity {

    String okmail;
    ListView listView;
    private CustomAdapter2 adapter;
    DatabaseReference databaseReference;
    List<realtimedata> data=new ArrayList<>();
    private FirebaseAuth mAuth;
    FirebaseDatabase database=FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_data);
        okmail = getIntent().getStringExtra("key");
        databaseReference = FirebaseDatabase.getInstance().getReference("previousData");
        listView=findViewById(R.id.listid2);
        loadData();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                realtimedata item=(realtimedata) parent.getItemAtPosition(position);
                databaseReference.child(okmail).child(item.getKey()).removeValue();
                loadData();
                Toast.makeText(PreviousData.this, "Removed from history!", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.item2, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.gobackid){
            finish();
        }
        else if(item.getItemId()==R.id.clearid){
            databaseReference.child(okmail).removeValue();
            loadData();
            Toast.makeText(PreviousData.this, "Cleared All Previous Data!", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
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
                CustomAdapter adapter=new CustomAdapter(PreviousData.this, data);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}