/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cache.DataCache;
import cache.DataCacheType;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class StringRequest extends Request<String> {
    private final Listener<String> mListener;
    private String url;
    private final Map<String, String> headers;
    private Map<String, String> params;
    private DataCacheType cacheType;

    /**
     * Creates a new request with the given method.
     *
     * @param method        the request {@link Method} to use
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public StringRequest(int method, String url, Map<String, String> headers,
                         Map<String, String> params, Listener<String> listener,
                         ErrorListener errorListener, DataCacheType cacheType) {
        super(method, url, errorListener);
        mListener = listener;
        this.url = url;
        this.headers = headers;
        this.params = params;
        this.cacheType = cacheType;
    }

    // default for POST PUT
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params != null ? params : super.getParams();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    /**
     * Creates a new GET request.
     *
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public StringRequest(String url, Listener<String> listener,
                         ErrorListener errorListener, DataCacheType cacheType) {
        this(Method.GET, url, null, null, listener, errorListener, cacheType);
    }

    public StringRequest(String url, Listener<String> listener,
                         Map<String, String> headers, ErrorListener errorListener,
                         DataCacheType cacheType) {
        this(Method.GET, url, headers, null, listener, errorListener, cacheType);
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {

            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            Log.e("-----JSON----", parsed);
            switch (cacheType) {
                case CACHE:
                    DataCache.getDataCache().saveToCache(url, parsed);
                    break;
                case TEMP_CACHE:
                    DataCache.getDataCache().saveToTempCache(url, parsed);
                    break;
                case USE_OLD_CACHE:
                    String cacheStr = DataCache.getDataCache().queryCache(url);
                    //是空证明是第一次请求没有保存数据，不是空就需要返回333，不需要重新加载，下次再加载
                    if (!TextUtils.isEmpty(cacheStr)) {
                        //只保存，返回网络333错误

                        DataCache.getDataCache().saveToCache(url, parsed);
                        String str = "{ code: 0, msg: \"成功\",statusCode: 333,success: true}";

                        return Response.success(str, HttpHeaderParser.parseCacheHeaders(response), 333);

                    }
                    DataCache.getDataCache().saveToCache(url, parsed);
                    break;
                default:
                    break;
            }
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }
}
