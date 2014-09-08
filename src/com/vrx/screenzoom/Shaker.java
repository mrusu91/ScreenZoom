package com.vrx.screenzoom;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

public class Shaker {
  private SensorManager sensorManager;
  private long lastShakeTimestamp = 0;
  private double threshold = 1.0d;
  private long gap = 0;
  private Callback callback = null;

  public Shaker(Context context, double threshold, long gap, Callback callback) {
    this.threshold = (threshold * threshold)
        * (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
    this.gap = gap;
    this.callback = callback;
    sensorManager = (SensorManager) context
        .getSystemService(Context.SENSOR_SERVICE);
    sensorManager.registerListener(listener,
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_UI);
  }

  public void close() {
    sensorManager.unregisterListener(listener);
  }

  private void isShaking() {
    lastShakeTimestamp = SystemClock.uptimeMillis();
    if (callback != null) {
      callback.shakingStarted();
    }
  }

  private void isNotShaking() {
    long now = SystemClock.uptimeMillis();
    if (lastShakeTimestamp > 0) {
      if (now - lastShakeTimestamp > gap) {
        lastShakeTimestamp = 0;
        if (callback != null) {
          callback.shakingStopped();
        }
      }
    }
  }

  public interface Callback {
    void shakingStarted();

    void shakingStopped();
  }

  private SensorEventListener listener = new SensorEventListener() {
    public void onSensorChanged(SensorEvent sensorEvent) {
      if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        double netForce = sensorEvent.values[0] * sensorEvent.values[0];
        netForce += sensorEvent.values[1] * sensorEvent.values[1];
        netForce += sensorEvent.values[2] * sensorEvent.values[2];
        if (threshold < netForce) {
          isShaking();
        } else {
          isNotShaking();
        }
      }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // unused
    }
  };

}
