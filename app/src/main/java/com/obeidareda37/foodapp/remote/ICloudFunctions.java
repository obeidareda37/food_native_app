package com.obeidareda37.foodapp.remote;

import com.squareup.okhttp.ResponseBody;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICloudFunctions {
    @GET("")
    Observable<ResponseBody> getCustomToken (@Query("access_token") String accessToken);
}
