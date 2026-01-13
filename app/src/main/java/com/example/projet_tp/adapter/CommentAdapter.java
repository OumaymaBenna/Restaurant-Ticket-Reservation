package com.example.projet_tp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.model.StudentComment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<StudentComment> comments;

    public CommentAdapter(List<StudentComment> comments) {
        this.comments = comments;
    }

    public void updateComments(List<StudentComment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        StudentComment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUserName;
        private TextView textViewDate;
        private TextView textViewComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewComment = itemView.findViewById(R.id.textViewComment);
        }

        public void bind(StudentComment comment) {
            textViewUserName.setText(comment.getUserName() != null ? comment.getUserName() : "Étudiant");
            textViewComment.setText(comment.getComment() != null ? comment.getComment() : "");

            if (comment.getCreatedAt() != null && !comment.getCreatedAt().isEmpty()) {
                try {
                    String dateStr = comment.getCreatedAt();
                    SimpleDateFormat inputFormat;
                    
                    if (dateStr.contains("T")) {
                        if (dateStr.contains(".")) {
                            inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                        } else {
                            inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        }
                    } else {
                        inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    }
                    
                    Date date = inputFormat.parse(dateStr);
                    if (date != null) {
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        textViewDate.setText(outputFormat.format(date));
                    } else {
                        textViewDate.setText(dateStr);
                    }
                } catch (ParseException e) {
                    textViewDate.setText(comment.getCreatedAt());
                }
            } else {
                textViewDate.setText("");
            }
        }
    }
}


