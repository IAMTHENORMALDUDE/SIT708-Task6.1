package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.utils.ApiClient;
import com.example.llmexample.utils.SessionManager;

public class UpgradeActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button starterPurchaseButton, intermediatePurchaseButton, advancedPurchaseButton;
    private ProgressBar loadingIndicator;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    // Package prices
    private static final double STARTER_PRICE = 4.99;
    private static final double INTERMEDIATE_PRICE = 9.99;
    private static final double ADVANCED_PRICE = 19.99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        starterPurchaseButton = findViewById(R.id.starterPurchaseButton);
        intermediatePurchaseButton = findViewById(R.id.intermediatePurchaseButton);
        advancedPurchaseButton = findViewById(R.id.advancedPurchaseButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);

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
        
        starterPurchaseButton.setOnClickListener(v -> 
                startPaymentActivity("Starter", STARTER_PRICE));
        
        intermediatePurchaseButton.setOnClickListener(v -> 
                startPaymentActivity("Intermediate", INTERMEDIATE_PRICE));
        
        advancedPurchaseButton.setOnClickListener(v -> 
                startPaymentActivity("Advanced", ADVANCED_PRICE));
    }

    private void startPaymentActivity(String packageType, double price) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("PACKAGE_TYPE", packageType);
        intent.putExtra("PACKAGE_PRICE", price);
        startActivity(intent);
    }
}
