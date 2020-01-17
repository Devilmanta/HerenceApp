package com.example.herence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private String username, password;
    private EditText usernameEditText, passwordEditText;
    private Button button;
    private FirebaseAuth mAuth;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        if (currentUser != null) {
            logUserInActivity();

        }

    }

    public void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Signed-in!", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            logUserInActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Sign-in Failed", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Logged-in!", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            logUserInActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Login-Fail!", "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void logUserInActivity() {
        Intent intent = new Intent(getApplicationContext(), listen.class);
        startActivity(intent);
    }

    public void switchLoginSignUp(View view) {
        TextView textView = findViewById(view.getId());
        Button button = findViewById(R.id.loginButton);
        if (textView.getTag().equals("login")) {
            textView.setTag("signup");
            textView.setText("or, Login!");
            button.setText("Sign Up!");
            button.setTag("signup");
        } else {
            textView.setTag("login");
            textView.setText("or, Sign Up!");
            button.setText("Login");
            button.setTag("login");
        }
    }

    public void loginSignupButtonAction(View view) {

        button = (Button) view;
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        Log.i("buttonlog", button.getTag().toString());
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Username or Password can't be empty! ", Toast.LENGTH_SHORT).show();
        } else {
            if (button.getTag() == "signup") {
                createAccount(username, password);
            } else {
                signIn(username, password);
            }
        }
    }

}
