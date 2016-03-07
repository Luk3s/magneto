package unipi.luk3s.magneto;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static unipi.luk3s.magneto.MagnetoUtils.round;

/**
 * <p>This UI-less fragment lets you access the device's compass sensor. A low-pass filter is 
 * applied to the raw sensor data in order to stabilise the readings. Add the fragment to an
 * activity in its onCreate method. The fragment makes sure to disable the compass sensor when the
 * activity that added it is paused, and enable it when it resumes. A common way to add the
 * fragment to an activity is the following:</p>
 *
 * <pre class="prettyprint">
 * MagnetoFragment magneto = MagnetoFragment.newInstance(true);
 * FragmentManager fragmentManager = getSupportFragmentManager();
 * FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 * fragmentTransaction.add(magneto, "MagnetoFragment");
 * fragmentTransaction.commit();
 * </pre>
 *
 * <p>If the activity that adds this fragment do not implement the
 * {@link unipi.luk3s.magneto.MagnetoFragment.MagneticSensorEventListener} a <i>RuntimeException</i>
 * is thrown. The fragment communicates with the actual sensor through the
 * {@link SensorEventListener}. The default rate at which events are received is
 * {#link SensorManager#SENSOR_DELAY_GAME}.</p>
 *
 * @see unipi.luk3s.magneto.MagnetoFragment.MagneticSensorEventListener
 * @see SensorEventListener
 * @see SensorManager
 * @see SensorEvent
 */
public class MagnetoFragment extends Fragment implements SensorEventListener
{
    // Used in Intents and/or Bundles
    private static final String SHOW_ACCURACY_TOAST = "showAccuracyToast";
    private static final String SENSOR_DELAY= "sensorDelay";

    private boolean showAccuracyToast;
    private int sensorDelay;

    private SensorManager sensorManagerReference;
    private Sensor magnetometerReference;
    private MagneticSensorEventListener magneticSensorEventListener;

    private SimpleMovingAverage sma;
    private float[] smaValues;
    private float[] smaRoundedValues;
    private float[] highPassValues;

    public MagnetoFragment()
    {
        // Required empty public constructor
    }

    public static MagnetoFragment newInstance(boolean showAccuracyToast)
    {
        MagnetoFragment fragment = new MagnetoFragment();

        Bundle args = new Bundle();
        args.putBoolean(SHOW_ACCURACY_TOAST, showAccuracyToast);
        args.putInt(SENSOR_DELAY, SensorManager.SENSOR_DELAY_GAME);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            showAccuracyToast = getArguments().getBoolean(SHOW_ACCURACY_TOAST);
            sensorDelay = getArguments().getInt(SENSOR_DELAY);
        }

        sma = new SimpleMovingAverage();
        smaValues = new float[3];
        smaRoundedValues = new float[3];
        highPassValues = new float[3];

