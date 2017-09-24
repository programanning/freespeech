package com.example.freespeech;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OpenService extends Service {
    public OpenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
