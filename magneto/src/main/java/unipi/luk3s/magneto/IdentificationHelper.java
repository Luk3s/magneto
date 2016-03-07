package unipi.luk3s.magneto;

import static unipi.luk3s.magneto.MagnetoUtils.computeRoundedNorm;

/**
 * <p>To be used together with {@link MagnetoFragment}.
 * Helps in identifying which magnet, in a set of magnets with different shapes and generated 
 * magnetic field, is in a predetermined position close to the device compass sensor.</p>
 *
 * <p>It is possible to choose the number of magnets to be identified.
 * A threshold value is used to improve magnets recognition, the higher the threshold the
 * higher the possibility of a false positive. Once the magnet is in its final position, the
 * magnitude sensed can be stored by calling {@link #storeCurrentlySensedMagnitude()}, which returns
 * the unique index in the array of stored magnitudes (AID for short). </p>
 *
 * <p>Only after the initialisation is over (i.e. all the magnitudes have been stored), the
 * activity should query the helper by calling {@link #doesCurrentlySensedMagnitudeMatchAStoredOne()}.
 * If a match if found, i.e. the previous method returns true, it is possible to retrieve the
 * AID by calling {@link #getLastMatchedMagnitudeIndex()} </p>
 *
 * <p>For more information read Section 4.5.1 of
 * <a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/">
 *     Interacting with mobile devices using magnetic fields</a></p>
 */
public class IdentificationHelper
{
    // Used in Intents and/or Bundles
    public static final String NUMBER_OF_MAGNETS =
            "unipi.luk3s.magneto.IdentificationHelper.NumberOfMagnets";
    public static final String THRESHOLD =
            "unipi.luk3s.magneto.IdentificationHelper.Threshold";

    private float threshold;
    private float[] storedMagnitudes;
    private int lastMatchedMagnitudeIndex;
    private int lastStoredMagnitudeIndex;
    private final MagnetoFragment magneto;

    public IdentificationHelper(MagnetoFragment magneto, int numberOfMagnets, float threshold)
    {
        storedMagnitudes = new float[numberOfMagnets];
        this.threshold = threshold;
        this.magneto = magneto;
        lastMatchedMagnitudeIndex = -1;
        lastStoredMagnitudeIndex = -1;
    }

    /**
     * <p>Note: if the specified number of magnitudes have already been stored, the method
     * returns the value -1 (it means there is no more space to store new magnitudes)</p>
     */
    public int storeCurrentlySensedMagnitude()
    {
        if(hasInitEnded())
            return -1;

        float magnitude = computeRoundedNorm(magneto.getLastRoundedReadings());

        lastStoredMagnitudeIndex++;
        storedMagnitudes[lastStoredMagnitudeIndex] = magnitude;

        return lastStoredMagnitudeIndex;
    }

    public boolean hasInitEnded()
    {
        return (lastStoredMagnitudeIndex == storedMagnitudes.length-1);
    }

    public boolean doesCurrentlySensedMagnitudeMatchAStoredOne()
    {
        float magnitude = computeRoundedNorm(magneto.getLastRoundedReadings());

        for(int i=0; i<storedMagnitudes.length; i++)
        {
            if( magnitude>=(storedMagnitudes[i]-threshold)
                    && magnitude<=(storedMagnitudes[i]+threshold) )
            {
                lastMatchedMagnitudeIndex = i;
                return true;
            }
        }
        return false;
    }

    public int getLastMatchedMagnitudeIndex()
    {
        return lastMatchedMagnitudeIndex;
    }

    public void setThreshold(float threshold)
    {
        this.threshold = threshold;
    }

    public float getThreshold()
    {
        return threshold;
    }
}
