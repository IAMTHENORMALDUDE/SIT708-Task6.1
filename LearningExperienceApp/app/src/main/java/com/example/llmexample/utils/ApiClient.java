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
import com.example.llmexample.models.Purchase;
import com.example.llmexample.models.QuizHistory;
import com.example.llmexample.models.QuizQuestion;
import com.example.llmexample.models.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Callbacks
    public interface QuizCallback {
        void onSuccess(List<QuizQuestion> questions);
        void onError(String message);
    }

    public interface UserCallback {
        void onSuccess(JSONObject user);
        void onError(String message);
    }

    public interface QuizHistoryCallback {
        void onSuccess(List<QuizHistory> quizHistoryList);
        void onError(String message);
    }

    public interface PurchaseCallback {
        void onSuccess(Purchase purchase);
        void onError(String message);
    }

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onError(String message);
    }

    public interface GenericCallback {
        void onSuccess(JSONObject response);
        void onError(String message);
    }

    // Quiz methods
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

    // User authentication methods
    public void registerUser(String username, String password, final UserCallback callback) {
        String url = BASE_URL + "/api/users/register";
        
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    public void loginUser(String username, String password, final UserCallback callback) {
        String url = BASE_URL + "/api/users/login";
        
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", username);
            requestBody.put("password", password);
        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    // Quiz history methods
    public void getUserQuizHistory(String userId, final QuizHistoryCallback callback) {
        String url = BASE_URL + "/api/quiz-history/user/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<QuizHistory> historyList = new ArrayList<>();
                            JSONArray historyArray = response.getJSONArray("history");
                            
                            for (int i = 0; i < historyArray.length(); i++) {
                                JSONObject historyObj = historyArray.getJSONObject(i);
                                QuizHistory history = new QuizHistory(
                                        historyObj.getString("_id"),
                                        historyObj.getString("userId"),
                                        historyObj.getString("topic"),
                                        historyObj.getInt("score"),
                                        historyObj.getInt("totalQuestions"),
                                        historyObj.getString("createdAt")
                                );
                                historyList.add(history);
                            }
                            
                            callback.onSuccess(historyList);
                        } catch (JSONException e) {
                            callback.onError("Error parsing quiz history: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    // Purchase methods
    public void createPurchase(String userId, String packageType, String paymentMethod, 
                              double amount, final PurchaseCallback callback) {
        String url = BASE_URL + "/api/purchases";
        
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", userId);
            requestBody.put("packageType", packageType);
            requestBody.put("paymentMethod", paymentMethod);
            requestBody.put("amount", amount);
        } catch (JSONException e) {
            callback.onError("Error creating request: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Purchase purchase = new Purchase(
                                    response.getString("_id"),
                                    response.getString("userId"),
                                    response.getString("packageType"),
                                    response.getString("paymentMethod"),
                                    response.getDouble("amount"),
                                    response.getString("createdAt")
                            );
                            callback.onSuccess(purchase);
                        } catch (JSONException e) {
                            callback.onError("Error parsing purchase response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    public void getUserPurchases(String userId, final PurchaseCallback callback) {
        String url = BASE_URL + "/api/purchases/user/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray purchasesArray = response.getJSONArray("purchases");
                            if (purchasesArray.length() > 0) {
                                JSONObject latestPurchase = purchasesArray.getJSONObject(0);
                                Purchase purchase = new Purchase(
                                        latestPurchase.getString("_id"),
                                        latestPurchase.getString("userId"),
                                        latestPurchase.getString("packageType"),
                                        latestPurchase.getString("paymentMethod"),
                                        latestPurchase.getDouble("amount"),
                                        latestPurchase.getString("createdAt")
                                );
                                callback.onSuccess(purchase);
                            } else {
                                callback.onError("No purchases found");
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing purchases: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    // Profile methods
    public void getUserProfile(String userId, final ProfileCallback callback) {
        String url = BASE_URL + "/api/users/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject userObj = response.getJSONObject("user");
                            UserProfile profile = new UserProfile(
                                    userObj.getString("_id"),
                                    userObj.getString("username"),
                                    userObj.optInt("totalQuestions", 0),
                                    userObj.optInt("correctAnswers", 0),
                                    userObj.optInt("incorrectAnswers", 0)
                            );
                            callback.onSuccess(profile);
                        } catch (JSONException e) {
                            callback.onError("Error parsing profile: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    // Sharing methods
    public void getShareableProfile(String userId, final GenericCallback callback) {
        String url = BASE_URL + "/api/users/" + userId + "/share";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleError(error, callback);
                    }
                }
        );

        requestQueue.add(request);
    }

    // Helper method to handle errors
    private <T> void handleError(VolleyError error, T callback) {
        String errorMsg = "Unknown error";
        if (error.networkResponse != null) {
            errorMsg = "HTTP " + error.networkResponse.statusCode + ": " + new String(error.networkResponse.data);
        } else if (error.getMessage() != null) {
            errorMsg = error.getMessage();
        }
        Log.e(TAG, "API Error: " + errorMsg, error);
        
        if (callback instanceof UserCallback) {
            ((UserCallback) callback).onError(errorMsg);
        } else if (callback instanceof QuizHistoryCallback) {
            ((QuizHistoryCallback) callback).onError(errorMsg);
        } else if (callback instanceof PurchaseCallback) {
            ((PurchaseCallback) callback).onError(errorMsg);
        } else if (callback instanceof ProfileCallback) {
            ((ProfileCallback) callback).onError(errorMsg);
        } else if (callback instanceof GenericCallback) {
            ((GenericCallback) callback).onError(errorMsg);
        } else if (callback instanceof QuizCallback) {
            ((QuizCallback) callback).onError(errorMsg);
        }
    }
}
