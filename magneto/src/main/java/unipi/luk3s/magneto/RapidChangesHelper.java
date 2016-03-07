package unipi.luk3s.magneto;

import android.hardware.SensorEvent;

import static unipi.luk3s.magneto.MagnetoUtils.computeNorm;
import static unipi.luk3s.magneto.MagnetoUtils.computeRoundedNorm;
import static unipi.luk3s.magneto.MagnetoUtils.round;

/**
 * <p>To be used together with {@link MagnetoFragment}.
 * Helps in identifying rapid changes of the magnetic field, usually a consequence of flip-like
 * actions and fast movements of a magnet nearby the compass sensor.</p>
 *
 * <p>The initialisation consists in storing a low and  a high threshold.
 * When the sensed high-pass filtered euclidean norm of the magnetic field is higher than the high
 * threshold, a rapid change in the magnetic field is detected. In order to detect a new rapid
 * change, the sensed euclidean norm should fall below the low threshold and then get higher than
 * the high one. An internal variable counts how many taps since the end of the initalisation
 * have been detected.</p>
 *
 * <p>After the initialisation, every time in the activity implementing
 * {@link unipi.luk3s.magneto.MagnetoFragment.MagneticSensorEventListener} the method
 * {@link unipi.luk3s.magneto.MagnetoFragment.MagneticSensorEventListener#onSensorChanged(SensorEvent)}
 * gets called, the method {@link #hasATapBeenDetected()} should be invoked to check if a rapid
 * change in the magnetic field has happened. Doing so, the internal tap counter is increased
 * accordingly.</p>
 *
 * <p>For more information read Section 4.5.5 of
 * <a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/">
 *     Interacting with mobile devices using magnetic fields</a></p>
 */
public class RapidChangesHelper
{
    private final MagnetoFragment magneto;
    private boolean initialisationEnded;
    private boolean magnitudeAlreadyHigherThanHighThreshold;
    private int tapCounter;
    private float lowThreshold;
    private float highThreshold;

    public RapidChangesHelper(MagnetoFragment magneto)
    {
        this.magneto = magneto;
        initialisationEnded = false;
        magnitudeAlreadyHigherThanHighThreshold = false;
    }

    public void setLowThreshold(float threshold)
    {
        lowThreshold = threshold;
    }

    public void setHighThreshold(float threshold)
    {
        highThreshold = threshold;
    }

    public void endInit()
    {
        initialisationEnded = true;
    }

    /** Clearing the thresholds invalidates the previous initialisation*/
    public void clearThresholds()
    {
        highThreshold = lowThreshold = 0;
        initialisationEnded = false;
    }

    public boolean hasInitEnded()
    {
        return initialisationEnded;
    }

    public float getHighPassMagnitude()
    {
        return computeNorm(magneto.getLastHighPassReadings());
    }

    public float[] getHighPassValues()
    {
        return magneto.getLastHighPassReadings();
    }

    private void updateTapCounter()
    {
        float highPassFilteredValueMagnitude = computeRoundedNorm(magneto.getLastHighPassReadings());

        if(highPassFilteredValueMagnitude > highThreshold && !magnitudeAlreadyHigherThanHighThreshold)
        {
            tapCounter++;
            magnitudeAlreadyHigherThanHighThreshold = true;
        }
        else
        if(magnitudeAlreadyHigherThanHighThreshold && highPassFilteredValueMagnitude < lowThreshold)
        {
            magnitudeAlreadyHigherThanHighThreshold = false;
        }
    }

    public boolean hasATapBeenDetected()
    {
        int oldTapCounter = tapCounter;

        updateTapCounter();

        return (oldTapCounter != tapCounter);
    }

    public int getTapCounter()
    {
        return tapCounter;
    }

    public void clearTapCounter()
    {
        tapCounter = 0;
    }
}
