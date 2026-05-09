package com.example.secretpanda;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Protocol;

@Implements(OkHttpClient.class)
public class ShadowOkHttpClient {

    // Simplemente ignoramos la creación de nuevos clientes
    // para evitar que se levanten Dispatchers y TaskRunners infinitos

    @Implementation
    public Call newCall(Request request) {
        // En los tests, no queremos que OkHttp levante hilos de verdad.
        // Haremos que responda un Error 500 falso al instante.
        return new okhttp3.Call() {
            @Override
            public Request request() { return request; }

            @Override
            public Response execute() { return buildFakeResponse(request); }

            @Override
            public void enqueue(okhttp3.Callback responseCallback) {
                try {
                    responseCallback.onResponse(this, buildFakeResponse(request));
                } catch (Exception e) {}
            }

            @Override
            public void cancel() {}

            @Override
            public boolean isExecuted() { return true; }

            @Override
            public boolean isCanceled() { return false; }

            @Override
            public okio.Timeout timeout() { return okio.Timeout.NONE; }

            @Override
            public Call clone() { return this; }
        };
    }

    private Response buildFakeResponse(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(500) // Fallo falso
                .message("Test Blocked")
                .body(okhttp3.ResponseBody.create(null, new byte[0]))
                .build();
    }
}