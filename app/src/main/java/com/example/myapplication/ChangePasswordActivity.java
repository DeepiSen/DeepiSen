package com.example.myapplication;

import static java.lang.Math.log;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {

    TextInputLayout currentPasswordInput, newPasswordInput,mobileNumberInput;
    Button changePasswordBtn;
    FirebaseFirestore db;
    //String userId  = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Replace with the actual user identifier




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Hooks
        mobileNumberInput = findViewById(R.id.mobileNumber);
        currentPasswordInput = findViewById(R.id.currentPassword);
        newPasswordInput = findViewById(R.id.newPassword);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();




        // Handle password change
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobileNumber = mobileNumberInput.getEditText().getText().toString().trim();
                String currentPassword = currentPasswordInput.getEditText().getText().toString().trim();
                String newPassword = newPasswordInput.getEditText().getText().toString().trim();

                if (validatePassword(newPassword)) {
                    checkAndChangePassword(mobileNumber,currentPassword, newPassword);
                }
            }
        });
    }


    private void checkAndChangePassword(String mobileNumber,String currentPassword, String newPassword) {
        DocumentReference userRef = db.collection("users").document(mobileNumber);
        Log.d("user id","User ID: " + mobileNumber);


        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve current password and last three passwords
                String currentPasswordFromDB = documentSnapshot.getString("password");
                List<String> oldPasswords = (List<String>) documentSnapshot.get("oldPasswords");

                if (oldPasswords == null) {
                    oldPasswords = new ArrayList<>();
                }

                // Check if the current password matches
                if (!currentPasswordFromDB.equals(currentPassword)) {
                    currentPasswordInput.setError("Incorrect current password");
                    currentPasswordInput.requestFocus();
                    return;
                }

                // Check if the new password is the same as the last three passwords
                if (oldPasswords.contains(newPassword) || newPassword.equals(currentPasswordFromDB)) {
                    newPasswordInput.setError("New password must not be the same as the last 3 passwords");
                    newPasswordInput.requestFocus();
                    return;
                }

                // Update password and oldPasswords in Firestore
                oldPasswords.add(currentPasswordFromDB);
                if (oldPasswords.size() > 3) {
                    oldPasswords.remove(0); // Keep only the last 3 passwords
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("password", newPassword);
                updates.put("oldPasswords", oldPasswords);

                userRef.update(updates)
                        .addOnSuccessListener(aVoid -> { Toast.makeText(ChangePasswordActivity.this, "Password updated successfully. Please log in again.", Toast.LENGTH_SHORT).show();

                            // Redirecting to LoginActivity after a short delay
                            new android.os.Handler().postDelayed(() -> {
                                FirebaseAuth.getInstance().signOut(); // Sign out the user
                                Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                startActivity(intent);
                                finish();
                            }, 2000);
                        });
            } else {
                Log.e("Firestore", "User not found");
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching user data", e));
    }

    private Boolean validatePassword(String val) {
        String passwordVal = "^" +
                "(?=.*[a-zA-Z])" +      // any letter
                "(?=.*[@#$%^&+=])" +    // at least 1 special character
                "(?=\\S+$)" +           // no white spaces
                ".{4,}" +               // at least 4 characters
                "$";
        if (val.isEmpty()) {
            newPasswordInput.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            newPasswordInput.setError("Password is too weak");
            return false;
        } else {
            newPasswordInput.setError(null);
            newPasswordInput.setErrorEnabled(false);
            return true;
        }
    }


}
