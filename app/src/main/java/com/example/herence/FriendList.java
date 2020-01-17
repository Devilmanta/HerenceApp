package com.example.herence;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompatSideChannelService;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class FriendList extends AppCompatActivity {

    private ListView friendList = null;
    private ArrayList<String> friendListArray = null;
    private ArrayAdapter adapter = null;
    private ArrayList<SearchModel> userList = null;
    private boolean hasRecord = false;
    private String objectID = "";
    private String currentUser = null;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        friendList = findViewById(R.id.listViewFriendList);
        friendListArray = new ArrayList<>();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUser = firebaseUser.getDisplayName();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstname = String.valueOf(dataSnapshot.child("EX4iGCMi1BmrLvRJaFRd").child("firstname"));
                Log.i("Firstname", firstname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(getIntent().getExtras() != null){
            hasRecord = true;
            Intent intent = getIntent();
            objectID = intent.getStringExtra("objectID");
        }

        userList = new ArrayList<>();

        refreshFriendList();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.addfriend);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
            }
        });

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent sendMessageIntent = new Intent(getApplicationContext(), ChatActivity.class);
                if(hasRecord){
                    sendMessageIntent.putExtra("objectID", objectID);
                    sendMessageIntent.putExtra("activeUser", friendListArray.get(position));
                    startActivity(sendMessageIntent);
                } else{
                    sendMessageIntent.putExtra("activeUser", friendListArray.get(position));
                    startActivity(sendMessageIntent);
                    Log.i("objectID", "IS NULL YOU DON'T HAVE TO WORRY ABOUT IT!!!");
                }


            }
        });
    }
    public void addFriend(){
        try {
            new SimpleSearchDialogCompat<>(FriendList.this, "Here some advices", "Who are you looking for?",
                    null, userList, new SearchResultListener<SearchModel>() {
                @Override
                public void onSelected(BaseSearchDialogCompat dialog, SearchModel item, int position) {
                    Log.i("Selected", item.getTitle());
                        if(friendListArray.get(0).equals("You have no friends :'("))
                            friendListArray.clear();
                        if (!friendListArray.contains(item.getTitle())) {
                            friendListArray.add(item.getTitle());
                            adapter.notifyDataSetChanged();
                            userList.remove(position);
                        } else
                            Toast.makeText(FriendList.this, "This person already in your list!", Toast.LENGTH_LONG).show();

                    dialog.dismiss();
                }
            }).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshFriendList(){
        adapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, friendListArray);
        friendList.setAdapter(adapter);
    }


}
