package com.example.llmexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listView;
    private LinearLayout loadingContainer;
    private ArrayList<String> quizItems;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        listView = findViewById(R.id.listView);
        loadingContainer = findViewById(R.id.loadingContainer);
        quizItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, quizItems);
        listView.setAdapter(adapter);

        // URL for the Flask server
        String url = "http://10.0.2.2:4000/getQuiz?topic=Movies";
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the JSON Object Request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Hide loading container when response is received
                        loadingContainer.setVisibility(View.GONE);

                        try {
                            Log.i(TAG, "Response received: " + response.toString());
                            JSONArray quizArray = response.getJSONArray("quiz");

                            // Clear previous quiz items
                            quizItems.clear();

                            // Process each quiz question
                            for (int i = 0; i < quizArray.length(); i++) {
                                JSONObject quizQuestion = quizArray.getJSONObject(i);

                                String question = quizQuestion.getString("question");
                                JSONArray optionsArray = quizQuestion.getJSONArray("options");
                                String correctAnswer = quizQuestion.getString("correct_answer");

                                // Build the display string for this question
                                StringBuilder questionText = new StringBuilder();
                                questionText.append("Q").append(i + 1).append(": ").append(question).append("\n");
                                for (int j = 0; j < optionsArray.length(); j++) {
                                    char optionLetter = (char) ('A' + j);
                                    questionText.append(optionLetter).append(": ")
                                            .append(optionsArray.getString(j)).append("\n");
                                }
                                questionText.append("Correct Answer: ").append(correctAnswer);

                                // Add to the list
                                quizItems.add(questionText.toString());
                            }

                            // Update the ListView
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Quiz loaded successfully!", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
                            Toast.makeText(MainActivity.this, "Error parsing JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hide loading container on error
                        loadingContainer.setVisibility(View.GONE);

                        String errorMsg = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMsg = "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data);
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e(TAG, "Error fetching quiz: " + errorMsg, error);
                        Toast.makeText(MainActivity.this, "Error fetching quiz: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });

        // Set a custom retry policy with a longer timeout
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10-second timeout (adjust as needed)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 1 retry
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // Default backoff multiplier
        ));

        // Show loading container before sending the request
        loadingContainer.setVisibility(View.VISIBLE);

        // Add the request to the queue
        Log.i(TAG, "Sending request to: " + url);
        queue.add(jsonObjectRequest);
    }
}