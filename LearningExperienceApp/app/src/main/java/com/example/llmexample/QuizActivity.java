package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.models.AppDatabase;
import com.example.llmexample.models.QuizQuestion;
import com.example.llmexample.models.Task;
import com.example.llmexample.models.TaskDao;
import com.example.llmexample.utils.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {

    private TextView taskTitle, taskDescription;
    private Button submitButton;
    private ProgressBar loadingIndicator;
    
    private RadioGroup question1Options, question2Options, question3Options;
    private TextView question1Text, question2Text, question3Text;
    
    private List<QuizQuestion> quizQuestions;
    private int taskId;
    private String topic;
    private AppDatabase database;
    private TaskDao taskDao;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize database
        database = AppDatabase.getInstance(this);
        taskDao = database.taskDao();
        executor = Executors.newSingleThreadExecutor();

        // Get task details from intent
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getIntExtra("TASK_ID", -1);
            String title = intent.getStringExtra("TASK_TITLE");
            String description = intent.getStringExtra("TASK_DESCRIPTION");
            topic = intent.getStringExtra("TASK_TOPIC");

            if (taskId == -1 || topic == null) {
                Toast.makeText(this, "Error: Invalid task data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize UI components
            taskTitle = findViewById(R.id.taskTitle);
            taskDescription = findViewById(R.id.taskDescription);
            submitButton = findViewById(R.id.submitButton);
            loadingIndicator = findViewById(R.id.loadingIndicator);

            // Question containers
            question1Text = findViewById(R.id.question1Text);
            question2Text = findViewById(R.id.question2Text);
            question3Text = findViewById(R.id.question3Text);
            question1Options = findViewById(R.id.question1Options);
            question2Options = findViewById(R.id.question2Options);
            question3Options = findViewById(R.id.question3Options);

            // Set task details
            taskTitle.setText(title);
            taskDescription.setText(description);

            // Set up submit button
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitQuiz();
                }
            });

            // Load quiz questions
            loadQuizQuestions();
        }
    }

    private void loadQuizQuestions() {
        loadingIndicator.setVisibility(View.VISIBLE);
        submitButton.setEnabled(false);

        // Hide question containers while loading
        findViewById(R.id.question1Container).setVisibility(View.GONE);
        findViewById(R.id.question2Container).setVisibility(View.GONE);
        findViewById(R.id.question3Container).setVisibility(View.GONE);

        // Fetch quiz questions from API
        ApiClient.getInstance(this).getQuizQuestions(topic, new ApiClient.QuizCallback() {
            @Override
            public void onSuccess(List<QuizQuestion> questions) {
                loadingIndicator.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                quizQuestions = questions;
                displayQuizQuestions();
            }

            @Override
            public void onError(String message) {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(QuizActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayQuizQuestions() {
        if (quizQuestions == null || quizQuestions.size() < 3) {
            Toast.makeText(this, "Error: Not enough quiz questions", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show question containers
        findViewById(R.id.question1Container).setVisibility(View.VISIBLE);
        findViewById(R.id.question2Container).setVisibility(View.VISIBLE);
        findViewById(R.id.question3Container).setVisibility(View.VISIBLE);

        // Question 1
        QuizQuestion q1 = quizQuestions.get(0);
        question1Text.setText("1. " + q1.getQuestion());
        setRadioButtonTexts(question1Options, q1.getOptions());

        // Question 2
        QuizQuestion q2 = quizQuestions.get(1);
        question2Text.setText("2. " + q2.getQuestion());
        setRadioButtonTexts(question2Options, q2.getOptions());

        // Question 3
        QuizQuestion q3 = quizQuestions.get(2);
        question3Text.setText("3. " + q3.getQuestion());
        setRadioButtonTexts(question3Options, q3.getOptions());
    }

    private void setRadioButtonTexts(RadioGroup group, List<String> options) {
        if (options.size() >= 4) {
            ((RadioButton) group.getChildAt(0)).setText("A: " + options.get(0));
            ((RadioButton) group.getChildAt(1)).setText("B: " + options.get(1));
            ((RadioButton) group.getChildAt(2)).setText("C: " + options.get(2));
            ((RadioButton) group.getChildAt(3)).setText("D: " + options.get(3));
        }
    }

    private void submitQuiz() {
        // Check if all questions are answered
        if (question1Options.getCheckedRadioButtonId() == -1 ||
                question2Options.getCheckedRadioButtonId() == -1 ||
                question3Options.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected answers
        String answer1 = getSelectedAnswer(question1Options);
        String answer2 = getSelectedAnswer(question2Options);
        String answer3 = getSelectedAnswer(question3Options);

        // Set user answers
        quizQuestions.get(0).setUserAnswer(answer1);
        quizQuestions.get(1).setUserAnswer(answer2);
        quizQuestions.get(2).setUserAnswer(answer3);

        // Mark task as completed
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Task task = taskDao.getTaskById(taskId);
                if (task != null) {
                    task.setCompleted(true);
                    taskDao.update(task);
                }
            }
        });

        // Navigate to results screen
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("QUESTION_1", quizQuestions.get(0).getQuestion());
        intent.putExtra("QUESTION_2", quizQuestions.get(1).getQuestion());
        intent.putExtra("QUESTION_3", quizQuestions.get(2).getQuestion());
        intent.putExtra("CORRECT_1", quizQuestions.get(0).isCorrect());
        intent.putExtra("CORRECT_2", quizQuestions.get(1).isCorrect());
        intent.putExtra("CORRECT_3", quizQuestions.get(2).isCorrect());
        startActivity(intent);
        finish();
    }

    private String getSelectedAnswer(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        RadioButton selectedButton = findViewById(selectedId);
        String text = selectedButton.getText().toString();
        // Extract the letter (A, B, C, D) from the text
        return text.substring(0, 1);
    }
}
