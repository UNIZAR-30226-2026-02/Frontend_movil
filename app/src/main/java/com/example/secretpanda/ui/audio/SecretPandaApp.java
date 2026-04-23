package com.example.secretpanda.ui.audio;

import android.app.Application;
import androidx.lifecycle.ProcessLifecycleOwner;

public class SecretPandaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver());
    }
}
