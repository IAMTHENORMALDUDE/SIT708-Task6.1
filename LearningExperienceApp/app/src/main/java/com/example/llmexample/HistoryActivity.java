package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.llmexample.adapters.HistoryAdapter;
import com.example.llmexample.models.QuizHistory;
import com.example.llmexample.utils.ApiClient;
import com.example.llmexample.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private TextView emptyHistoryText;
    private ProgressBar loadingIndicator;
    private ImageButton backButton;
    private HistoryAdapter historyAdapter;
    private List<QuizHistory> historyList;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize UI components
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyHistoryText = findViewById(R.id.emptyHistoryText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        backButton = findViewById(R.id.backButton);

        // Initialize session manager and API client
        sessionManager = new SessionManager(this);
        apiClient = ApiClient.getInstance(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Set up click listeners
        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize RecyclerView
        historyList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyList);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);

        // Load quiz history
        loadQuizHistory();
    }

    private void loadQuizHistory() {
        loadingIndicator.setVisibility(View.VISIBLE);
        String userId = sessionManager.getUsername(); // In a real app, this would be the user ID

        apiClient.getUserQuizHistory(userId, new ApiClient.QuizHistoryCallback() {
            @Override
            public void onSuccess(List<QuizHistory> quizHistoryList) {
                loadingIndicator.setVisibility(View.GONE);
                updateHistoryList(quizHistoryList);
            }

            @Override
            public void onError(String message) {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(HistoryActivity.this, "Error loading history: " + message, Toast.LENGTH_SHORT).show();
                
                // For demo purposes, create dummy data if API fails
                createDummyHistory();
            }
        });
    }

    private void createDummyHistory() {
        List<QuizHistory> dummyHistory = new ArrayList<>();
        
        // Create dummy history items
        dummyHistory.add(new QuizHistory("1", sessionManager.getUsername(), "Android Development", 3, 3, "2023-05-27T10:30:00Z"));
        dummyHistory.add(new QuizHistory("2", sessionManager.getUsername(), "Java Programming", 2, 3, "2023-05-26T14:15:00Z"));
        dummyHistory.add(new QuizHistory("3", sessionManager.getUsername(), "Mobile UI Design", 3, 3, "2023-05-25T09:45:00Z"));
        
        updateHistoryList(dummyHistory);
    }

    private void updateHistoryList(List<QuizHistory> quizHistoryList) {
        if (quizHistoryList.isEmpty()) {
            emptyHistoryText.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        } else {
            emptyHistoryText.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
            historyList.clear();
            historyList.addAll(quizHistoryList);
            historyAdapter.notifyDataSetChanged();
        }
    }
}
