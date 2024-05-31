package com.example.notestakingapp.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.notestakingapp.ui.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class FirebaseAuthHandler {
    public static final String TAG = "EmailPassword";
    private FirebaseAuth mAuth;

    public static String userId;


    public FirebaseAuthHandler() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void signUp(String email, String password, final Context context) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user, context);
                    } else {
                        // If sign up fails, display a message to the user
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(context, "Sign Up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null, context);
                    }
                }
            });
    }
    public void signIn(String email, String password, final Context context) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user, context);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(context, "Sign In failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        updateUI(null, context);
                    }
                }
            });
    }
    public void signOut(Context context) {
        mAuth.signOut();
        SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "signOut:success");
        updateUI(null, context);
    }
    public void changePassword(String oldPassword, final String newPassword, final Context context) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.signInWithEmailAndPassword(Objects.requireNonNull(user.getEmail()), oldPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "Failed to update password: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            }
                        } else {
                            Toast.makeText(context, "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        } else {
            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }
    public static String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            userId = null;
        }
        return userId;
    }
    public void updateUI(FirebaseUser user, Context context) {
        if (user != null) {
            // Save user info in shared preferences
            SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_id", user.getUid());
            editor.putString("user_email", user.getEmail());
            editor.apply();
            Toast.makeText(context, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }
    public void reload(Context context) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "reload:success");
                        updateUI(user, context);
                    } else {
                        Log.e(TAG, "reload:failure", task.getException());
                        Toast.makeText(context, "Failed to reload user.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    public String getUserEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            return firebaseUser.getEmail();
        }
        return null;
    }
}

