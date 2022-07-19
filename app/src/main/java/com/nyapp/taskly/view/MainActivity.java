package com.nyapp.taskly.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nyapp.taskly.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String KEY_USERNAME = "Username";
    private static final String KEY_GROUP_NAME = "Group name";
    private static final String KEY_USER_COUNT = "User Count";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sp;
    private String groupName;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("UserPref", Context.MODE_PRIVATE);

        groupName = sp.getString("groupName", "");
        username = sp.getString("username", "");

        if (!groupName.equals("") && !username.equals("")) {
            startActivity(new Intent(MainActivity.this, TaskListActivity.class));
        }
        EditText usernameInput = findViewById(R.id.editTextChooseUsername);
        EditText groupNameInput = findViewById(R.id.editTextChooseGroupName);

        Button buttonJoin = findViewById(R.id.buttonJoinGroup);
        Button buttonCreate = findViewById(R.id.buttonCreateGroup);


        buttonJoin.setOnClickListener(view -> {
            username = usernameInput.getText().toString();
            groupName = groupNameInput.getText().toString();
            if (username.equals("") | groupName.equals("")) {
                Toast.makeText(MainActivity.this, "Username / group name can't be Empty", Toast.LENGTH_SHORT).show();
            } else {
                joinGroup();
            }
        });

        buttonCreate.setOnClickListener(view -> {
            username = usernameInput.getText().toString();
            groupName = groupNameInput.getText().toString();

            if (username.equals("") | groupName.equals("")) {
                Toast.makeText(MainActivity.this, "Username / group name can't be Empty", Toast.LENGTH_SHORT).show();
            } else {
                createGroup();
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        groupName = sp.getString("groupName", "");
        username = sp.getString("username", "");

        EditText usernameInput = findViewById(R.id.editTextChooseUsername);
        EditText groupNameInput = findViewById(R.id.editTextChooseGroupName);
        if (!groupName.equals("") && !username.equals("")) {
            usernameInput.setText(sp.getString("username", ""));
            groupNameInput.setText(sp.getString("groupName", ""));
        } else {
            usernameInput.setText("");
            groupNameInput.setText("");
        }
    }

    public void createGroup() {

        //does the group exist already?
        db.collection("groups").document(groupName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    //Group does exist
                    if (documentSnapshot.exists()) {
                        Toast.makeText(MainActivity.this, "Group exists already", Toast.LENGTH_SHORT).show();
                        //Group does not exist
                    } else {
                        saveData();
                        createUser();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                });

    }


    public void joinGroup() {
        DocumentReference userRef = db.collection("groups").document(groupName).collection("users").document(username);
        //Check if group exists already
        db.collection("groups").document(groupName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        //Check if user exists already
                        userRef.get().addOnSuccessListener(documentSnapshot1 -> {
                            //user exists
                            if (documentSnapshot1.exists()) {
                                Toast.makeText(MainActivity.this, "User exists already", Toast.LENGTH_SHORT).show();
                                //user does not exist
                            } else {
                                saveData();
                                addUser();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        });

                    } else {
                        Toast.makeText(MainActivity.this, "Group does not exist", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                });
    }

    private void addUser() {
        DocumentReference userRef = db.collection("groups").document(groupName).collection("users").document(username);
        Map<String, Object> user = new HashMap<>();
        user.put(KEY_USERNAME, username);
        userRef.set(user)
                .addOnSuccessListener(e -> {
                    Toast.makeText(MainActivity.this, "Group joined", Toast.LENGTH_SHORT).show();
                    db.collection("groups").document(groupName).update("User Count", FieldValue.increment(1));
                    startActivity(new Intent(MainActivity.this, TaskListActivity.class));
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                });
    }

    private void createUser() {
        DocumentReference userRef = db.collection("groups").document(groupName).collection("users").document(username);
        Map<String, Object> group = new HashMap<>();
        Map<String, Object> user = new HashMap<>();
        group.put(KEY_GROUP_NAME, groupName);
        group.put(KEY_USER_COUNT, 1);
        user.put(KEY_USERNAME, username);
        db.collection("groups").document(groupName).set(group);
        userRef.set(user)
                .addOnSuccessListener(e -> {
                    Toast.makeText(MainActivity.this, "Group Created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, TaskListActivity.class));
                }).addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                });
    }

    private void saveData() {

        SharedPreferences.Editor editor = sp.edit();

        editor.putString("username", username);
        editor.putString("groupName", groupName);
        editor.apply();
    }
}