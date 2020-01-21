package com.example.herence;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class listen extends AppCompatActivity implements View.OnClickListener {

    boolean recording, playing;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private static String appFolder, userFolder, fileExtension, fullPath = null;
    private FloatingActionButton fab = null;
    private ProgressBar progressBar = null;
    private TextView textView, textViewInfo = null;
    private Button button = null;
    private ImageButton shareButton = null;
    private LinearLayout pll = null;
    private LinearLayout hll = null;
    private int userRecordCount;
    private final int maxRecordCount = 3;
    private int idControl;
    private CountDownTimer cdt = null;
    private String loggedOnUser = null;
    private int recordCount;
    private String record = "Record";
    FirebaseUser firebaseUser = null;
    private String friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordCount = 0;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_listen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recording = false;
        playing = false;
        idControl = 0;
        pll = findViewById(R.id.linearLayout);
        friendList = "";


        fileExtension = ".3gp";

        if(firebaseUser != null) {
            try{
                loggedOnUser = firebaseUser.getEmail();
                appFolder = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
                userFolder = appFolder + File.separator + loggedOnUser;

                File folder = new File(userFolder);
                if (!folder.exists()) {
                    boolean folderCreated = folder.mkdirs();
                    if(folderCreated)
                        Log.i("Folder is", "Created");
                } else {
                    if (Objects.requireNonNull(folder.list()).length > 0) {
                        loadExistingLocalRecords(userFolder);
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                           != PackageManager.PERMISSION_GRANTED) {
                       // Permission is not granted
                       ActivityCompat.requestPermissions(listen.this,
                               new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                   } else {
                       //Make the recordings !!!HERE!!!
                       fullPath = userFolder + File.separator + record + userRecordCount + fileExtension;
                       if (userRecordCount < maxRecordCount) {
                           if (!recording) {
                               fab.setImageResource(R.drawable.stop96);
                               startRecording(fullPath);
                               textViewInfo = findViewById(R.id.textViewInformant);
                               cdt = new CountDownTimer(10100, 1000) {
                                   int cd = 10;

                                   @Override
                                   public void onTick(long millisUntilFinished) {
                                       textViewInfo.setText(R.string.recording + cd);
                                       cd--;
                                   }

                                   @Override
                                   public void onFinish() {
                                       stopRecording();
                                       fab.setImageResource(R.drawable.microphone);
                                       createRecordField(fullPath, (record + userRecordCount));
                                       userRecordCount++;
                                       textViewInfo.setText(R.string.finished);
                                       //recording = false;

                                   }
                               };
                               cdt.start();
                           } else {
                               stopRecording();
                               cdt.cancel();
                               textViewInfo.setText(R.string.finished);
                               fab.setImageResource(R.drawable.microphone);
                               createRecordField(fullPath, (record + userRecordCount));
                               userRecordCount++;
                           }
                           recording = !recording;
                       } else {
                           Toast.makeText(listen.this, "You only have 3 rights!", Toast.LENGTH_SHORT).show();
                       }
                   }
               }
           });
    }

    public void loadExistingLocalRecords(String filePath){
        File folder = new File(filePath);
        if (folder.exists() && Objects.requireNonNull(folder.list()).length > 0) {
            Log.i("WARNING", "Files found at internal storage");
            for(String file : Objects.requireNonNull(folder.list())){
                String recordPath = filePath + File.separator + record + recordCount + fileExtension;
                createRecordField(recordPath, record + recordCount);
                recordCount++;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("Permission", "Granted!");

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "OLMADI BRO", Toast.LENGTH_SHORT).show();
                }
            }
            case 0:{
                Log.i("Permission", "Request Code is different!");

            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    //Creating Menu & elements
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.logout);
        item.setTitle("Logout(" + loggedOnUser + ")");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("friendList", friendList);
            startActivity(intent);
        } else if (item.getItemId() == R.id.removeall) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Herence");
            builder.setMessage("Do you really want to remove all records?");
            builder.setNegativeButton("Cancel", null);
            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    File dir = new File(userFolder);
                    if(dir.length() > 0) {
                        File[] filenames = dir.listFiles();
                        for (File file : Objects.requireNonNull(filenames)) {
                            file.delete();
                        }
                    }
                    pll.removeAllViews();
                    userRecordCount = 0;
                    idControl = 0;
                }
            });
            builder.show();
        }else{
            Intent intent = new Intent(getApplicationContext(), FriendList.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeOnline(String username) {
        ParseQuery<ParseObject> q = ParseQuery.getQuery("HerenceAudio");
        q.whereEqualTo("username", username);
        q.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject obj : objects) {
                    obj.deleteInBackground();
                }
            }
        });


    }

    //Creating recordings buttons explanation and progressbar
    public void createRecordField(String fileAsTag, String songName) {
        idControl++;
        hll = new LinearLayout(this);
        hll.setOrientation(LinearLayout.HORIZONTAL);
        hll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        pbparams.weight = 2f;
        pbparams.setMargins(10, 0, 10, 0);
        pbparams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(pbparams);
        progressBar.setScaleY(3f);
        progressBar.setId(idControl * 10);
        LinearLayout.LayoutParams playButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        playButtonParams.gravity = Gravity.CENTER;
        button = new Button(this);
        button.setLayoutParams(playButtonParams);
        button.setText(R.string.play);
        //button = new ImageButton(this);
        //button.setImageResource(R.drawable.play96);
        button.setTag(fileAsTag);
        button.setId(idControl);
        textView = new TextView(this);
        textView.setText(songName);
        LinearLayout.LayoutParams twparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        twparams.setMargins(10, 0, 10, 0);
        twparams.gravity = Gravity.CENTER;
        textView.setLayoutParams(twparams);
        shareButton = new ImageButton(this);
        shareButton.setId(idControl*100);
        shareButton.setTag(fileAsTag);
        shareButton.setImageResource(R.drawable.share_symbol);
        LinearLayout.LayoutParams shareButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        shareButtonParams.weight = 0.1f;
        shareButtonParams.gravity = Gravity.CENTER;
        shareButton.setLayoutParams(shareButtonParams);
        shareButton.setBackground(null);
        shareButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        shareButton.setScaleX(0.5f);
        shareButton.setScaleY(0.5f);
        shareButton.setPadding(10,10,10,10);
        shareButton.setAdjustViewBounds(true);
        /*
        lineBetweenRecords = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 10);
        lineBetweenRecords.setLayoutParams(lineParams);
        lineBetweenRecords.setBackgroundColor(Color.GRAY);
         */
        hll.addView(button);
        hll.addView(textView);
        hll.addView(progressBar);
        hll.addView(shareButton);
        pll.addView(hll);
        //pll.addView(lineBetweenRecords);
        button.setOnClickListener(this);
        shareButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        String filePath = v.getTag().toString();
        if (id < 100) {
            button = (Button) v;
            if (!playing) {
                button.setText(R.string.stop);
                //button.setImageResource(R.drawable.stop48);
                startPlaying(filePath);
                disableOtherButtons(id);
                //disableEnableControls(false, pll);
                getProgressBarDuration(progressBar, id, filePath);
            } else {
                button.setText(R.string.play);
                //button.setImageResource(R.drawable.play96);
                stopPlaying();
                enableOtherButtons(id);
                //disableEnableControls(true, pll);
                progressBar = findViewById(id * 10);
                progressBar.setProgress(0);
                cdt.cancel();


            }
            playing = !playing;
        }
        else {
            shareButton = (ImageButton) v;
            Log.i("ShareButton", String.valueOf(v.getId()));
            Intent intent = new Intent(getApplicationContext(), FriendList.class);
            startActivity(intent);
        }

    }


    private void disableEnableControls(boolean enable, ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }

    private void disableOtherButtons(int id) {
        for (int i = 0; i < pll.getChildCount(); i++) {
            LinearLayout ll = (LinearLayout) pll.getChildAt(i);
            View child;
            if (i != id - 1) {
                for (int j = 0; j < ll.getChildCount(); j++) {
                    child = ll.getChildAt(j);
                    child.setEnabled(false);
                }
            }
        }
    }

    private void enableOtherButtons(int id) {
        for (int i = 0; i < pll.getChildCount(); i++) {
            LinearLayout ll = (LinearLayout) pll.getChildAt(i);
            View child;
            if (i != id - 1) {
                for (int j = 0; j < ll.getChildCount(); j++) {
                    child = ll.getChildAt(j);
                    child.setEnabled(true);
                }

            }
        }
    }

    public void getProgressBarDuration(ProgressBar pb, final int id, String pathStr) {
        progressBar = findViewById(id * 10);
        Uri uri = Uri.parse(pathStr);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        final int millSecond = Integer.parseInt(durationStr);
        progressBar.setMax(millSecond);
        final int[] currentProgress = {0};
        progressBar.setProgress(currentProgress[0]);
        cdt = new CountDownTimer(millSecond + 200, 100) {

            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(currentProgress[0] += 100);
            }

            public void onFinish() {
                playing = false;
                //enableOtherButtons(id);
                disableEnableControls(true, pll);
                button = findViewById(id);
                button.setText(R.string.play);
                //button.setImageResource(R.drawable.play96);
            }
        };
        cdt.start();
    }


    // Recorder Settings
    private void startRecording(String filePath) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    // Player Settings
    private void startPlaying(String path) {
        player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
        //playing = false;
    }


    public ArrayList<String> stringToArray (String value){
        ArrayList<String> result = new ArrayList<>();
        String replace = value.replaceAll("^\\[|]$", "");
        Collections.addAll(result, replace.split(","));
        return result;
    }
}
