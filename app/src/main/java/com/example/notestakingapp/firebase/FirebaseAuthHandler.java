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
                            SharedPreferences sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("userEmail", email);
                            editor.apply();
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
        SharedPreferences sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "signOut:success");
        updateUI(null, context);
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
            // save user info in shared preferences
            SharedPreferences preferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("user_id", user.getUid());
            editor.putString("user_email", user.getEmail());
            editor.apply();
            Toast.makeText(context, "Welcome, " + user.getEmail(), Toast.LENGTH_SHORT).show();

            // redirect to home screen
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
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
