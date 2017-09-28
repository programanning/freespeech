package com.example.freespeech;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.IBinder;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.Toast;

public class OpenService  extends Service implements SensorEventListener {
    SensorManager sensorManager;
    int count = 0;

    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;
    private static final int MIN_FORCE = 10;
    private static final int MIN_DIRECTION_CHANGE = 3;
    private static final int MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE = 200;
    private static final int MAX_TOTAL_DURATION_OF_SHAKE = 400;
    private long mFirstDirectionChangeTime = 0;
    private long mLastDirectionChangeTime;
    private int mDirectionChangeCount = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        return START_STICKY;
    }

    public void onStart(Intent intent, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressWarnings("deprecation")
    private void getAccelerometer(SensorEvent event) {
        //Toast.makeText(getApplicationContext(),"Shaked",Toast.LENGTH_LONG).show();

        float x = event.values[SensorManager.DATA_X];
        float y = event.values[SensorManager.DATA_Y];
        float z = event.values[SensorManager.DATA_Z];

        float xMovement = Math.abs(x-lastX);
        float yMovement = Math.abs(y-lastY);
        float zMovement = Math.abs(z-lastZ);

        if ((xMovement > MIN_FORCE) || (yMovement > MIN_FORCE) || (zMovement > MIN_FORCE)) {
            long now = System.currentTimeMillis();
            if (mFirstDirectionChangeTime == 0) {
                mFirstDirectionChangeTime = now;
                mLastDirectionChangeTime = now;
            }

            long lastChangeWasAgo = now - mLastDirectionChangeTime;

            if (lastChangeWasAgo < MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE) {
                mLastDirectionChangeTime = now;
                mDirectionChangeCount++;

                lastX = x;
                lastY = y;
                lastZ = z;

                if (mDirectionChangeCount >= MIN_DIRECTION_CHANGE) {
                    long totalDuration = now - mFirstDirectionChangeTime;
                    if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {
//                        ADD OPEN CODE HERE!!!

                        KeyguardManager myKM = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
                        if (myKM.inKeyguardRestrictedInputMode()){
                            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                            wakeLock.acquire();

                            KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
                            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");

                            keyguardLock.disableKeyguard();
                            resetShakeParameters();
                        }
                    }
                }

            } else {
                resetShakeParameters();
            }
        }
    }


    public void onSensorChanged(SensorEvent event) {
        getAccelerometer(event);
    }

    protected void onResume() {
        sensorManager.registerListener(this, sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        sensorManager.unregisterListener(this);
    }

    private void resetShakeParameters() {
        mFirstDirectionChangeTime = 0;
        mDirectionChangeCount = 0;
        mLastDirectionChangeTime = 0;
        lastX = 0;
        lastY = 0;
        lastZ = 0;
    }
}