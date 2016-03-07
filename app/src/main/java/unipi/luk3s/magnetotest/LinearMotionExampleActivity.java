package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import unipi.luk3s.magneto.LinearMotionHelper;
import unipi.luk3s.magneto.MagnetoFragment;

import static unipi.luk3s.magneto.MagnetoUtils.computeNorm;
import static unipi.luk3s.magneto.MagnetoUtils.computeRoundedNorm;
import static unipi.luk3s.magneto.MagnetoUtils.round;

/**
 * <p>Example of how to use  {@link unipi.luk3s.magneto.MagnetoFragment} together with
 * {@link unipi.luk3s.magneto.LinearMotionHelper} to find the position of a magnet along a
 * straight line.</p>
 *
 * <p>A slider displays the position of the magnet along the line. An image is modified as the
 * magnet moves along the line.</p>
 *
 * <p><b>Flow of control</b>: first store the magnitude sensed when the magnet is at the left
 * endpoint of the line, then when it is at the right endpoint. This flow of control is ensured
 * by enabling and disabling the right buttons.</p>
 */
public class LinearMotionExampleActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    private LinearMotionHelper lmHelper;
    private MagnetoFragment magneto;
    private TextView currentMagnitudeTextView;

    private SeekBar slider_seekBar;
    private ImageView imageMirroringSlider_imageView;
    private int initialImgWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_motion_example);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        magneto = MagnetoFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        lmHelper = new LinearMotionHelper(magneto);

        slider_seekBar = (SeekBar) findViewById(R.id.LinearMotionExampleActivity_seekBar);
            slider_seekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        imageMirroringSlider_imageView = (ImageView)
                findViewById(R.id.LinearMotionExampleActivity_imageView_slidingImage);

        initialImgWidth = -1;

        currentMagnitudeTextView =
                ((TextView) findViewById(R.id.LinearMotionExampleActivity_textView_currentMagnitude));

        getSupportActionBar().setTitle(R.string.LinearMotionExampleActivity_title);
    }

    public void storeMagnitude(View view)
    {
        float lastRecordedMagnitude = computeRoundedNorm(magneto.getLastRoundedReadings());
        lmHelper.storeMagnitude();

        switch (view.getId())
        {
            case R.id.LinearMotionExampleActivity_button_leftEnd:
                findViewById(R.id.LinearMotionExampleActivity_button_rightEnd).setEnabled(true);
                break;
            case R.id.LinearMotionExampleActivity_button_rightEnd:
                // Do Nothing
                break;
        }

        TextView currentView = ((TextView) view);
        String text = currentView.getText().toString();
        text += " (" + lastRecordedMagnitude + ")";
        currentView.setText(text);
        view.setEnabled(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {

        if(lmHelper.hasInitEnded())
        {
            // TODO: find the a better "place/time" to get the width
            if(initialImgWidth == -1)
                initialImgWidth = imageMirroringSlider_imageView.getWidth();

            float magnetPosition = lmHelper.getMagnetPosition();

            slider_seekBar.setProgress((int) (magnetPosition * 100));

            int newWidth = (int) (initialImgWidth * magnetPosition);

            imageMirroringSlider_imageView.getLayoutParams().width = newWidth;
            imageMirroringSlider_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageMirroringSlider_imageView.requestLayout();
        }

        String newText = computeRoundedNorm(smaRoundedValues) + " µT";
        currentMagnitudeTextView.setText(newText);
    }
}
