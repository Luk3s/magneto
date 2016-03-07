package unipi.luk3s.magnetotest;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import unipi.luk3s.magneto.MagnetoFragment;
import unipi.luk3s.magneto.PositionHelper;

import static unipi.luk3s.magneto.IdentificationHelper.NUMBER_OF_MAGNETS;
import static unipi.luk3s.magneto.IdentificationHelper.THRESHOLD;

/**
 * <p>Example of how to use  {@link unipi.luk3s.magneto.MagnetoFragment} together with
 * {@link unipi.luk3s.magneto.PositionHelper} to identify different positions
 * of a magnet in space. Only two positions are being identified, a magnet has been embedded
 * in a common switch (the positions represent the ON and OFF states)</p>
 *
 * <p>A <i>Number Picker</i> is provided in order to let users changing the threshold used
 * for the recognition.</p>
 *
 * <p><b>Flow of control</b>: store the magnetic field along the axes of the ON state, then the
 * same for the OFF state. This flow of control is ensured by enabling and disabling
 * the right buttons.</p>
 */
public class PositionExampleActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener,NumberPicker.OnValueChangeListener
{
    //Constants to identify the two states
    private static final int ON = 0;
    private static final int OFF = 1;

    private MagnetoFragment magneto;
    private PositionHelper posHelper;
    private View showGuessedColour_view;
    private TextView currentMagneticField_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_example);

        Intent intent = getIntent();
        int numberOfMagnets = intent.getIntExtra(NUMBER_OF_MAGNETS, 2);
        float threshold = intent.getFloatExtra(THRESHOLD, 2F);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        magneto = MagnetoFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        posHelper = new PositionHelper(magneto, numberOfMagnets, threshold);

        showGuessedColour_view = findViewById(R.id.PositionExampleActivity_view_state);
        currentMagneticField_textView =
                ((TextView) findViewById(R.id.PositionExampleActivity_textView_currentReadings));

        NumberPicker thresholdSelector = (NumberPicker) findViewById(
                R.id.PositionExampleActivity_numberPicker_threshold);
        thresholdSelector.setMaxValue(15);
        thresholdSelector.setMinValue(0);
        thresholdSelector.setValue(2);
        thresholdSelector.setWrapSelectorWheel(false);
        thresholdSelector.setOnValueChangedListener(this);

        getSupportActionBar().setTitle(R.string.PositionExampleActivity_title);
    }

    /**
     * <ul>
     * <li>Stores the magnetic field along the axes using the PositionHelper.</li>
     * <li>Updates the label of the button invoking this method by adding the value of the stored
     * magnetic field along the axes to it.</li>
     * <li>Enables/Disables the buttons to ensure the right actions order.</li>
     * </ul>
     */
    public void storeMagneticField(View view)
    {
        float[] lastRecordedReadings = magneto.getLastRoundedReadings();
        posHelper.storeCurrentlySensedMagneticField();

        Button currentView = ((Button) view);
        String text = currentView.getText().toString();
        text += "\n (" + lastRecordedReadings[0] + ", "
                + lastRecordedReadings[1] + ", " +lastRecordedReadings[2] + ")";
        currentView.setText(text);

        switch (view.getId())
        {
            case R.id.PositionExampleActivity_button_on:
                findViewById(R.id.PositionExampleActivity_button_off).setEnabled(true);
                break;
            case R.id.IdentificationActivity_button_circle:
                // Do nothing here
                break;
        }
        view.setEnabled(false);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal)
    {
        posHelper.setThreshold(newVal);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        if(posHelper.hasInitEnded())
            if(posHelper.doesCurrentlySensedMagneticFieldMatchAStoredOne())
                switch (posHelper.getLastMatchedMagneticFieldIndex())
                {
                    case ON:
                        showGuessedColour_view.setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.holo_green_dark));
                        break;
                    case OFF:
                        showGuessedColour_view.setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        break;
                }
            else
                showGuessedColour_view.setBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.darker_gray));

        String newLabel = "(" + smaRoundedValues[0] + ", " + smaRoundedValues[1]
                                                            + ", " + smaRoundedValues[2] + ") µT";
        currentMagneticField_textView.setText(newLabel);
    }
}
