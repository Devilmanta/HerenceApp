package com.example.herence;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class FriendList extends AppCompatActivity {

    private ListView friendList = null;
    private ArrayList<String> friendListArray = null;
    private ArrayAdapter<String> adapter = null;
    private ArrayList<SearchModel> userList = null;
    private boolean hasRecord = false;
    private String objectID = "";
    private String contacts = "";
    private String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        friendList = findViewById(R.id.listViewFriendList);
        friendListArray = new ArrayList<>();

        readContactsData(new MyCallback() {
            @Override
            public void onCallback(ArrayList<String> friendListA) {
                adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, friendListA);
                friendList.setAdapter(adapter);
            }
        });

        userList = new ArrayList<>();

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

    public void refreshFriendList(ArrayList<String> arrayList, ListView listView){

    }

    public ArrayList<String> stringToArray (String value){
        String replace;
        ArrayList<String> result = new ArrayList<>();
        replace = value.replaceAll("^\\[|]$", "");
        for(String s : replace.split(",")){
            result.add(s.trim());
        }
        return result;
    }

    public interface MyCallback {
        void onCallback(ArrayList<String> friendList);
    }

    public void readContactsData(final MyCallback myCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> attractionsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                attractionsList= stringToArray(Objects.requireNonNull(document.get("contacts")).toString());
                            }
                            myCallback.onCallback(attractionsList);
                        }
                    }
        });
    }
}
