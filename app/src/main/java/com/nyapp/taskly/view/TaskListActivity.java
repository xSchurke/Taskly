package com.nyapp.taskly.view;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nyapp.taskly.R;
import com.nyapp.taskly.controller.TaskAdapter;
import com.nyapp.taskly.model.Task;

import java.util.ArrayList;
import java.util.List;


public class TaskListActivity extends AppCompatActivity {
    private static final String KEY_STATUS = "Status";
    private static final String KEY_ASSIGNMENT = "Assignment";
    private static final String KEY_USER_COUNT = "User Count";
    private SharedPreferences sp;
    private  String groupName;
    private  String username;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private long userCount = 0;
    private TaskAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        sp = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        username = sp.getString("username","");
        groupName = sp.getString("groupName","");

        TextView nameView = findViewById(R.id.textViewGroupName);
        nameView.setText(sp.getString("groupName",""));

        Button buttonLeave = findViewById(R.id.buttonLeave);
        buttonLeave.setOnClickListener(view2 -> leaveGroup());

        FloatingActionButton buttonNewTask = findViewById(R.id.ButtonNewTask);
        buttonNewTask.setOnClickListener(view ->
                startActivity(new Intent(TaskListActivity.this, AddNewTaskActivity.class))
        );

        initRecyclerView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void initRecyclerView() {
        Query query = db.collection("groups").document(groupName).collection("tasks").orderBy("Date", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        adapter = new TaskAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.RecyclerViewTaskList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteTask(viewHolder.getAbsoluteAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnTaskClickListener((documentSnapshot, position) -> {
            Task task = documentSnapshot.toObject(Task.class);
            String id = documentSnapshot.getId();
            showDialog(id, task);
        });

    }

    private void leaveGroup() {
        db.collection("groups").document(groupName).get()
                .addOnSuccessListener(documentSnapshot -> {
                    userCount = documentSnapshot.getLong(KEY_USER_COUNT);

                    if (userCount == 1) {
                        db.collection("groups").document(groupName).collection("tasks").get().addOnSuccessListener(queryDocumentSnapshots -> {
                            for(QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots){
                                String taskId = documentSnapshot1.getId();
                                db.collection("groups").document(groupName).collection("tasks").document(taskId).delete();
                            }
                        });
                        db.collection("groups").document(groupName).delete();

                    }

                    db.collection("groups").document(groupName).collection("users").document(username).delete();
                    db.collection("groups").document(groupName).update("User Count", FieldValue.increment(-1));

                    String emptyString = "";
                    sp.edit().putString("username", emptyString).apply();
                    sp.edit().putString("groupName", emptyString).apply();

                    finish();
                });
    }

    //show edit dialog on task click
    private void showDialog(String id, Task task) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.item_dialog);
        dialog.setTitle("task dialog");

        TextView taskNameInfo = dialog.findViewById(R.id.taskNameInfo);
        TextView taskDescriptionInfo = dialog.findViewById(R.id.taskDescriptionInfo);
        TextView taskDateInfo = dialog.findViewById(R.id.taskDateInfo);
        Spinner spinner = dialog.findViewById(R.id.spinnerReassign);

        taskNameInfo.setText(task.getName());
        taskDescriptionInfo.setText(task.getDescription());
        taskDateInfo.setText(task.getDate());

        List<String> users = new ArrayList<>();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, users);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        db.collection("groups").document(groupName).collection("users").get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                users.add("");
                for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                    String user = queryDocumentSnapshot.getString("Username");
                    users.add(user);
                }
                spinnerAdapter.notifyDataSetChanged();

            }
        });

        Button buttonSetDone = dialog.findViewById(R.id.buttonSetDone);
        Button buttonSetInProgress = dialog.findViewById(R.id.buttonSetInProgress);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonDelete = dialog.findViewById(R.id.buttonDelete);
        Button buttonReassign = dialog.findViewById(R.id.buttonReassign);

        DocumentReference ref = db.collection("groups").document(groupName).collection("tasks").document(id);

        //Done
        buttonSetDone.setOnClickListener(view -> {
            ref.update(KEY_STATUS, "done");
            dialog.dismiss();
        });
        //In Progress
        buttonSetInProgress.setOnClickListener(view -> {
            ref.update(KEY_STATUS, "in Progress");
            dialog.dismiss();
        });
        //Cancel
        buttonCancel.setOnClickListener(view -> dialog.dismiss());

        //Delete
        buttonDelete.setOnClickListener(view -> {
            ref.delete();
            dialog.dismiss();
        });
        //Reassign
        buttonReassign.setOnClickListener(view -> {
            String assignment = spinner.getSelectedItem().toString();
            if (assignment.equals("")) {
                ref.update(KEY_STATUS, "not assigned");
                ref.update(KEY_ASSIGNMENT, "");
            } else {
                ref.update(KEY_STATUS, "assigned");
                ref.update(KEY_ASSIGNMENT, assignment);
            }
            dialog.dismiss();
        });
        dialog.show();

    }

}


