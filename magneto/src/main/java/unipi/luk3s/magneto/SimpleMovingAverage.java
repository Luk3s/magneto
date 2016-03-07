package unipi.luk3s.magneto;

/**
 * <p>A SMA (Simple moving average) implementation to compute the low-pass filter values of
 * the magnetic field along the three axes. For details about the algorithm used see:
 * <a href="http://en.wikipedia.org/wiki/Moving_average">
 *     http://en.wikipedia.org/wiki/Moving_average</a>.</p>
 *
 * <p>Every time new values from the compass sensor are available, the 
 * {@link #updateSMA(float,float,float)} method has to be called. 
 * To retrieve the resulting low-pass filtered values, different methods can be
 * called: {@link #getValues()}, {@link #getX()}, {@link #getY()}, or {@link #getZ()}.</p>
 *
 * <p>The SMA algorithm makes use of a window value (i.e. the number of raw values needed before a
 * correct filtered value can be computed). This implementation computes a SMA for each axis of the
 * magnetic field. To do so, a 2D-array is used to keep track of <i>sma_window * 3</i> values
 * (a total of sma_window values for each axis). The first index of the matrix discriminates
 * among the different buffers (one for each axis).</p>
 *
 * <p>The last available SMA of the magnetic field is always stored in {@link #average3D}</p>
 */
public class SimpleMovingAverage
{
    private float [][] circularBuffer;
    private float [] average3D;
    private int indexOfTheOldestElement;
    private boolean isBufferEmpty;

    private static final int smaWindow = 10;
    // Use these indexes to access the right buffer
    private static final int x = 0;
    private static final int y = 1;
    private static final int z = 2;

    public SimpleMovingAverage()
    {
        circularBuffer = new float[3][smaWindow];
        average3D = new float[3];
        isBufferEmpty = true;
        indexOfTheOldestElement = 0;
    }

    // Get the current moving average along the three axes
    public float[] getValues()
    {
        return average3D;
    }

    public float getX()
    {
        return average3D[x];
    }

    public float getY()
    {
        return average3D[y];
    }

    public float getZ()
    {
        return average3D[z];
    }

    /**
     * <p>Call this function every time new raw values are fired by the
     * {@link android.hardware.SensorManager}. When new values are added,
     * the current SMA is updated.</p>
     *
     * <p>The SMA window should be filled before a valid SMA can be computed. We avoid waiting for
     * SMA_window values by filling the buffers with the first values received.</p>
     *
     * @param xMagneticField the x coordinate of the magnetic field
     * @param yMagneticField the y coordinate of the magnetic field
     * @param zMagneticField the z coordinate of the magnetic field
     */
    public void updateSMA(float xMagneticField, float yMagneticField, float zMagneticField)
    {
        if (isBufferEmpty)
        {
            fillBufferWithValue(xMagneticField, x);
            fillBufferWithValue(yMagneticField, y);
            fillBufferWithValue(zMagneticField, z);
            isBufferEmpty = false;
        }
        else
        {
            updateSMA(xMagneticField, x);
            updateSMA(yMagneticField, y);
            updateSMA(zMagneticField, z);
            indexOfTheOldestElement = updateIndex(indexOfTheOldestElement);
        }
    }

    /**
     * <p>Add a new value (magnetic field along an axis) in a buffer (the buffer referring to the
     * axis of the value added). To do so the oldest value in the circular buffer is
     * removed and the SMA currently stored in {@link #average3D} for that axis is updated.</p>
     * @param newValue the value to add
     * @param whichBuffer the buffer to add the value to
     */
    private void updateSMA(float newValue, int whichBuffer)
    {
        float oldestValue = circularBuffer[whichBuffer][indexOfTheOldestElement];
        average3D[whichBuffer] = average3D[whichBuffer] +
                (newValue - oldestValue) / circularBuffer[whichBuffer].length;

        circularBuffer[whichBuffer][indexOfTheOldestElement] = newValue;
    }

    /**
     * <p>This method should be called only after one new value have been added to all the buffers.
     * The index is increased according to the circular buffer policy. It will point to the "next"
     * element in the buffer, i.e. the one that will be erased when a new value is available.</p>
     * @param currentIndex the index to increase
     * @return the index correctly increased
     */
    private int updateIndex(int currentIndex)
    {
        if (currentIndex + 1 >= circularBuffer[0].length)
            return 0;

        return currentIndex + 1;
    }

    /**
     * @param val the value to use for filling the appropriate buffer
     * @param whichBuffer the buffer to add the value to
     */
    private void fillBufferWithValue(float val, int whichBuffer)
    {
        for (int i = 0; i < circularBuffer[whichBuffer].length; ++i)
            circularBuffer[whichBuffer][i] = val;

        average3D[whichBuffer] = val;
    }
}