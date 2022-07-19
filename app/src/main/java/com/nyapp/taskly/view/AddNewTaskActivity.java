package com.nyapp.taskly.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nyapp.taskly.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class AddNewTaskActivity extends AppCompatActivity {
    private static final String TAG = "AddNewTaskActivity";

    private static final String KEY_NAME = "Name";
    private static final String KEY_DESCRIPTION = "Description";
    private static final String KEY_DATE = "Date";
    private static final String KEY_ASSIGNMENT = "Assignment";
    private static final String KEY_STATUS = "Status";
    final Calendar myCalendar = Calendar.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText taskNameInput;
    private EditText taskDescriptionInput;
    private EditText taskDateInput;
    private Spinner taskAssignmentInput;
    private SharedPreferences sp;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);
        sp = getApplicationContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);

        taskNameInput = findViewById(R.id.EditTextTaskName);
        taskDescriptionInput = findViewById(R.id.EditTextTaskDescription);
        taskDateInput = findViewById(R.id.EditTextDate);
        taskAssignmentInput = findViewById(R.id.SpinnerAssignment);

        initSpinner();
        initDatePicker();

        Button buttonCreateTask = findViewById(R.id.ButtonAddTask);

        buttonCreateTask.setOnClickListener(view -> saveNewTask(taskNameInput.getText().toString(), taskDescriptionInput.getText().toString(),
                taskDateInput.getText().toString(), taskAssignmentInput.getSelectedItem().toString()));

    }

    private void initSpinner() {
        groupName = sp.getString("groupName", "");
        List<String> users = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskAssignmentInput.setAdapter(adapter);
        db.collection("groups").document(groupName).collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                users.add("");
                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                    String user = queryDocumentSnapshot.getString("Username");
                    users.add(user);
                }
                adapter.notifyDataSetChanged();

            }
        });
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener datePickListener = (view1, sYear, sMonth, sDay) -> {
            myCalendar.set(Calendar.YEAR, sYear);
            myCalendar.set(Calendar.MONTH, sMonth);
            myCalendar.set(Calendar.DAY_OF_MONTH, sDay);
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.GERMANY);
            taskDateInput.setText(dateFormat.format(myCalendar.getTime()));


        };
        taskDateInput.setOnClickListener(view2 -> new DatePickerDialog(AddNewTaskActivity.this, datePickListener, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void saveNewTask(String Name, String Description, String Date, String Assignment) {

        if (Name.trim().isEmpty() || Date.trim().isEmpty()) {
            Toast.makeText(this, "please insert a name and select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        String taskStatus;
        if (Assignment.equals("")) {
            taskStatus = "not assigned";
        } else {
            taskStatus = "assigned";
        }

        Map<String, Object> task = new HashMap<>();
        task.put(KEY_NAME, Name);
        task.put(KEY_DESCRIPTION, Description);
        task.put(KEY_DATE, Date);
        task.put(KEY_ASSIGNMENT, Assignment);
        task.put(KEY_STATUS, taskStatus);
        groupName = sp.getString("groupName", "");

        db.collection("groups")
                .document(groupName)
                .collection("tasks")
                .document()
                .set(task)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(AddNewTaskActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddNewTaskActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    finish();
                });
        finish();
    }
}