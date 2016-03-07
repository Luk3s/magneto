package unipi.luk3s.magneto;

import static unipi.luk3s.magneto.MagnetoUtils.computeAzimuth;
import static unipi.luk3s.magneto.MagnetoUtils.computeNorm;

/**
 * <p>To be used together with {@link MagnetoFragment}.
 * Helps in finding the position of a magnet that moves around the compass sensor in a 2D plane.</p>
 *
 * <p>If you want to get the radial position only (i.e. the position of the magnet on a circle 
 * centered around the compass sensor) no initialisation is needed. The function 
 * {@link #getRadialPosition()} takes care of computing the right value.</p>
 *
 * <p>It is possible to have a full 2D input by performing an initialisation, that is
 * calling {@link #setOverCompassValue()} and {@link #setFarAwayValue()}. Only after the
 * initialisation is over, the activity should request the magnet distance from the origin 
 * (the origin coincides with the position of the compass sensor on the device) by calling
 * {@link #getDistanceFromOrigin()}.</p>
 *
 * <p>For more information read Section 4.5.6 of
 * <a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/">
 *     Interacting with mobile devices using magnetic fields</a></p>
 */
public class RadialPositionHelper
{
    private final MagnetoFragment magneto;
    private float farAwayValue;
    private float overCompassValue;
    private boolean initialisationEnded;

    public  RadialPositionHelper(MagnetoFragment magneto)
    {
        initialisationEnded = false;
        this.magneto = magneto;
    }

    public void setFarAwayValue()
    {
        farAwayValue = computeNorm(magneto.getLastRoundedReadings());
    }

    public  void setOverCompassValue()
    {
        overCompassValue =  computeNorm(magneto.getLastRoundedReadings());
    }

    public void endInit()
    {
        initialisationEnded = true;
    }

    public boolean hasInitEnded()
    {
        return initialisationEnded;
    }

    /**
     * @return a float value in the set [0,1] that indicates how far from the compass sensor the
     * magnet is located (0 means close to the sensor | 1 means far from the device)
     */
    public float getDistanceFromOrigin()
    {
        float norm = computeNorm(magneto.getLastRoundedReadings());
        norm = (norm> overCompassValue)? overCompassValue :norm;
        norm = (norm< farAwayValue)? farAwayValue :norm;
        return (norm-overCompassValue)/(farAwayValue-overCompassValue);
    }

    /**
     * @return the angle describing the position of the magnet in a Cartesian coordinate system.
     *          The values are in the range [-180, 180] degrees
     */
    public float getRadialPosition()
    {
        return computeAzimuth(magneto.getLastRoundedReadings())*-1;
    }
}
