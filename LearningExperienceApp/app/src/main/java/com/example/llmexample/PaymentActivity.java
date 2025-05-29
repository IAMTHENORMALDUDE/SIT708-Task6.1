package com.example.llmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.models.Purchase;
import com.example.llmexample.utils.ApiClient;
import com.example.llmexample.utils.SessionManager;

public class PaymentActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView packageNameText, packageDescriptionText, packagePriceText;
    private LinearLayout googlePayContainer, creditCardContainer, paypalContainer;
    private RadioButton googlePayRadioButton, creditCardRadioButton, paypalRadioButton;
    private Button completePaymentButton;
    private ProgressBar loadingIndicator;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    private String packageType;
    private double packagePrice;
    private String selectedPaymentMethod = "Google Pay"; // Default payment method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get package information from intent
        packageType = getIntent().getStringExtra("PACKAGE_TYPE");
        packagePrice = getIntent().getDoubleExtra("PACKAGE_PRICE", 0.0);

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        packageNameText = findViewById(R.id.packageNameText);
        packageDescriptionText = findViewById(R.id.packageDescriptionText);
        packagePriceText = findViewById(R.id.packagePriceText);
        googlePayContainer = findViewById(R.id.googlePayContainer);
        creditCardContainer = findViewById(R.id.creditCardContainer);
        paypalContainer = findViewById(R.id.paypalContainer);
        googlePayRadioButton = findViewById(R.id.googlePayRadioButton);
        creditCardRadioButton = findViewById(R.id.creditCardRadioButton);
        paypalRadioButton = findViewById(R.id.paypalRadioButton);
        completePaymentButton = findViewById(R.id.completePaymentButton);
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

        // Set package information
        packageNameText.setText(packageType);
        packageDescriptionText.setText(getPackageDescription(packageType));
        packagePriceText.setText(String.format("$%.2f / month", packagePrice));

        // Set up click listeners
        backButton.setOnClickListener(v -> onBackPressed());
        
        googlePayContainer.setOnClickListener(v -> selectPaymentMethod("Google Pay"));
        creditCardContainer.setOnClickListener(v -> selectPaymentMethod("Credit Card"));
        paypalContainer.setOnClickListener(v -> selectPaymentMethod("PayPal"));
        
        completePaymentButton.setOnClickListener(v -> processPayment());
    }

    private String getPackageDescription(String packageType) {
        switch (packageType) {
            case "Starter":
                return "Basic personalized quizzes, access to history feature, 5 quizzes per day";
            case "Intermediate":
                return "Enhanced personalization, detailed analytics, 20 quizzes per day, priority support";
            case "Advanced":
                return "Premium AI-driven quizzes, unlimited quizzes, advanced analytics, custom learning paths";
            default:
                return "Improved Quiz generation";
        }
    }

    private void selectPaymentMethod(String paymentMethod) {
        // Update radio buttons
        googlePayRadioButton.setChecked(paymentMethod.equals("Google Pay"));
        creditCardRadioButton.setChecked(paymentMethod.equals("Credit Card"));
        paypalRadioButton.setChecked(paymentMethod.equals("PayPal"));
        
        // Update selected payment method
        selectedPaymentMethod = paymentMethod;
    }

    private void processPayment() {
        loadingIndicator.setVisibility(View.VISIBLE);
        String userId = sessionManager.getUsername(); // In a real app, this would be the user ID

        apiClient.createPurchase(userId, packageType, selectedPaymentMethod, packagePrice, 
                new ApiClient.PurchaseCallback() {
                    @Override
                    public void onSuccess(Purchase purchase) {
                        loadingIndicator.setVisibility(View.GONE);
                        showPaymentSuccessDialog(purchase);
                    }

                    @Override
                    public void onError(String message) {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(PaymentActivity.this, 
                                "Payment error: " + message, Toast.LENGTH_SHORT).show();
                        
                        // For demo purposes, show success dialog anyway
                        showPaymentSuccessDialog(new Purchase(
                                "1",
                                userId,
                                packageType,
                                selectedPaymentMethod,
                                packagePrice,
                                "2023-05-27T12:00:00Z"
                        ));
                    }
                });
    }

    private void showPaymentSuccessDialog(Purchase purchase) {
        new AlertDialog.Builder(this)
                .setTitle("Payment Successful")
                .setMessage("Thank you for upgrading to " + purchase.getPackageType() + 
                        "! Your account has been upgraded and you now have access to all the features.")
                .setPositiveButton("Go to Dashboard", (dialog, which) -> {
                    Intent intent = new Intent(PaymentActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
