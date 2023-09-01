package com.cloudx.ios17.core.network.duckduckgo;

import io.reactivex.Observable;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DuckDuckGoSuggestionService {
    @GET("/ac/")
    Observable<List<DuckDuckGoResult>> query(@Query("q") String query);
}
