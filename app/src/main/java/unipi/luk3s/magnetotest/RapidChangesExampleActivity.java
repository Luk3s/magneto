package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import unipi.luk3s.magneto.MagnetoFragment;
import unipi.luk3s.magneto.RapidChangesHelper;

import static unipi.luk3s.magneto.MagnetoUtils.round;

/**
 * <p>Example of how to use  {@link unipi.luk3s.magneto.MagnetoFragment} together with
 * {@link unipi.luk3s.magneto.RapidChangesHelper} to sense fast changes in the magnetic
 * field around the compass sensor.</p>
 *
 * <p><b>Flow of control</b>: first set the low threshold, and then the high one.
 * This flow of control is ensured by enabling and disabling the right buttons. It is possible
 * to reset the threshold and start again (the flow of control stays the same).</p>
 */
public class RapidChangesExampleActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    private RapidChangesHelper movHelper;

    private TextView magneticFieldTextView;
    private TextView tapCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rapid_changes_example);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        MagnetoFragment magneto = MagnetoFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        movHelper = new RapidChangesHelper(magneto);

        magneticFieldTextView = (TextView) findViewById(R.id.RapidChangesExampleActivity_textView_magnitude);
        tapCounter = (TextView) findViewById(R.id.RapidChangesExampleActivity_textView_tapCounter);

        getSupportActionBar().setTitle(R.string.RapidChangesExampleActivity_title);
    }

    public void setThresholds(View view)
    {
        view.setEnabled(false);

        switch (view.getId())
        {
            case R.id.RapidChangesExampleActivity_button_setLowThreshold:

                EditText lowThresholdEditText =
                        (EditText) findViewById(R.id.RapidChangesExampleActivity_editText_lowThreshold);
                lowThresholdEditText.setEnabled(false);
                String lowThresholdValueString = lowThresholdEditText.getText().toString();
                movHelper.setLowThreshold(Integer.parseInt(lowThresholdValueString));
                findViewById(R.id.RapidChangesExampleActivity_button_setHighThreshold).setEnabled(true);
                break;
            case R.id.RapidChangesExampleActivity_button_setHighThreshold:
                EditText highThresholdEditText =
                        (EditText) findViewById(R.id.RapidChangesExampleActivity_editText_highThreshold);
                highThresholdEditText.setEnabled(false);
                String highThresholdValueString = highThresholdEditText.getText().toString();
                movHelper.setHighThreshold(Integer.parseInt(highThresholdValueString));
                movHelper.endInit();
                break;
        }
    }

    public void resetActivity(View view)
    {
        movHelper.clearThresholds();
        movHelper.clearTapCounter();

        EditText highThresholdEditText =
                (EditText) findViewById(R.id.RapidChangesExampleActivity_editText_highThreshold);
        highThresholdEditText.setText("0");
        highThresholdEditText.setEnabled(false);


        EditText lowThresholdEditText =
                (EditText) findViewById(R.id.RapidChangesExampleActivity_editText_lowThreshold);
        lowThresholdEditText.setText("0");
        lowThresholdEditText.setEnabled(false);

        (findViewById(R.id.RapidChangesExampleActivity_button_setLowThreshold)).setEnabled(true);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues,
                                float[] smaRoundedValues, float[] highPassValues)
    {
        if(movHelper.hasInitEnded() && movHelper.hasATapBeenDetected())
            tapCounter.setText(movHelper.getTapCounter() + "");

        float highPassFilteredValueMagnitude = movHelper.getHighPassMagnitude();
        highPassFilteredValueMagnitude = round(highPassFilteredValueMagnitude);
        magneticFieldTextView.setText("Filtered Magnitude: " + highPassFilteredValueMagnitude);
    }

}
