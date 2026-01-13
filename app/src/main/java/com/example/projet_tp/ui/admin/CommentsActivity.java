package com.example.projet_tp.ui.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projet_tp.R;
import com.example.projet_tp.adapter.CommentAdapter;
import com.example.projet_tp.api.RetrofitClient;
import com.example.projet_tp.model.CommentResponse;
import com.example.projet_tp.model.StudentComment;
import com.example.projet_tp.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewComments;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private CommentAdapter commentAdapter;
    private List<StudentComment> commentList;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        apiService = RetrofitClient.getApiService();
        commentList = new ArrayList<>();

        setupToolbar();
        initViews();
        setupRecyclerView();
        loadComments();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Commentaires des étudiants");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void initViews() {
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }

    private void setupRecyclerView() {
        if (recyclerViewComments != null) {
            commentAdapter = new CommentAdapter(commentList);
            recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewComments.setAdapter(commentAdapter);
        }
    }

    private void loadComments() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (textViewEmpty != null) {
            textViewEmpty.setVisibility(View.GONE);
        }
        if (recyclerViewComments != null) {
            recyclerViewComments.setVisibility(View.GONE);
        }

        if (apiService == null) {
            showError("Erreur de connexion");
            return;
        }

        apiService.getAllComments().enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    CommentResponse commentResponse = response.body();
                    if (commentResponse.getComments() != null && !commentResponse.getComments().isEmpty()) {
                        commentList.clear();
                        commentList.addAll(commentResponse.getComments());
                        if (commentAdapter != null) {
                            commentAdapter.updateComments(commentList);
                        }
                        updateEmptyState();
                    } else {
                        updateEmptyState();
                    }
                } else {
                    showError("Erreur lors du chargement des commentaires");
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("CommentsActivity", "Erreur de connexion", t);
                showError("Erreur de connexion: " + (t != null ? t.getMessage() : "Erreur inconnue"));
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (commentList.isEmpty()) {
            if (textViewEmpty != null) {
                textViewEmpty.setVisibility(View.VISIBLE);
            }
            if (recyclerViewComments != null) {
                recyclerViewComments.setVisibility(View.GONE);
            }
        } else {
            if (textViewEmpty != null) {
                textViewEmpty.setVisibility(View.GONE);
            }
            if (recyclerViewComments != null) {
                recyclerViewComments.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComments();
    }
}

