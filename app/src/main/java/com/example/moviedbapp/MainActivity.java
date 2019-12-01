package com.example.moviedbapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.moviedbapp.API.Client;
import com.example.moviedbapp.API.Service;
import com.example.moviedbapp.Adapter.MoviesAdapter;
import com.example.moviedbapp.Adapter.PaginationAdapter;
import com.example.moviedbapp.Model.Movie;
import com.example.moviedbapp.Model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity /*implements SharedPreferences.OnSharedPreferenceChangeListener*/ {

    private static final String TAG = ".MainActivity";

    PaginationAdapter adapterP;
    LinearLayoutManager linearLayoutManager;

    Button pagNo;

    RecyclerView rv;
    ProgressBar progressBar;

    private static  final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private int Total_PAGES = 100;
    private int currentPage = PAGE_START;

    private Service movieService;

    //private RecyclerView recyclerView;
    //private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog pd;
    //private SwipeRefreshLayout swipeContainer;
    public static final String LOG_TAG = MoviesAdapter.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);

        pagNo = (Button) findViewById(R.id.PageNo);

        adapterP = new PaginationAdapter(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);

        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapterP);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                },1000);
            }

            @Override
            public int getTotalPageCount() {
                return Total_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        //init service and load data
        Client client = new Client();
        movieService = client.getClient().create(Service.class);

        loadFirstPage();

        //initViews();

        /*swipeContainer = (SwipeRefreshLayout) findViewById(R.id.main_contents);
        swipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(MainActivity.this, "Movies Refreshed",Toast.LENGTH_SHORT).show();
            }
        });*/

    }

    private Call<MoviesResponse> callTopRatedMoviesApi() {
        return movieService.getTopRatedMovies(
                BuildConfig.THE_MOVIE_DB_API_TOKEN, currentPage
        );
    }

    private List<Movie> fetchResults(Response<MoviesResponse> response) {
        MoviesResponse topRatedMovies = response.body();
        return topRatedMovies.getResults();
    }

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        callTopRatedMoviesApi().enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                //Got Data. Send it to apapter

                List<Movie> results = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                adapterP.addAll(results);

                if (currentPage <= Total_PAGES) {
                    adapterP.addLoadingFooter();
                }
                else {
                    isLoading = true;

                }
                pagNo.setText(String.valueOf(currentPage));

            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callTopRatedMoviesApi().enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                adapterP.removeLoadingFooter();
                isLoading = false;

                List<Movie> results = fetchResults(response);
                adapterP.addAll(results);

                if (currentPage != Total_PAGES) adapterP.addLoadingFooter();
                else isLastPage = true;

                pagNo.setText(String.valueOf(currentPage));
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public Activity getActivity() {
        Context context = this;
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /*
    private void initViews() {
        pd = new ProgressDialog(this);
        pd.setMessage("Fetching movies...");
        pd.show();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        else {
            recyclerView.setLayoutManager(new GridLayoutManager(this,4));
        }

        adapter.notifyDataSetChanged();

        checkSortOrder();
    }

    private void loadJSON1() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Please obtain API key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getUpcomingMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, currentPage);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = null;
                    if (response.body() != null) {
                        movies = response.body().getResults();
                    }

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_SHORT).show();

                }
            });

        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJSON2() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Please obtain API key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, currentPage);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = null;
                    if (response.body() != null) {
                        movies = response.body().getResults();
                    }

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_SHORT).show();

                }
            });

        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJSON3() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Please obtain API key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, currentPage);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = null;
                    if (response.body() != null) {
                        movies = response.body().getResults();
                    }

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_SHORT).show();

                }
            });

        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJSON4() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Please obtain API key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getOnTheAirTV(BuildConfig.THE_MOVIE_DB_API_TOKEN,currentPage);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = null;
                    if (response.body() != null) {
                        movies = response.body().getResults();
                    }

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_SHORT).show();

                }
            });

        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJSON5() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Please obtain API key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getPopularTV(BuildConfig.THE_MOVIE_DB_API_TOKEN, currentPage);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = null;
                    if (response.body() != null) {
                        movies = response.body().getResults();
                    }

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_SHORT).show();

                }
            });

        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadJSON6() {
        try {
            if (BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty()) {
                Toast.makeText(getApplicationContext(),"Please obtain API key firstly from themoviedb.org", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                return;
            }
            Client client = new Client();
            Service apiService = client.getClient().create(Service.class);
            Call<MoviesResponse> call = apiService.getTopRatedTV(BuildConfig.THE_MOVIE_DB_API_TOKEN,currentPage);
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = null;
                    if (response.body() != null) {
                        movies = response.body().getResults();
                    }

                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if (swipeContainer.isRefreshing()) {
                        swipeContainer.setRefreshing(false);
                    }
                    pd.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d("Error", t.getMessage());
                    Toast.makeText(MainActivity.this, "Error Fetching Data", Toast.LENGTH_SHORT).show();

                }
            });

        }
        catch (Exception e) {
            Log.d("Error", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(LOG_TAG, "Preferences updated");
        checkSortOrder();
    }
    private void checkSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrder = preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );
        if (sortOrder.equals(this.getString(R.string.pref_upcoming))) {
            Log.d(LOG_TAG, "Sorting by Upcoming Movies");
            loadJSON1();
        }
        else if(sortOrder.equals(this.getString(R.string.pref_most_popular))){
            Log.d(LOG_TAG, "Sorting by most Popular Movies");
            loadJSON2();
        }
        else if(sortOrder.equals(this.getString(R.string.pref_highest_rated))){
            Log.d(LOG_TAG, "Sorting by Top Rated Movies");
            loadJSON3();
        }
        else if(sortOrder.equals(this.getString(R.string.pref_on_the_air))){
            Log.d(LOG_TAG, "Sorting by On The Air TV Shows");
            loadJSON4();
        }
        else if(sortOrder.equals(this.getString(R.string.pref_tv_popular))){
            Log.d(LOG_TAG, "Sorting by Popular TV Show");
            loadJSON5();
        }
        else {
            Log.d(LOG_TAG, "Sorting by Top Rated TV Show");
            loadJSON6();
        }
    }





    @Override
    public void onResume() {
        super.onResume();
        if (movieList.isEmpty()) {
            checkSortOrder();
        }
        else {

        }
    }*/
}
