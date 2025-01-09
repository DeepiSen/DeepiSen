package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    TextInputLayout regUsername, regEmail, regPassword, regPhone;
    Button reg_btn, dataSave_btn;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Hooks
        regUsername = findViewById(R.id.username);
        regEmail = findViewById(R.id.emailid);
        regPhone = findViewById(R.id.phone);
        regPassword = findViewById(R.id.password);
        reg_btn = findViewById(R.id.submit);
        dataSave_btn = findViewById(R.id.signup_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Save data in Firestore when clicking submit
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate inputs
                if (!validateUsername() | !validatePassword() | !validateEmail() | !validatePhone()) {
                    return;
                }

                // Check if username already exists
                String username = regUsername.getEditText().getText().toString().trim();
                checkUsernameExists(username);
            }
        });

        // Navigate to login screen
        dataSave_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkUsernameExists(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Username already exists
                        regUsername.setError("Username already taken. Please choose a different username.");
                        regUsername.requestFocus();
                    } else {
                        // Username is available, proceed with saving user data
                        saveUserData();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking username", e);
                });
    }

    private void saveUserData() {
        // Get all the values
        String username = regUsername.getEditText().getText().toString().trim();
        String email = regEmail.getEditText().getText().toString().trim();
        String phone = regPhone.getEditText().getText().toString().trim();
        String password = regPassword.getEditText().getText().toString().trim();

        // Create user data map
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("password", password);

        // Save data to Firestore
        db.collection("users").document(phone).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User data successfully written!");
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error writing user data", e);
                });
    }

    private Boolean validateUsername() {
        String val = regUsername.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            regUsername.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 15) {
            regUsername.setError("Username too long");
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            regUsername.setError("White spaces are not allowed");
            return false;
        } else {
            regUsername.setError(null);
            regUsername.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = regEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            regEmail.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            regEmail.setError("Invalid email address");
            return false;
        } else {
            regEmail.setError(null);
            regEmail.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePhone() {
        String val = regPhone.getEditText().getText().toString();
        String phonePattern = "[0-9]{10}";
        if (val.isEmpty()) {
            regPhone.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(phonePattern)) {
            regPhone.setError("Invalid phone number");
            return false;
        } else {
            regPhone.setError(null);
            regPhone.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = regPassword.getEditText().getText().toString();
        String passwordVal = "^" +
                "(?=.*[a-zA-Z])" +      // any letter
                "(?=.*[@#$%^&+=])" +    // at least 1 special character
                "(?=\\S+$)" +           // no white spaces
                ".{4,}" +               // at least 4 characters
                "$";
        if (val.isEmpty()) {
            regPassword.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            regPassword.setError("Password is too weak");
            return false;
        } else {
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            return true;
        }
    }
}
