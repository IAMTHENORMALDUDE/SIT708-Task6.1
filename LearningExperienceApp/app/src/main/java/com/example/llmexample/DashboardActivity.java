package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.llmexample.adapters.TaskAdapter;
import com.example.llmexample.models.AppDatabase;
import com.example.llmexample.models.Task;
import com.example.llmexample.models.TaskDao;
import com.example.llmexample.models.UserInterestDao;
import com.example.llmexample.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private TextView greetingText, taskCountText;
    private RecyclerView tasksRecyclerView;
    private ProgressBar loadingIndicator;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private AppDatabase database;
    private TaskDao taskDao;
    private UserInterestDao userInterestDao;
    private SessionManager sessionManager;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize database and DAOs
        database = AppDatabase.getInstance(this);
        taskDao = database.taskDao();
        userInterestDao = database.userInterestDao();
        sessionManager = new SessionManager(this);
        executor = Executors.newSingleThreadExecutor();

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        greetingText = findViewById(R.id.greetingText);
        taskCountText = findViewById(R.id.taskCountText);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Set greeting text
        String username = sessionManager.getUsername();
        greetingText.setText("Hello,\n" + username);

        // Initialize RecyclerView
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskAdapter);

        // Load tasks
        loadTasks();
    }

    private void loadTasks() {
        loadingIndicator.setVisibility(View.VISIBLE);
        final String username = sessionManager.getUsername();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Get user's tasks
                final List<Task> tasks = taskDao.getTasksByUsername(username);

                // If no tasks exist, generate some based on user interests
                if (tasks.isEmpty()) {
                    generateTasksFromInterests(username);
                } else {
                    // Update UI with existing tasks
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTaskList(tasks);
                        }
                    });
                }
            }
        });
    }

    private void generateTasksFromInterests(final String username) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Get user interests
                List<String> interests = userInterestDao.getUserInterests(username);
                final List<Task> generatedTasks = new ArrayList<>();

                // If user has interests, generate tasks based on them
                if (!interests.isEmpty()) {
                    Random random = new Random();
                    int numTasks = Math.min(3, interests.size()); // Generate up to 3 tasks

                    for (int i = 0; i < numTasks; i++) {
                        // Pick a random interest
                        String interest = interests.get(random.nextInt(interests.size()));
                        interests.remove(interest); // Remove to avoid duplicates

                        // Create a task for this interest
                        Task task = new Task(username, 
                                "Learn about " + interest, 
                                "Test your knowledge on " + interest + " concepts", 
                                interest);

                        // Save to database
                        long taskId = taskDao.insert(task);
                        task.setId((int) taskId);
                        generatedTasks.add(task);
                    }
                }

                // Update UI with generated tasks
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTaskList(generatedTasks);
                    }
                });
            }
        });
    }

    private void updateTaskList(List<Task> tasks) {
        loadingIndicator.setVisibility(View.GONE);
        taskList.clear();
        taskList.addAll(tasks);
        taskAdapter.notifyDataSetChanged();

        // Update task count text
        int incompleteTasks = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                incompleteTasks++;
            }
        }

        if (incompleteTasks == 0) {
            taskCountText.setText("You have no pending tasks");
        } else if (incompleteTasks == 1) {
            taskCountText.setText("You have 1 task due");
        } else {
            taskCountText.setText("You have " + incompleteTasks + " tasks due");
        }
    }

    @Override
    public void onTaskClick(Task task) {
        // Launch quiz activity for the selected task
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_DESCRIPTION", task.getDescription());
        intent.putExtra("TASK_TOPIC", task.getTopic());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tasks when returning to this activity
        if (sessionManager.isLoggedIn()) {
            loadTasks();
        }
    }
}
