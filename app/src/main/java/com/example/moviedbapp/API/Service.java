package com.example.moviedbapp.API;

import com.example.moviedbapp.Model.MoviesResponse;
import com.example.moviedbapp.Model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/upcoming")
    Call<MoviesResponse> getUpcomingMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("tv/on_the_air")
    Call<MoviesResponse> getOnTheAirTV(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("tv/popular")
    Call<MoviesResponse> getPopularTV(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("tv/top_rated")
    Call<MoviesResponse> getTopRatedTV(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("person/popular")
    Call<MoviesResponse> getPopularCelebrity(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/{movie_id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path("movie_id") int id, @Query("api_key") String apiKey);
}
