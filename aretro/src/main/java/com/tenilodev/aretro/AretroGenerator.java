package com.tenilodev.aretro;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AretroGenerator {

    private static OkHttpClient.Builder httpClient = null;

    private static Retrofit.Builder builder = new Retrofit.Builder();
    private static Retrofit retrofit = null;


    private static HttpLoggingInterceptor loggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logging;
    }

    public static <S> S createService(String base_url, Class<S> serviceClass) {

        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder();
        }

        httpClient.addInterceptor(loggingInterceptor());
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("User-Agent", "Atupato-App")
                        .header("Accept", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });

        builder.baseUrl(base_url);
        builder.addConverterFactory(new ToStringConverterFactory());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.client(httpClient.build());

        if (retrofit == null) {
            retrofit = builder.build();
        }

        return retrofit.create(serviceClass);
    }


    private static class ToStringConverterFactory extends Converter.Factory {
        private final MediaType MEDIA_TYPE = MediaType.parse("text/plain");


        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            if (String.class.equals(type)) {
                return new Converter<ResponseBody, String>() {
                    @Override
                    public String convert(ResponseBody value) throws IOException {
                        return value.string();
                    }
                };
            }
            return null;
        }

        @Override
        public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                              Annotation[] methodAnnotations, Retrofit retrofit) {

            if (String.class.equals(type)) {
                return new Converter<String, RequestBody>() {
                    @Override
                    public RequestBody convert(String value) throws IOException {
                        return RequestBody.create(MEDIA_TYPE, value);
                    }
                };
            }
            return null;
        }
    }

}
