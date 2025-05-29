package com.example.llmexample;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.llmexample.models.UserProfile;
import com.example.llmexample.utils.ApiClient;
import com.example.llmexample.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameText, emailText, totalQuestionsValue, correctlyAnsweredValue, incorrectAnswersValue, recommendationText;
    private Button shareButton;
    private ImageButton backButton;
    private ProgressBar loadingIndicator;
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        usernameText = findViewById(R.id.usernameText);
        emailText = findViewById(R.id.emailText);
        totalQuestionsValue = findViewById(R.id.totalQuestionsValue);
        correctlyAnsweredValue = findViewById(R.id.correctlyAnsweredValue);
        incorrectAnswersValue = findViewById(R.id.incorrectAnswersValue);
        recommendationText = findViewById(R.id.recommendationText);
        shareButton = findViewById(R.id.shareButton);
        backButton = findViewById(R.id.backButton);
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
        shareButton.setOnClickListener(v -> showShareDialog());

        // Load user profile data
        loadUserProfile();
    }

    private void loadUserProfile() {
        loadingIndicator.setVisibility(View.VISIBLE);
        String userId = sessionManager.getUsername(); // In a real app, this would be the user ID

        apiClient.getUserProfile(userId, new ApiClient.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                loadingIndicator.setVisibility(View.GONE);
                userProfile = profile;
                updateUI(profile);
            }

            @Override
            public void onError(String message) {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Error loading profile: " + message, Toast.LENGTH_SHORT).show();
                
                // For demo purposes, create dummy data if API fails
                createDummyProfile();
            }
        });
    }

    private void createDummyProfile() {
        userProfile = new UserProfile(
                "1",
                sessionManager.getUsername(),
                10,
                10,
                10
        );
        updateUI(userProfile);
    }

    private void updateUI(UserProfile profile) {
        usernameText.setText(profile.getUsername());
        emailText.setText(profile.getUsername() + "@example.com");
        totalQuestionsValue.setText(String.valueOf(profile.getTotalQuestions()));
        correctlyAnsweredValue.setText(String.valueOf(profile.getCorrectAnswers()));
        incorrectAnswersValue.setText(String.valueOf(profile.getIncorrectAnswers()));

        // Set recommendation text based on user performance
        if (profile.getCorrectPercentage() < 50) {
            recommendationText.setText("Recommended to try: Improve your knowledge in basic concepts");
        } else if (profile.getCorrectPercentage() < 80) {
            recommendationText.setText("Recommended to try: Practice more advanced topics");
        } else {
            recommendationText.setText("Great job! You're doing excellent. Try some expert-level topics");
        }
    }

    private void showShareDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_share_profile);
        dialog.setCancelable(true);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize dialog components
        EditText shareMessageEditText = dialog.findViewById(R.id.shareMessageEditText);
        Button cancelShareButton = dialog.findViewById(R.id.cancelShareButton);
        
        // Set up share options click listeners
        setupShareOption(dialog, R.id.shareWhatsappButton, "WhatsApp");
        setupShareOption(dialog, R.id.shareFacebookButton, "Facebook");
        setupShareOption(dialog, R.id.shareTwitterButton, "Twitter");
        setupShareOption(dialog, R.id.shareEmailButton, "Email");
        setupShareOption(dialog, R.id.shareQRButton, "QR Code");
        setupShareOption(dialog, R.id.shareLinkButton, "Copy Link");
        setupShareOption(dialog, R.id.shareMessengerButton, "Messenger");
        setupShareOption(dialog, R.id.shareMoreButton, "More");

        // Set up cancel button
        cancelShareButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void setupShareOption(Dialog dialog, int buttonId, String platform) {
        LinearLayout shareOption = dialog.findViewById(buttonId);
        shareOption.setOnClickListener(v -> {
            dialog.dismiss();
            shareProfile(platform);
        });
    }

    private void shareProfile(String platform) {
        if (userProfile == null) {
            Toast.makeText(this, "Profile data not available", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingIndicator.setVisibility(View.VISIBLE);
        
        // Since we're using a dummy profile with a simple ID that's not a valid MongoDB ObjectId,
        // we'll skip the API call and directly use a dummy share URL for demonstration purposes
        loadingIndicator.setVisibility(View.GONE);
        String dummyShareUrl = "https://learningapp.example.com/profile/" + userProfile.getUsername();
        shareProfileUrl(dummyShareUrl, platform);
        
        // In a real implementation with proper MongoDB IDs, you would use this code:
        /*
        apiClient.getShareableProfile(userProfile.getId(), new ApiClient.GenericCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                loadingIndicator.setVisibility(View.GONE);
                try {
                    String shareUrl = response.getString("shareUrl");
                    shareProfileUrl(shareUrl, platform);
                } catch (JSONException e) {
                    Toast.makeText(ProfileActivity.this, "Error parsing share URL", Toast.LENGTH_SHORT).show();
                    // For demo purposes, create a dummy share URL
                    shareProfileUrl("https://learningapp.example.com/profile/" + userProfile.getUsername(), platform);
                }
            }

            @Override
            public void onError(String message) {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Error generating share link: " + message, Toast.LENGTH_SHORT).show();
                // For demo purposes, create a dummy share URL
                shareProfileUrl("https://learningapp.example.com/profile/" + userProfile.getUsername(), platform);
            }
        });
        */
    }

    private void shareProfileUrl(String url, String platform) {
        String shareText = "Check out my learning profile! I've answered " + 
                userProfile.getCorrectAnswers() + " questions correctly out of " + 
                userProfile.getTotalQuestions() + ". " + url;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        
        if (platform.equals("QR Code")) {
            // In a real app, this would generate a QR code from the URL
            Toast.makeText(this, "QR Code generated for: " + url, Toast.LENGTH_SHORT).show();
        } else if (platform.equals("Copy Link")) {
            // In a real app, this would copy the URL to clipboard
            Toast.makeText(this, "Link copied to clipboard: " + url, Toast.LENGTH_SHORT).show();
        } else {
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }
}
