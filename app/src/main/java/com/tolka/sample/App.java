package com.tolka.sample;

import android.app.Application;

public class App extends Application {
    static {
        System.loadLibrary("ijkffmpeg");
    }
}
