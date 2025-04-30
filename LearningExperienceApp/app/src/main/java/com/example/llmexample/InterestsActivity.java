package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.models.AppDatabase;
import com.example.llmexample.models.UserInterest;
import com.example.llmexample.models.UserInterestDao;
import com.example.llmexample.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InterestsActivity extends AppCompatActivity {

    private Button nextButton;
    private List<Button> interestButtons;
    private List<String> selectedInterests;
    private AppDatabase database;
    private UserInterestDao userInterestDao;
    private SessionManager sessionManager;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        // Initialize database and DAO
        database = AppDatabase.getInstance(this);
        userInterestDao = database.userInterestDao();
        sessionManager = new SessionManager(this);
        executor = Executors.newSingleThreadExecutor();

        // Initialize UI components
        nextButton = findViewById(R.id.nextButton);
        interestButtons = new ArrayList<>();
        selectedInterests = new ArrayList<>();

        // Add all interest buttons to the list
        interestButtons.add(findViewById(R.id.interestAlgorithms));
        interestButtons.add(findViewById(R.id.interestDataStructures));
        interestButtons.add(findViewById(R.id.interestWebDevelopment));
        interestButtons.add(findViewById(R.id.interestTesting));
        interestButtons.add(findViewById(R.id.interestMobileDev));
        interestButtons.add(findViewById(R.id.interestAI));
        interestButtons.add(findViewById(R.id.interestDatabases));
        interestButtons.add(findViewById(R.id.interestNetworking));
        interestButtons.add(findViewById(R.id.interestSecurity));
        interestButtons.add(findViewById(R.id.interestCloudComputing));

        // Set up click listeners for all interest buttons
        for (Button button : interestButtons) {
            // Initialize all buttons with unselected style
            button.setBackgroundResource(R.drawable.interest_button_unselected);
            button.setTextColor(getResources().getColor(R.color.white, null));
            button.setAlpha(0.8f);
            button.setElevation(0f);
            
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleInterestSelection((Button) v);
                }
            });
        }

        // Set up next button click listener
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedInterests.isEmpty()) {
                    Toast.makeText(InterestsActivity.this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
                } else {
                    saveUserInterests();
                }
            }
        });
    }

    private void toggleInterestSelection(Button button) {
        String interest = button.getText().toString();

        if (selectedInterests.contains(interest)) {
            selectedInterests.remove(interest);
            button.setBackgroundResource(R.drawable.interest_button_unselected);
            button.setTextColor(getResources().getColor(R.color.white, null));
            button.setAlpha(0.8f);

        } else {
            if (selectedInterests.size() >= 10) {
                Toast.makeText(this, "You can select maximum 10 interests", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedInterests.add(interest);
            button.setBackgroundResource(R.drawable.interest_button_selected);
            button.setTextColor(getResources().getColor(R.color.white, null));
            button.setAlpha(1.0f);
            button.setElevation(8f);
        }

        // Provide haptic feedback
        button.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }
    private void saveUserInterests() {
        final String username = sessionManager.getUsername();
        
        if (username == null) {
            Toast.makeText(this, "User session error", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Clear existing interests for this user
                userInterestDao.deleteAllUserInterests(username);
                
                // Save new interests
                List<UserInterest> userInterests = new ArrayList<>();
                for (String interest : selectedInterests) {
                    userInterests.add(new UserInterest(username, interest));
                }
                
                userInterestDao.insertAll(userInterests);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(InterestsActivity.this, "Interests saved successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(InterestsActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
}
