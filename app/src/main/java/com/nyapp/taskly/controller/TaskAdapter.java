package com.nyapp.taskly.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nyapp.taskly.R;
import com.nyapp.taskly.model.Task;

public class TaskAdapter extends FirestoreRecyclerAdapter<Task, TaskAdapter.TaskHolder> {
    private OnTaskClickListener listener;

    public TaskAdapter(@NonNull FirestoreRecyclerOptions<Task> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskHolder holder, int position, @NonNull Task model) {
        holder.Name.setText(model.getName());
        holder.Status.setText(model.getStatus());
        holder.Assignment.setText(model.getAssignment());
        holder.Date.setText(model.getDate());
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item
                , parent, false);
        return new TaskHolder(view, listener);
    }

    public void deleteTask(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public interface OnTaskClickListener {
        void onTaskClick(DocumentSnapshot documentSnapshot, int position);
    }

    class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView Name;
        TextView Status;
        TextView Assignment;
        TextView Date;
        OnTaskClickListener listener;

        public TaskHolder(@NonNull View itemView, OnTaskClickListener listener) {
            super(itemView);
            Name = itemView.findViewById(R.id.textViewName);
            Status = itemView.findViewById(R.id.textViewDone);
            Assignment = itemView.findViewById(R.id.textViewAssignment);
            Date = itemView.findViewById(R.id.textViewDate);
            this.listener = listener;

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTaskClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }

        @Override
        public void onClick(View view) {

        }
    }
}