        sensorManagerReference = (SensorManager)
                getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        magnetometerReference = sensorManagerReference.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // No UI required
        return null;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof MagneticSensorEventListener)
            magneticSensorEventListener = (MagneticSensorEventListener) context;
        else
            throw new RuntimeException(context.toString()
                    + " must implement MagneticSensorEventListener");
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        magneticSensorEventListener = null;
    }

    /**
     * Register and unregister are useful to avoid draining the battery
     */
    public void onResume()
    {
        super.onResume();
        sensorManagerReference.registerListener(this, magnetometerReference, sensorDelay);
    }

    /**
     * Register and unregister are useful to avoid draining the battery
     */
    public void onPause()
    {
        super.onPause();
        sensorManagerReference.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putBoolean(SHOW_ACCURACY_TOAST, showAccuracyToast);
        savedInstanceState.putInt(SENSOR_DELAY, sensorDelay);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * <p>For more information about the delay see the
     * {@link SensorManager#registerListener(SensorEventListener, Sensor, int) registerListener}
     * method of the {@link SensorManager}.</p>
     */
    public void modifySensorDelay(int delay)
    {
        sensorDelay = delay;
        sensorManagerReference.unregisterListener(this);
        sensorManagerReference.registerListener(this, magnetometerReference, sensorDelay);
    }

    /**
     * <p>Used for receiving notifications from the MagnetoFragment when sensor values have changed.
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.</p>
     */
    public interface MagneticSensorEventListener
    {
        /**
         * <p>Called when the accuracy of the compass sensor has changed. If
         * {@link unipi.luk3s.magneto.MagnetoFragment#showAccuracyToast} is set to {@code true}
         * a Toast (LENGTH_LONG) showing the new accuracy is displayed.</p>
         *
         * <p>See the SENSOR_STATUS_* constants in
         * {@link android.hardware.SensorManager SensorManager} for details.</p>
         *
         * <p>See  {@link SensorEventListener#onAccuracyChanged(Sensor,int)} for more 
         * information.</p>
         *
         * @param sensor The ID of the compass sensor
         * @param accuracy The new accuracy of the compass sensor, one of
         *         {@code SensorManager.SENSOR_STATUS_*}
         * @param accuracyToString The new accuracy as a String, one of {Unreliable, Low, High,
         *                         Medium}.
         */
        void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString);

        /**
         * <p>Called when sensor values have changed.</p>
         *
         * <p><b>NOTE:</b> The application doesn't own the
         * {@link android.hardware.SensorEvent event} object passed as a parameter and therefore
         * cannot hold on to it. The object may be part of an internal pool and may be reused by
         * the framework. The smaValues and smaRoundedValues arrays are reused every time
         * a new event is fired, so if you need store some values in your application, please
         * clone the array instead of copy its reference.</p>
         *
         * <p>See  {@link SensorEventListener#onSensorChanged(SensorEvent)} for more 
         * information.</p>
         *
         * @param sensorEvent the {@link SensorEvent SensorEvent}.
         * @param smaValues low-pass filtered magnetic field strength along the axes. In detail:
         *                  <ul>
         *                  <li>smaValues[0] strength along x.</li>
         *                  <li>smaValues[1] strength along y.</li>
         *                  <li>smaValues[2] strength along z.</li>
         *                  </ul>
         * @param smaRoundedValues the smaValues rounded to 2 decimal places.
         * @param highPassValues high-pass filtered magnetic field strength along the axes
         */
        void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues);
    }

    /**
     * @see unipi.luk3s.magneto.MagnetoFragment.MagneticSensorEventListener#onAccuracyChanged(Sensor, int, String) MagneticSensorEventListener#onAccuracyChanged
     */
    public void setShowAccuracyToast(boolean showAccuracyToast)
    {
        this.showAccuracyToast = showAccuracyToast;
    }

    /**
     * Tha vast majority of applications want to use the low-pass filtered rounded values.
     */
    public float[] getLastRoundedReadings()
    {
        return smaRoundedValues;
    }

    public float[] getLastHighPassReadings()
    {
        return highPassValues;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        String accuracyToString = "";

        switch (accuracy)
        {
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                accuracyToString = "Unreliable";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyToString = "Low";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyToString = "Medium";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyToString = "High";
                break;
        }

        if(showAccuracyToast)
            Toast.makeText(getActivity(),
                    "Compass Accuracy: " + accuracyToString, Toast.LENGTH_LONG).show();

        magneticSensorEventListener.onAccuracyChanged(sensor, accuracy, accuracyToString);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        sma.updateSMA(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

        smaValues = sma.getValues();

        smaRoundedValues[0] = round(smaValues[0]);
        smaRoundedValues[1] = round(smaValues[1]);
        smaRoundedValues[2] = round(smaValues[2]);

        highPassValues[0] = round(sensorEvent.values[0] - smaValues[0]);
        highPassValues[1] = round(sensorEvent.values[1] - smaValues[1]);
        highPassValues[2] = round(sensorEvent.values[2] - smaValues[2]);

        magneticSensorEventListener.onSensorChanged(sensorEvent, smaValues, smaRoundedValues,
                highPassValues);
    }

}
