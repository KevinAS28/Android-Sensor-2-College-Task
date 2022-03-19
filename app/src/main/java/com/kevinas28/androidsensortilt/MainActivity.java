package com.kevinas28.androidsensortilt;


import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.SensorEvent;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView spotTop;
    private ImageView spotBottom;
    private ImageView spotLeft;
    private ImageView spotRight;

    private SensorManager mSensorManager;

    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

    private TextView mTextSensorAzimuth;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;

    private static final float VALUE_DRIFT = 0.05f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mTextSensorAzimuth = findViewById(R.id.label_azimuth);
        mTextSensorPitch = findViewById(R.id.label_pitch);
        mTextSensorRoll = findViewById(R.id.label_roll);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        spotTop = findViewById(R.id.spot_top);
        spotBottom = findViewById(R.id.spot_bottom);
        spotRight = findViewById(R.id.spot_right);
        spotLeft = findViewById(R.id.spot_left);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }

        float[] rotationMatrix = new float[9];
        boolean rotationOk = SensorManager.getRotationMatrix(rotationMatrix, null, mAccelerometerData, mMagnetometerData);
        if (!rotationOk) {
            return;
        }

        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        float azimuth = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];

        spotRight.setAlpha(0f);
        spotLeft.setAlpha(0f);
        spotTop.setAlpha(0f);
        spotBottom.setAlpha(0f);

        if (Math.abs(pitch) < VALUE_DRIFT) {
            pitch = 0;
        }
        if (Math.abs(roll) < VALUE_DRIFT) {
            roll = 0;
        }

        if (pitch > 0) {
            spotBottom.setAlpha(pitch);
        } else {
            spotTop.setAlpha(Math.abs(pitch));
        }

        if (roll > 0) {
            spotLeft.setAlpha(roll);
        } else {
            spotRight.setAlpha(Math.abs(roll));
        }

        String azimuthValue = getResources().getString(R.string.value_format, azimuth);
        String pitchValue = getResources().getString(R.string.value_format, pitch);
        String rollValue = getResources().getString(R.string.value_format, roll);
        String azimuthLabel = getResources().getString(R.string.label_azimuth_string);
        String pitchLabel = getResources().getString(R.string.label_pitch_string);
        String rollLabel = getResources().getString(R.string.label_roll_string);

        mTextSensorAzimuth.setText(String.format("%s %s", azimuthLabel, azimuthValue));
        mTextSensorPitch.setText(String.format("%s %s", pitchLabel, pitchValue));
        mTextSensorRoll.setText(String.format("%s %s", rollLabel, rollValue));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}