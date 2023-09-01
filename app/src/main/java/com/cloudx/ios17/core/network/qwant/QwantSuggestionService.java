package com.cloudx.ios17.core.network.qwant;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QwantSuggestionService {

    @GET("/api/suggest/")
    Observable<QwantResult> query(@Query("q") String query);
}
