package unipi.luk3s.magneto;

/**
 * <p>To be used together with {@link MagnetoFragment}.
 * Helps in identifying different positions of a magnet in space.</p>
 *
 * <p>It is possible to choose the number of positions to be identified.
 * A threshold value is used to improve magnets recognition, the higher the threshold the
 * higher the possibility of a false positive. Once the magnet is in one of the positions, the
 * magnetic field sensed can be stored by calling {@link #storeCurrentlySensedMagneticField()}, which
 * returns the unique index in the 2D-array of stored magnetic fields (AID for short). </p>
 *
 * <p>Only after the initialisation is over (i.e. all the magnetic fields have been stored),
 * the activity should query the helper by calling
 * {@link #doesCurrentlySensedMagneticFieldMatchAStoredOne()}.
 * If a match if found, i.e. the previous method returns true, it is possible to retrieve the
 * AID by calling {@link #getLastMatchedMagneticFieldIndex()} </p>
 *
 * <p>For more information read Section 4.5.2 of
 * <a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/">
 *     Interacting with mobile devices using magnetic fields</a></p>
 */
public class PositionHelper
{
    // Used in Intents and/or Bundles
    public static final String NUMBER_OF_POSITIONS =
            "unipi.luk3s.magneto.PositionHelper.NumberOfPositions";
    public static final String THRESHOLD =
            "unipi.luk3s.magneto.PositionHelper.Threshold";

    private float[][] storedMagneticFieldsAlongAxes;
    private float threshold;
    private int lastMatch;
    private int lastStoredFieldIndex;
    private final MagnetoFragment magneto;

    public PositionHelper(MagnetoFragment magneto, int numberOfPositions, float threshold)
    {
        storedMagneticFieldsAlongAxes = new float[numberOfPositions][3];
        this.threshold = threshold;
        this.magneto = magneto;
        lastMatch = -1;
        lastStoredFieldIndex = -1;
    }

    /**
     * <p>Note: if the specified number of magnetic fields have already been stored, the method
     * returns the value -1 (it means there is no more space to store new ones)</p>
     */
    public int storeCurrentlySensedMagneticField()
    {
        if(hasInitEnded())
            return -1;

        lastStoredFieldIndex++;
        storedMagneticFieldsAlongAxes[lastStoredFieldIndex] =
                magneto.getLastRoundedReadings().clone();

        return lastStoredFieldIndex;
    }

    public boolean hasInitEnded()
    {
        return (lastStoredFieldIndex == storedMagneticFieldsAlongAxes.length-1);
    }

    public boolean doesCurrentlySensedMagneticFieldMatchAStoredOne()
    {
        float[] currentMagneticField = magneto.getLastRoundedReadings();
        for(int i=0; i<storedMagneticFieldsAlongAxes.length; i++)
        {
            if(doesStoredMatchCurrent(storedMagneticFieldsAlongAxes[i], currentMagneticField))
            {
                lastMatch = i;
                return true;
            }
        }
        return false;
    }

    /**
     * Note: it is <b>important</b> that the first argument is one of the float[3] representing
     * a magnetic field array stored during the initialisation.
     */
    private boolean doesStoredMatchCurrent(float[] stored, float[] current)
    {
        return (stored[0]>current[0]-threshold
                && stored[0]<current[0]+threshold
                && stored[1]>current[1]-threshold
                && stored[1]<current[1]+threshold
                && stored[2]>current[2]-threshold
                && stored[2]<current[2]+threshold);
    }

    public int getLastMatchedMagneticFieldIndex()
    {
        return lastMatch;
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

