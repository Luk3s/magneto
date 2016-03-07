package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import unipi.luk3s.magneto.MagnetoFragment;

import static unipi.luk3s.magneto.MagnetoUtils.computeAzimuth;
import static unipi.luk3s.magneto.MagnetoUtils.computeNorm;
import static unipi.luk3s.magneto.MagnetoUtils.round;


/**
 * <p>A basic usage of the {@link unipi.luk3s.magneto.MagnetoFragment}.
 * Displays multiple information computed from the magnetic field along the axes.</p>
 */
public class DisplaySensorDataActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{

    private MagnetoFragment magneto;
    private TextView cartesianCoordinates_textView;
    private TextView cylindricalCoordinates_textView;
    private TextView euclideanNorm_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sensor_data);

        cartesianCoordinates_textView = (TextView) findViewById(
                R.id.DisplaySensorDataActivity_textView_magneticFieldCartesianCoordinates);
        cylindricalCoordinates_textView = (TextView) findViewById(
                R.id.DisplaySensorDataActivity_textView_magneticFieldCylindricalCoordinates);
        euclideanNorm_textView = (TextView) findViewById(
                R.id.DisplaySensorDataActivity_textView_magneticFieldNorm);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        magneto = MagnetoFragment.newInstance(true);
        magneto.setShowAccuracyToast(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        getSupportActionBar().setTitle(R.string.DisplaySensorDataActivity_title);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString){}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        cartesianCoordinates_textView.setText("Cartesian Coordinates \n" +
                "x: " + smaRoundedValues[0] + "\n" +
                "y: " + smaRoundedValues[1] + "\n" +
                "z: " + smaRoundedValues[2]);

        float r = round(Math.sqrt(x * x + y * y));
        float theta = computeAzimuth(sensorEvent.values);

        cylindricalCoordinates_textView.setText("Cylindrical Coordinates \n" +
                "ρ: " + r + "\n" +
                "θ: " + theta + "  degrees\n" +
                "z: " + smaRoundedValues[2]);

        float norm_x = round(Math.sqrt(x * x));
        float norm_xy = r;
        float norm_xyz = round(computeNorm(sensorEvent.values));

        euclideanNorm_textView.setText("Norm(s) of the Magnetic Field \n" +
                "x: " + norm_x + "\n" +
                "xy: " + norm_xy + "\n" +
                "xyz: " + norm_xyz);
    }
}
