package unipi.luk3s.magneto;

import static unipi.luk3s.magneto.MagnetoUtils.computeAzimuth;

/**
 * <p>To be used together with {@link MagnetoFragment}.
 * Helps in detecting a virtual tap.</p>
 *
 * <p>The initialisation consists in storing an initial value of the azimuth. After the
 * the initialisation has ended, the helper cab be called to detect taps using the
 * {@link #hasTapBeenDetected()} </p>
 *
 * <p>For more information read Section 4.5.6 of
 * <a href="http://etd.adm.unipi.it/theses/available/etd-11152015-012617/">
 *     Interacting with mobile devices using magnetic fields</a></p>
 */
public class VirtualTapHelper
{
    private final MagnetoFragment magneto;
    private float lastStoredAzimuth;
    private boolean initialisationEnded;
    
    public VirtualTapHelper(MagnetoFragment magneto)
    {
        initialisationEnded = false;
        this.magneto = magneto;
    }

    public void init()
    {
        lastStoredAzimuth = computeAzimuth(magneto.getLastRoundedReadings());
        initialisationEnded = true;
    }

    public boolean hasInitEnded()
    {
        return initialisationEnded;
    }

    public boolean hasTapBeenDetected()
    {
        float currentAzimuth = computeAzimuth(magneto.getLastRoundedReadings());

        // a tap is recognised when two azimuths have opposite signs
        // if the sings are opposite the xor gives a negative number
        if((((int)lastStoredAzimuth)^((int)currentAzimuth))<0)
        {
            lastStoredAzimuth = currentAzimuth;
            return true;
        }

        return false;
    }
}
