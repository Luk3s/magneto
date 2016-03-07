package unipi.luk3s.magnetotest;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import unipi.luk3s.magneto.MagnetoFragment;
import unipi.luk3s.magneto.IdentificationHelper;

import static unipi.luk3s.magneto.IdentificationHelper.NUMBER_OF_MAGNETS;
import static unipi.luk3s.magneto.IdentificationHelper.THRESHOLD;
import static unipi.luk3s.magneto.MagnetoUtils.computeRoundedNorm;

/**
 * <p>Example of how to use  {@link unipi.luk3s.magneto.MagnetoFragment} together with
 * {@link unipi.luk3s.magneto.IdentificationHelper} to identify three different magnets.</p>
 *
 * <p><b>Flow of control</b>: first store the magnitude of the rectangle, then the circle's,
 * finally the cylinder's. This flow of control is ensured by enabling and disabling
 * the right buttons.</p>
 */
public class IdentificationExampleActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    // Constants that identify the different magnets.
    private static final int RECTANGLE = 0;
    private static final int CIRCLE = 1;
    private static final int CYLINDER = 2;

    private MagnetoFragment magneto;
    private IdentificationHelper idHelper;
    private ImageView showGuessedImage_imageView;
    private TextView currentMagnitude_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_example);

        Intent intent = getIntent();
        int numberOfMagnets = intent.getIntExtra(NUMBER_OF_MAGNETS, 3);
        float threshold = intent.getFloatExtra(THRESHOLD, 1.83F);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        magneto = MagnetoFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        idHelper = new IdentificationHelper(magneto, numberOfMagnets, threshold);

        showGuessedImage_imageView =
                (ImageView) findViewById(R.id.IdentificationActivity_imageView_guess);
        currentMagnitude_textView =
                ((TextView) findViewById(R.id.IdentificationActivity_textView_currentMagnitude));

        getSupportActionBar().setTitle(R.string.IdentificationExampleActivity_title);
    }

    /**
     * <ul>
     * <li>Stores the magnitude using the <i>IdentificationHelper</i>.</li>
     * <li>Updates the label of the button invoking this method by adding the value of the stored
     * magnitude to it.</li>
     * <li>Enables/Disables the buttons to ensure the right actions order.</li>
     * </ul>
     */
    public void storeMagnitude(View view)
    {
        idHelper.storeCurrentlySensedMagnitude();

        // Update the button current label by adding to it the current magnitude
        Button currentView = ((Button) view);
        String text = currentView.getText().toString();
        text += " (" + computeRoundedNorm(magneto.getLastRoundedReadings()) + ")";
        currentView.setText(text);

        // Enable/Disable the right buttons
        switch (view.getId())
        {
            case R.id.IdentificationActivity_button_rectangle:
                findViewById(R.id.IdentificationActivity_button_circle).setEnabled(true);
                break;
            case R.id.IdentificationActivity_button_circle:
                findViewById(R.id.IdentificationActivity_button_cylinder).setEnabled(true);
                break;
            case R.id.IdentificationActivity_button_cylinder:
                // Nothing to do here
                break;
        }
        view.setEnabled(false);
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString) {}


    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        if(idHelper.hasInitEnded())
            if(idHelper.doesCurrentlySensedMagnitudeMatchAStoredOne())
                switch (idHelper.getLastMatchedMagnitudeIndex())
                {
                    case RECTANGLE:
                        showGuessedImage_imageView.setImageResource(R.drawable.rectangle);
                        break;
                    case CIRCLE:
                        showGuessedImage_imageView.setImageResource(R.drawable.circle);
                        break;
                    case CYLINDER:
                        showGuessedImage_imageView.setImageResource(R.drawable.cylinder);
                        break;
                }
            else
                showGuessedImage_imageView.setImageResource(R.drawable.guessing);

        currentMagnitude_textView.setText(computeRoundedNorm(smaRoundedValues) + " µT");
    }

}
