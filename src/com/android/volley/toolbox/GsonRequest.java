package com.android.volley.toolbox;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import cache.DataCache;
import cache.DataCacheType;

//region Description
//<editor-fold desc="Description">

/**
 * Created by Jackrex on 2/18/14.
 */
public class GsonRequest<T> extends Request<T> {

    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private String url;
    private Map<String, String> params;
    private DataCacheType cacheType;

    /**
     * 带参数 带头(Header)的 GET POST 请求
     *
     * @param method
     * @param url
     * @param clazz
     * @param headers
     * @param params
     * @param listener
     * @param errorListener
     * @param  cacheType
     */
    public GsonRequest(int method, String url, Class<T> clazz,
                       Map<String, String> headers, Map<String, String> params,
                       Response.Listener<T> listener,
                       Response.ErrorListener errorListener, DataCacheType cacheType) {
        super(method, url, errorListener);
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
        this.url = url;
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

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }


    /**
     * 处理网络返回
     *
     * @param response
     * @return
     */
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {

            String json = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            switch (cacheType){
                case NO_CACHE:
                    break;
                case CACHE:
                    DataCache.getDataCache().saveToCache(url, json);
                    break;
                case USE_OLD_CACHE:
                    String cacheStr = DataCache.getDataCache().queryCache(url);
                    //是空证明是第一次请求没有保存数据，不是空就需要返回333，不需要重新加载，下次再加载
                    if (!TextUtils.isEmpty(cacheStr)) {
                        //只保存，返回网络333错误

                        DataCache.getDataCache().saveToCache(url, json);
                        T a = gson.fromJson(json,clazz);
                        try {
                            java.lang.reflect.Method m = clazz.getMethod("setStatusCode", int.class);
                            java.lang.reflect.Method m1 = clazz.getMethod("setCode", int.class);
                            m.setAccessible(true);//因为写成private 所以这里必须设置
                            m1.setAccessible(true);
                            try {
                                try {
                                    a = clazz.newInstance();
                                    m.invoke(a, 333);
                                    m1.invoke(a, 0);
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }


                        return Response.success(a, HttpHeaderParser.parseCacheHeaders(response), 333);

                    }
                    DataCache.getDataCache().saveToCache(url, json);
                    break;
                case TEMP_CACHE:
                    DataCache.getDataCache().saveToTempCache(url, json);
                    break;
            }
            return Response.success(gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            Log.e("JSON_EXCEPTION", "" + e.getCause());
            return Response.error(new ParseError(e));
        }
    }

}
