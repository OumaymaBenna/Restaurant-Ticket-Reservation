package com.example.projet_tp.network;

import com.example.projet_tp.model.Menu;
import com.example.projet_tp.model.Reservation;
import com.example.projet_tp.model.StudentComment;
import com.example.projet_tp.model.User;
import com.example.projet_tp.model.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/register")
    Call<User> registerUser(@Body User user);

    @POST("/login")
    Call<UserResponse> login(@Body User user);

    @GET("/menus")
    Call<com.example.projet_tp.model.MenuResponse> getMenus();

    @GET("/meal-reservations/user/{userId}")
    Call<List<Reservation>> getUserReservations(@Path("userId") String userId);

    @POST("/meal-reservations")
    Call<Reservation> createReservation(@Body Reservation reservation);



    @GET("/meal-reservations/cold")
    Call<List<Reservation>> getColdMealReservations();


    @POST("/cold-meal-reservations")
    Call<Reservation> createColdReservation(@Body Reservation reservation);

    @POST("/menus")
    Call<Menu> createMenu(@Body Menu menu);

    @retrofit2.http.PUT("/menus/{id}")
    Call<com.example.projet_tp.model.ApiResponse> updateMenu(@retrofit2.http.Path("id") String id, @Body Menu menu);

    @retrofit2.http.DELETE("/menus/{id}")
    Call<com.example.projet_tp.model.ApiResponse> deleteMenu(@retrofit2.http.Path("id") String id);

    // Routes pour les commentaires des étudiants
    @POST("/menus/{menuId}/comments")
    Call<com.example.projet_tp.model.ApiResponse> createComment(@Path("menuId") String menuId, @Body StudentComment comment);

    @GET("/menus/{menuId}/comments")
    Call<com.example.projet_tp.model.CommentResponse> getComments(@Path("menuId") String menuId);

    @GET("/comments")
    Call<com.example.projet_tp.model.CommentResponse> getAllComments();

    @retrofit2.http.DELETE("/comments/{id}")
    Call<com.example.projet_tp.model.ApiResponse> deleteComment(@retrofit2.http.Path("id") String id);

    // Route pour créer une commande avec commentaire
    @POST("/orders/comment")
    Call<com.example.projet_tp.model.ApiResponse> createOrderWithComment(@Body com.example.projet_tp.model.OrderComment orderComment);

    // Route pour récupérer toutes les commandes avec commentaires (admin)
    @GET("/orders/comments")
    Call<com.example.projet_tp.model.OrderCommentResponse> getAllOrderComments();
}