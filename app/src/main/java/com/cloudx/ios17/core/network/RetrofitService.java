package com.cloudx.ios17.core.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {
    private static OkHttpClient sOkHttpClient;
    private static GsonConverterFactory sGsonConverterFactory = GsonConverterFactory.create();
    private static RxJava2CallAdapterFactory sRxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create();

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        sOkHttpClient = new OkHttpClient.Builder().addInterceptor(logging).build();
    }

    public static Retrofit getInstance(String url) {
        return new Retrofit.Builder().baseUrl(url).client(sOkHttpClient)
                .addCallAdapterFactory(sRxJava2CallAdapterFactory).addConverterFactory(sGsonConverterFactory).build();
    }
}
