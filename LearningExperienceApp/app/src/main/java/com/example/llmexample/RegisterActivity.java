package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.models.AppDatabase;
import com.example.llmexample.models.User;
import com.example.llmexample.models.UserDao;
import com.example.llmexample.utils.SessionManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, confirmEmailInput, passwordInput, confirmPasswordInput, phoneInput;
    private Button createAccountButton;
    private AppDatabase database;
    private UserDao userDao;
    private SessionManager sessionManager;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database and DAO
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        sessionManager = new SessionManager(this);
        executor = Executors.newSingleThreadExecutor();

        // Initialize UI components
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        confirmEmailInput = findViewById(R.id.confirmEmailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        phoneInput = findViewById(R.id.phoneInput);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Set up create account button click listener
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        // Get input values
        final String username = usernameInput.getText().toString().trim();
        final String email = emailInput.getText().toString().trim();
        final String confirmEmail = confirmEmailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();
        final String confirmPassword = confirmPasswordInput.getText().toString().trim();
        final String phone = phoneInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (!email.equals(confirmEmail)) {
            confirmEmailInput.setError("Emails do not match");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            return;
        }

        // Create user and save to database
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Check if username already exists
                final User existingUser = userDao.getUserByUsername(username);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (existingUser != null) {
                            usernameInput.setError("Username already exists");
                        } else {
                            // Create new user
                            User newUser = new User(username, password, email, phone);

                            // Insert user in background thread
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    userDao.insert(newUser);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            sessionManager.createLoginSession(username);
                                            Intent intent = new Intent(RegisterActivity.this, InterestsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
