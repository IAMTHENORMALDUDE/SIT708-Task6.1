package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    private TextView question1Title, question1Result;
    private TextView question2Title, question2Result;
    private TextView question3Title, question3Result;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Initialize UI components
        question1Title = findViewById(R.id.question1Title);
        question1Result = findViewById(R.id.question1Result);
        question2Title = findViewById(R.id.question2Title);
        question2Result = findViewById(R.id.question2Result);
        question3Title = findViewById(R.id.question3Title);
        question3Result = findViewById(R.id.question3Result);
        continueButton = findViewById(R.id.continueButton);

        // Get quiz results from intent
        Intent intent = getIntent();
        if (intent != null) {
            String q1 = intent.getStringExtra("QUESTION_1");
            String q2 = intent.getStringExtra("QUESTION_2");
            String q3 = intent.getStringExtra("QUESTION_3");
            boolean correct1 = intent.getBooleanExtra("CORRECT_1", false);
            boolean correct2 = intent.getBooleanExtra("CORRECT_2", false);
            boolean correct3 = intent.getBooleanExtra("CORRECT_3", false);

            // Set question titles
            question1Title.setText("1. " + q1);
            question2Title.setText("2. " + q2);
            question3Title.setText("3. " + q3);

            // Set result texts
            question1Result.setText(getResultText(correct1));
            question1Result.setTextColor(getResources().getColor(correct1 ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
            
            question2Result.setText(getResultText(correct2));
            question2Result.setTextColor(getResources().getColor(correct2 ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
            
            question3Result.setText(getResultText(correct3));
            question3Result.setTextColor(getResources().getColor(correct3 ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        }

        // Set continue button click listener
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to dashboard
                Intent dashboardIntent = new Intent(ResultsActivity.this, DashboardActivity.class);
                dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(dashboardIntent);
                finish();
            }
        });
    }

    private String getResultText(boolean correct) {
        if (correct) {
            return "Correct! Great job!";
        } else {
            return "Incorrect. Keep learning!";
        }
    }
}
