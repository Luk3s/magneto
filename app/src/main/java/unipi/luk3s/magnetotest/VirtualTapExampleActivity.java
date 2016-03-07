package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import unipi.luk3s.magneto.MagnetoFragment;
import unipi.luk3s.magneto.VirtualTapHelper;

import static unipi.luk3s.magneto.MagnetoUtils.computeAzimuth;


/**
 * <p>Example of how to use  {@link unipi.luk3s.magneto.MagnetoFragment} together with
 * {@link unipi.luk3s.magneto.VirtualTapHelper} to sense virtual taps. Every time a tap occurs,
 * a new image is displayed.</p>
 */
public class VirtualTapExampleActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    private VirtualTapHelper tapHelper;

    private ImageView galleryImageView;
    private TextView azimuthTextView;

    private int imageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_tap_example);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        MagnetoFragment magneto  = MagnetoFragment.newInstance(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        tapHelper = new VirtualTapHelper(magneto);

        galleryImageView =
                (ImageView) findViewById(R.id.VirtualTapExampleActivity_imageView_gallery);
        azimuthTextView = (TextView) findViewById(R.id.VirtualTapExampleActivity_textView_angle);

        getSupportActionBar().setTitle(R.string.VirtualTapExampleActivity_title);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString) {}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        if(tapHelper.hasInitEnded() && tapHelper.hasTapBeenDetected())
        {
            int newImage = getResources().getIdentifier("tap_image_" + imageIndex,
                    "drawable", getPackageName());
            galleryImageView.setImageResource(newImage);
            imageIndex = (imageIndex+1)%4;
        }
        azimuthTextView.setText("Azimuth: " + computeAzimuth(smaRoundedValues) );
    }

    public void startDetectingVirtualTap(View view)
    {
        tapHelper.init();
        view.setEnabled(false);
    }
}
