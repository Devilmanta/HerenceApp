package com.example.herence;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ParseConnection extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);
        /* user + M4vSrVYUj6yt*/
        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(this)
                /*
                .applicationId("k0fzAIpVEWQfajpaeVu3N8eMZWDyi5iGPGivzVG9")
                .clientKey("id957lGt1XAnWruRVy9Tb5TKRxaR27XrVW2k5fDu")
                .server("https://parseapi.back4app.com/")
                .build()
                */
                .applicationId(getString(R.string.back4app_app_id))
                // if defined
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        );
        /*
        ParseObject object = new ParseObject("ExampleObject");
        object.put("myNumber", "123");
        object.put("myString", "engur");

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    Log.i("Parse Result", "Successful!");
                } else {
                    Log.i("Parse Result", "Failed" + ex.toString());
                }
            }
        });

        */
        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
