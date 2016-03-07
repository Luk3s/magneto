package unipi.luk3s.magneto;

import static unipi.luk3s.magneto.MagnetoUtils.computeRoundedNorm;

/**
 * <p>To be used together with {@link MagnetoFragment}.
 * Helps in identifying all the positions of a magnet moving along a straight line.</p>
 *
 * <p>The initialisation consists in storing the magnitude sensed when the magnet is at the
 * endpoints of the straight line. Only after that the initialisation is over, the
 * activity should query the helper by calling {@link #getMagnetPosition()}. </p>
 *
 * <p>For more information read Section 4.5.3 of
 * <a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/">
 *     Interacting with mobile devices using magnetic fields</a></p>
 */
public class LinearMotionHelper
{
    private final MagnetoFragment magneto;
    private float[] storedMagnitudes;
    private int lastStoredMagnitudeIndex;

    public LinearMotionHelper(MagnetoFragment magneto)
    {
        storedMagnitudes = new float[2];
        lastStoredMagnitudeIndex = -1;
        this.magneto = magneto;
    }

    public void storeMagnitude()
    {
        if(hasInitEnded())
            return;

        lastStoredMagnitudeIndex++;
        storedMagnitudes[lastStoredMagnitudeIndex] =
                computeRoundedNorm(magneto.getLastRoundedReadings());

        return;
    }

    public boolean hasInitEnded()
    {
        return (lastStoredMagnitudeIndex == 1);
    }
    
    /**
     * @return a float value in the set [0,1] that indicates how far from the compass sensor the
     * magnet is located. A zero means the magnet is close to the first endpoint stored, while
     * a one means the magnet is close to the second endpoint stored.
     */
    public float getMagnetPosition()
    {
        float currentNorm = computeRoundedNorm(magneto.getLastRoundedReadings());
        float position = (currentNorm-storedMagnitudes[0])/(storedMagnitudes[1]-storedMagnitudes[0]);
        return Math.abs(position);
    }
}
