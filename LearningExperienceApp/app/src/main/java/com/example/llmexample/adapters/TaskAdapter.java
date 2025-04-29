package com.example.llmexample.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.llmexample.R;
import com.example.llmexample.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task, listener);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription;
        ImageView taskIndicator;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskIndicator = itemView.findViewById(R.id.taskIndicator);
        }

        void bind(final Task task, final OnTaskClickListener listener) {
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());
            
            // Change indicator color based on completion status
            if (task.isCompleted()) {
                taskIndicator.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                taskIndicator.setColorFilter(itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
            }
            
            // Set click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onTaskClick(task);
                }
            });
        }
    }
}
