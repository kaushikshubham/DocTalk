package com.kaushik.doctalk.network;

import com.kaushik.doctalk.network.dataModel.Model;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BaseApiService {

    @GET("/search/users")
    Single<Model> getUsersList(@Query("q") String query, @Query("page") int pageNumber, @Query("per_page") int item_count);
}
