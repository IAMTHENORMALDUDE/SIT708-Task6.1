package com.example.llmexample.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.llmexample.models.QuizQuestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2:4000"; // For emulator
    private static ApiClient instance;
    private RequestQueue requestQueue;

    private ApiClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public interface QuizCallback {
        void onSuccess(List<QuizQuestion> questions);
        void onError(String message);
    }

    public void getQuizQuestions(String topic, final QuizCallback callback) {
        String url = BASE_URL + "/getQuiz?topic=" + topic;
        Log.d(TAG, "Fetching quiz from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<QuizQuestion> questions = parseQuizResponse(response);
                            callback.onSuccess(questions);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage(), e);
                            callback.onError("Failed to parse quiz data: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = "Unknown error";
                        if (error.networkResponse != null) {
                            errorMsg = "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data);
                        } else if (error.getMessage() != null) {
                            errorMsg = error.getMessage();
                        }
                        Log.e(TAG, "Error fetching quiz: " + errorMsg, error);
                        callback.onError("Failed to fetch quiz: " + errorMsg);
                    }
                }
        );

        // Set a longer timeout for the LLM API which might take time to generate content
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private List<QuizQuestion> parseQuizResponse(JSONObject response) throws JSONException {
        List<QuizQuestion> questions = new ArrayList<>();
        JSONArray quizArray = response.getJSONArray("quiz");

        for (int i = 0; i < quizArray.length(); i++) {
            JSONObject questionObj = quizArray.getJSONObject(i);
            String questionText = questionObj.getString("question");
            JSONArray optionsArray = questionObj.getJSONArray("options");
            String correctAnswer = questionObj.getString("correct_answer");

            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                options.add(optionsArray.getString(j));
            }

            questions.add(new QuizQuestion(questionText, options, correctAnswer));
        }

        return questions;
    }
}
