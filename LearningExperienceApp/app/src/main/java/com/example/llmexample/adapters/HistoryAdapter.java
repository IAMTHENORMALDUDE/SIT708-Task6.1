package com.example.llmexample.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.llmexample.R;
import com.example.llmexample.models.QuizHistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<QuizHistory> historyList;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public HistoryAdapter(List<QuizHistory> historyList) {
        this.historyList = historyList;
        
        // Initialize date formatters
        inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        outputFormat.setTimeZone(TimeZone.getDefault());
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        QuizHistory history = historyList.get(position);
        
        // Set question number and topic
        holder.questionNumberText.setText((position + 1) + ". Question " + (position + 1));
        holder.questionTopicText.setText("Topic: " + history.getTopic());
        
        // Set score
        holder.scoreText.setText("Score: " + history.getScore() + "/" + history.getTotalQuestions());
        
        // Format and set date
        String formattedDate = formatDate(history.getCreatedAt());
        holder.dateText.setText("Date: " + formattedDate);
        
        // Set result icon based on pass/fail
        if (history.isPassed()) {
            holder.resultIcon.setImageResource(android.R.drawable.presence_online);
            holder.resultIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
        } else {
            holder.resultIcon.setImageResource(android.R.drawable.presence_busy);
            holder.resultIcon.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
        }
        
        // Set up dummy answers for demo purposes
        setupDummyAnswers(holder, history);
        
        // Set up expand/collapse functionality
        holder.expandButton.setOnClickListener(v -> {
            if (holder.answersContainer.getVisibility() == View.VISIBLE) {
                holder.answersContainer.setVisibility(View.GONE);
                holder.expandButton.setImageResource(android.R.drawable.arrow_down_float);
            } else {
                holder.answersContainer.setVisibility(View.VISIBLE);
                holder.expandButton.setImageResource(android.R.drawable.arrow_up_float);
            }
        });
    }

    private void setupDummyAnswers(HistoryViewHolder holder, QuizHistory history) {
        // In a real app, these would come from the API
        if (history.getScore() == history.getTotalQuestions()) {
            // All correct
            holder.answer1Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            holder.answer1Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.answer1Text.setText("Answer 1 - Correct");
            
            holder.answer2Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            holder.answer2Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.answer2Text.setText("Answer 2 - Correct");
            
            holder.answer3Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            holder.answer3Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.answer3Text.setText("Answer 3 - Correct");
        } else if (history.getScore() == 0) {
            // All incorrect
            holder.answer1Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_busy, 0, 0, 0);
            holder.answer1Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.answer1Text.setText("Answer 1 - Incorrect");
            
            holder.answer2Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_busy, 0, 0, 0);
            holder.answer2Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.answer2Text.setText("Answer 2 - Incorrect");
            
            holder.answer3Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_busy, 0, 0, 0);
            holder.answer3Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.answer3Text.setText("Answer 3 - Incorrect");
        } else {
            // Mixed results
            holder.answer1Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            holder.answer1Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.answer1Text.setText("Answer 1 - Correct");
            
            holder.answer2Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_busy, 0, 0, 0);
            holder.answer2Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            holder.answer2Text.setText("Answer 2 - Incorrect");
            
            holder.answer3Text.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
            holder.answer3Text.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.answer3Text.setText("Answer 3 - Correct");
        }
    }

    private String formatDate(String dateString) {
        try {
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original string if parsing fails
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumberText, questionTopicText, scoreText, dateText;
        TextView answer1Text, answer2Text, answer3Text;
        ImageView resultIcon;
        ImageButton expandButton;
        LinearLayout answersContainer;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumberText = itemView.findViewById(R.id.questionNumberText);
            questionTopicText = itemView.findViewById(R.id.questionTopicText);
            scoreText = itemView.findViewById(R.id.scoreText);
            dateText = itemView.findViewById(R.id.dateText);
            resultIcon = itemView.findViewById(R.id.resultIcon);
            expandButton = itemView.findViewById(R.id.expandButton);
            answersContainer = itemView.findViewById(R.id.answersContainer);
            answer1Text = itemView.findViewById(R.id.answer1Text);
            answer2Text = itemView.findViewById(R.id.answer2Text);
            answer3Text = itemView.findViewById(R.id.answer3Text);
        }
    }
}
