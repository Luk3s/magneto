package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import unipi.luk3s.magneto.MagnetoFragment;
import unipi.luk3s.magneto.RadialPositionHelper;

import static unipi.luk3s.magneto.MagnetoUtils.computeNorm;
import static unipi.luk3s.magneto.MagnetoUtils.round;

/**
 * <p>Example of how to use  {@link unipi.luk3s.magneto.MagnetoFragment} together with
 * {@link unipi.luk3s.magneto.RadialPositionHelper} to display the position of a magnet around
 * the compass sensor of a device. The activity can also show the magnet distance from the
 * compass sensor after the {@link unipi.luk3s.magneto.RadialPositionHelper} has been
 * properly initialised.</p>
 *
 * <p>If the user does not want to display the distance of the magnet from the sensor, there is no
 * need to initialise the {@link unipi.luk3s.magneto.RadialPositionHelper}. It is up to the user
 * to choose the right side of the magnet facing the sensor (if the wrong side is chosen, the
 * displayed position is the opposite of the real one).</p>
 *
 * <p>An additional view {@link unipi.luk3s.magnetotest.RedDotView} is used to display the position
 * of the magnet as a red dot on a circle positioned at the centre of a cartesian coordinate
 * system.</p>
 *
 * <p><b>Flow of control</b>: store the magnetic field along the axes when the magnet is over
 * the compass sensor and then when it is far from the device. This flow of control is ensured by
 * enabling and disabling the right buttons.</p>
 */
public class RadialPositionExampleActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    private RadialPositionHelper radHelper;

    private TextView azimuth_textView;
    private TextView euclideanNorm_textView;
    private RedDotView redDotView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radial_position_example);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        MagnetoFragment magneto = MagnetoFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        radHelper = new RadialPositionHelper(magneto);

        azimuth_textView =
                (TextView) findViewById(R.id.RadialPositionExampleActivity_textView_azimuth);
        euclideanNorm_textView =
                (TextView) findViewById(R.id.RadialPositionExampleActivity_textView_norm);
        redDotView = (RedDotView) findViewById(R.id.RadialPositionExampleActivity_redDotView);

        getSupportActionBar().setTitle(R.string.RadialPositionExampleActivity_title);
    }


    /**
     * <ul>
     * <li>Sets max and min using the {@link unipi.luk3s.magneto.RadialPositionHelper}.</li>
     * <li>Enables/Disables the buttons to ensure the right actions order.</li>
     * </ul>
     */
    public void performInit(View view)
    {
        switch (view.getId())
        {
            case R.id.RadialPositionExampleActivity_button_setOverCompassValue:
                radHelper.setOverCompassValue();
                findViewById(R.id.RadialPositionExampleActivity_button_setFarFromCompass).setEnabled(true);
                break;
            case R.id.RadialPositionExampleActivity_button_setFarFromCompass:
                radHelper.setFarAwayValue();
                radHelper.endInit();
                break;
        }
        view.setEnabled(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        float radialPosition = radHelper.getRadialPosition();  //i.e. the azimuth
        float currentNorm = round(computeNorm(smaRoundedValues));

        if(radHelper.hasInitEnded())
        {
            float distanceFromOrigin = radHelper.getDistanceFromOrigin();  //[0,1]

            float newRadius = redDotView.maxCircleRadius * distanceFromOrigin;
            redDotView.setRadius((int)newRadius);
        }

        redDotView.setRedDot(radialPosition);
        redDotView.invalidate();

        azimuth_textView.setText(radialPosition + "°");
        euclideanNorm_textView.setText(currentNorm + " µT");
    }
}
