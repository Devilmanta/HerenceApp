package com.example.herence;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String activeUser = "";
    private ListView chatListView = null;
    private ArrayAdapter<String> adapter = null;
    private ArrayList<String> messages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        activeUser = intent.getStringExtra("activeUser");
        setTitle("Chat with " + activeUser);
        messages = new ArrayList<>();
        chatListView = findViewById(R.id.chatListView);

        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, messages);

        chatListView.setAdapter(adapter);

        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Messages");

        query1.whereEqualTo("Sender", ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recipient", activeUser);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Messages");

        query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("Sender", activeUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);

        query.orderByAscending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    messages.clear();

                    if(objects.size() > 0){
                        for (ParseObject message: objects) {
                            String messageContent = message.getString("message");
                            if(!message.getString("Sender").equals(ParseUser.getCurrentUser().getUsername())){
                                messageContent = "> " + messageContent;
                            }
                            messages.add(messageContent);
                        }
                        adapter.notifyDataSetChanged();
                    }

                }
            }
        });

    }

    public void sendChat(View view) {
        final EditText chatEditText = findViewById(R.id.chatEntryEditText);

        ParseObject message = new ParseObject("Messages");

        final String messageContent = chatEditText.getText().toString();

        message.put("Sender", ParseUser.getCurrentUser().getUsername());
        message.put("recipient", activeUser);
        message.put("message", messageContent);

        chatEditText.setText("");
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    messages.add(messageContent);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
