package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout regUsername, regPassword;
    Button loginBtn, callSignUp;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hooks
        regUsername = findViewById(R.id.username);
        regPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.submit);
        callSignUp = findViewById(R.id.signup_screen);


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Login Button Click Listener
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Navigate to SignUp
        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });


    }

    private Boolean validateUsername() {
        String val = regUsername.getEditText().getText().toString();
        if (val.isEmpty()) {
            regUsername.setError("Field cannot be empty");
            return false;
        } else {
            regUsername.setError(null);
            regUsername.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = regPassword.getEditText().getText().toString();
        if (val.isEmpty()) {
            regPassword.setError("Field cannot be empty");
            return false;
        } else {
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            return true;
        }
    }

    private void loginUser() {
        if (!validateUsername() | !validatePassword()) {
            return;
        }

        // Get user inputs
        String enteredUsername = regUsername.getEditText().getText().toString().trim();
        String enteredPassword = regPassword.getEditText().getText().toString().trim();

        // Query Firestore
        db.collection("users")
                .whereEqualTo("username", enteredUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QuerySnapshot result = task.getResult();
                        DocumentSnapshot document = result.getDocuments().get(0);
                        String passwordFromDB = document.getString("password");

                        if (passwordFromDB != null && passwordFromDB.equals(enteredPassword)) {
                            // Login successful
                            Intent intent = new Intent(LoginActivity.this, PostActivity.class);
                            intent.putExtra("username", document.getString("username"));
                            intent.putExtra("email", document.getString("email"));
                            intent.putExtra("phone", document.getString("phone"));
                            startActivity(intent);
                        } else {
                            // Incorrect password
                            regPassword.setError("Incorrect Password");
                            regPassword.requestFocus();
                        }
                    } else {
                        // Username not found
                        regUsername.setError("No such user exists");
                        regUsername.requestFocus();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error querying user", e);
                });
    }
}
