package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.models.AppDatabase;
import com.example.llmexample.models.User;
import com.example.llmexample.models.UserDao;
import com.example.llmexample.utils.SessionManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private AppDatabase database;
    private UserDao userDao;
    private SessionManager sessionManager;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database and DAO
        database = AppDatabase.getInstance(this);
        userDao = database.userDao();
        sessionManager = new SessionManager(this);
        executor = Executors.newSingleThreadExecutor();

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerLink = findViewById(R.id.registerLink);

        // Set up login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(username)) {
                    usernameInput.setError("Username is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordInput.setError("Password is required");
                    return;
                }

                // Attempt login
                loginUser(username, password);
            }
        });

        // Set up register link click listener
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser(final String username, final String password) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final User user = userDao.login(username, password);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null) {
                            // Login successful
                            sessionManager.createLoginSession(username);
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // Login failed
                            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
