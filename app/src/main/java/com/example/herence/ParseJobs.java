package com.example.herence;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.mirrajabi.searchdialog.core.Searchable;


public class ParseJobs {
    public int result;
    private String objectID;

    private byte[] convert(String path) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fis.read(b)) != -1; ) {
            bos.write(b, 0, readNum);
        }

        byte[] bytes = bos.toByteArray();

        return bytes;
    }

    public void convertByteToAudio(byte[] buf, String path) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        FileOutputStream fos = new FileOutputStream(path);
        byte[] b = new byte[1024];

        for (int readNum; (readNum = bis.read(b)) != -1;) {
            fos.write(b, 0, readNum);
        }
    }

    public void uploadAudio(String audioURLString) throws Exception {
        Log.i("upload", "logging.......................");
        byte[] byteArray = convert(audioURLString);
        ParseFile file = new ParseFile("herence.3gp", byteArray);
        ParseObject object = new ParseObject("HerenceAudio");
        object.put("record", file);
        object.put("username", ParseUser.getCurrentUser().getUsername());
        object.put("buttonTag", audioURLString);
        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("ParseInfo", "Upload successfull");

                } else {
                    Log.i("ParseError", e.getMessage());
                }
            }
        });
    }

    public ArrayList<SearchModel> getAllUserList() throws ParseException {
        String currentUser = ParseUser.getCurrentUser().getUsername();
        Log.i("CurrentUser", currentUser);
        ArrayList<SearchModel> allUsers = new ArrayList<>();
        ArrayList<ParseUser> queryResult = new ArrayList<>();
        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        queryResult = (ArrayList<ParseUser>) userParseQuery.find();
        for (int i=0; i<queryResult.size(); i++){
            if(!queryResult.get(i).getUsername().equals(currentUser)) {
                Log.i("user: ", queryResult.get(i).getUsername());
                allUsers.add(new SearchModel(queryResult.get(i).getUsername()));
            }
        }
        return allUsers;
    }

    public void addFriend(final String friend) throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", user.getUsername());
        query.getInBackground(user.getObjectId(), new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if(e==null){
                    object.addUnique("friendlist", friend);
                    object.saveInBackground();
                }else{
                    Log.i("Error", e.getMessage());
                }
            }
        });
    }

    public ArrayList<String> getFriendList() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", user.getUsername());
        List<ParseUser> users = new ArrayList<>();
        List<String> friendlist = new ArrayList<>();
        try {
            users = query.find();
            if(users.get(0).getList("friendlist") != null) {
                friendlist = users.get(0).getList("friendlist");
            }else{
                friendlist.add("You have no friends :'(");
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        ArrayList<String> resultList = (ArrayList<String>)friendlist;
        return resultList;
    }

    public ArrayList<String> loadRecordPaths(String username) {
        ArrayList<String> filePaths = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("HerenceAudio");
        query.whereEqualTo("username", username);
        try {
            List<ParseObject> result = query.find();
            for (ParseObject object : result) {
                filePaths.add(object.getString("buttonTag"));
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
        }
        return filePaths;
    }

    public int getOnlineRecordCount() {
        int returnResult = 0;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("HerenceAudio");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        try {
            List<ParseObject> results = query.find();
            returnResult = results.size();
            return returnResult;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return returnResult;
    }

    public String findObjectId(String username, String filePath){
        objectID = null;
        List<ParseObject> objects = new ArrayList<>();
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("HerenceAudio");
        parseQuery.whereEqualTo("username", username);
        parseQuery.whereEqualTo("buttonTag", filePath);
        try {
            objects = parseQuery.find();
            objectID = objects.get(0).getObjectId();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return objectID;
    }
}
