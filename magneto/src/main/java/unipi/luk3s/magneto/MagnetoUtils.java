package unipi.luk3s.magneto;

/**
 * <p>Common methods useful when working with the compass sensor.</p>
 * <p><b>Note:</b> every time a parameter {@code float[] magneticSensorEventValues} is found,
 * the method expects an array of three floats representing the magnetic field
 * along x, y, and z, respectively. That is the one stored in the float array found in the
 * sensor event ({@code sensorEvent.values}) parameter of the {@code onSensorChanged} method.</p>
 */
public class MagnetoUtils
{
    /** Private constructor in order to prevent instantiation */
    private MagnetoUtils(){}

    /** Round a number to 2 decimal places */
    public static float round(double num)
    {
        return (float)Math.round(num*100)/100;
    }

    /**
     * @param magneticSensorEventValues Check the <i>Note</i> of the
     *               {@link MagnetoUtils MagnetoUtils} class.
     * @param unitOfMeasurement The unit of measurement used for the magnetic field,
     *                          if null, the default unit is µT.
     * @return A String representing the magnetic sensor event values with the following
     *          format (x, y, z) µT
     */
    public static String magneticSensorEventValuesToString(float[] magneticSensorEventValues,
                                                           String unitOfMeasurement)
    {
        return "("+ magneticSensorEventValues[0] + ", "
                  + magneticSensorEventValues[1] + ", "
                  + magneticSensorEventValues[2] + ") "
                  + (unitOfMeasurement==null?"µT":unitOfMeasurement);
    }


    public static float computeNorm(float[] magneticSensorEventValues)
    {
        float accum = 0;
        for (int i = 0; i < magneticSensorEventValues.length; i++)
            accum += magneticSensorEventValues[i]*magneticSensorEventValues[i];
        return (float) Math.sqrt(accum);
    }

    public static float computeRoundedNorm(float[] magneticSensorEventValues)
    {
        return round(computeNorm(magneticSensorEventValues));
    }


    public static float computeAzimuth(float[] magneticSensorEventValues)
    {
        return round(Math.toDegrees(
                     Math.atan2(magneticSensorEventValues[1], magneticSensorEventValues[0])));
    }
}
