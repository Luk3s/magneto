package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import unipi.luk3s.magneto.MagnetoFragment;
import static unipi.luk3s.magneto.MagnetoUtils.computeNorm;
import static unipi.luk3s.magneto.MagnetoUtils.round;
import static unipi.luk3s.magneto.MagnetoUtils.magneticSensorEventValuesToString;

/**
 * <p>Stores values of the magnetic field along the axes sensed along some points in space
 * (for example while using a slider, you can save the magnetic field sensed along equidistant
 * points on the slider to study the correlation between values and positions).</p>
 *
 * <p>A <i>NumberPicker</i> is provided for selecting the number of points to be recorded.
 * It is possible to store fewer values than the one specified by the NumberPicker choice.</p>
 *
 * <p>A <i>ListView</i> shows the (temporary) stored values. It is possible to update an item
 * of the list by simply tapping on it.</p>
 *
 * <p>The values can be stored in the internal permanent memory as a CSV with the following
 * format (magnetic field along x, y, z, euclidean norm).</p>
 *
 * <p><b>Flow of control</b>: first choose the number of points to record, then store (some of) the points,
 * finally save the recorded points as CSV. This flow of control is ensured by enabling and
 * disabling the right buttons.</p>
 */
public class CollectSensorDataActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    // NumberPicker initialisation values
    private final int NUMBERPICKER_MAXVALUE = 20;
    private final int NUMBERPICKER_MINVALUE = 1;
    private final int NUMBERPICKER_DEFAULTVALUE = 3;

    private int numberOfPointsToRecord;

    private MagnetoFragment magneto;
    private Button recordAPosition_button;
    private Button saveToMemory_button;
    private TextView displaySensorData_textView;

    private ListView recordedPoints_listView;
    private ArrayAdapter<String> recordedPoints_arrayAdapter;
    private ArrayList<String> recordedPoints_arrayList_string;
    private ArrayList<float[]> recordedPoints_arrayList_float;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_sensor_data);

        recordAPosition_button =
                (Button) findViewById(R.id.CollectSensorDataActivity_button_recordPosition);
        saveToMemory_button =
                (Button) findViewById(R.id.CollectSensorDataActivity_button_saveToMemoryAsCsv);
        recordedPoints_listView =
                (ListView) findViewById(R.id.CollectSensorDataActivity_listView_recordedPositions);
        displaySensorData_textView =
                (TextView) findViewById(R.id.CollectSensorDataActivity_textView_dataDisplay);

        recordedPoints_listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                recordedPoints_arrayList_string.set(position, (position + 1) + ".   "
                        + magneticSensorEventValuesToString(magneto.getLastRoundedReadings(), ""));
                recordedPoints_arrayAdapter.notifyDataSetChanged();
                recordedPoints_listView.smoothScrollToPosition(position);
            }
        });

        NumberPicker pointsToRecord =
                (NumberPicker) findViewById(R.id.CollectSensorDataActivity_numberPicker);
        pointsToRecord.setMaxValue(NUMBERPICKER_MAXVALUE);
        pointsToRecord.setMinValue(NUMBERPICKER_MINVALUE);
        pointsToRecord.setValue(NUMBERPICKER_DEFAULTVALUE);
        pointsToRecord.setWrapSelectorWheel(false);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        magneto = MagnetoFragment.newInstance(true);
        magneto.setShowAccuracyToast(true);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(magneto, "MagnetoFragment");
        fragmentTransaction.commit();

        getSupportActionBar().setTitle(R.string.CollectSensorDataActivity_title);
    }

    /**
     * <p>Adds the last sensed magnetic field values along the axes (retrieved by calling
     * {@link MagnetoFragment#getLastRoundedReadings()}) both in the ListView as a String,
     * and as a <i>float[3]</i> array.</p>
     *
     * <p>This method should be called only after having chosen the number of points to record.</p>
     */
    public void recordAPosition(View view)
    {
        // If the max number of positions has been stored, disable the button invoking the method.
        if(recordedPoints_arrayList_string.size() >= numberOfPointsToRecord -1)
            recordAPosition_button.setEnabled(false);

        // 1. (34.2, -14.06, 51.88) µT
        recordedPoints_arrayList_string.add((recordedPoints_arrayList_string.size() + 1)
                        + ".   " + magneticSensorEventValuesToString(magneto.getLastRoundedReadings(), "")
        );

        // Clone is necessary because the magneto fragment does not create a new object
        // every time a sensor event is fired (the same array is updated)
        recordedPoints_arrayList_float.add(magneto.getLastRoundedReadings().clone());
        recordedPoints_arrayAdapter.notifyDataSetChanged();
        recordedPoints_listView.smoothScrollToPosition(recordedPoints_arrayList_string.size());
    }

    public void setNumberOfPoints(View view)
    {
        NumberPicker numberPicker =
                (NumberPicker) findViewById(R.id.CollectSensorDataActivity_numberPicker);
        numberOfPointsToRecord = numberPicker.getValue();
        numberPicker.setEnabled(false);
        view.setEnabled(false);

        saveToMemory_button.setEnabled(true);
        recordAPosition_button.setEnabled(true);

        recordedPoints_arrayList_float = new ArrayList<>();
        recordedPoints_arrayList_string = new ArrayList<>();
        recordedPoints_arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, recordedPoints_arrayList_string);
        recordedPoints_listView.setAdapter(recordedPoints_arrayAdapter);
    }

    /**
     * <p>Creates the CSV file from the stored magnetic field values in the correct format. It can
     * be called even if no values have been stored. It should be called only after having
     * chosen the number of points to record.</p>
     */
    public void storeAsCsv(View view)
    {
        // get the path to sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/magnetometer-readings/");
        // create this directory if not already created
        dir.mkdir();
        // create the file in which we will write the contents
        File file = new File(dir.getPath(), "recorded"
                + recordedPoints_arrayList_float.size()
                + "PointsOn"
                + new SimpleDateFormat("dd-M-yyyy hh:mm:ss").format(new Date()) + ".csv");
        String data = "x,y,z,norm\n";

        for(float[] aRecord : recordedPoints_arrayList_float)
        {
            data+= aRecord[0] + ", " + aRecord[1] + "," + aRecord[2] + "," +
                    round(computeNorm(aRecord)) + "\n";
        }

        try
        {
            FileOutputStream os = new FileOutputStream(file);
            os.write(data.getBytes());
            os.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Error while saving the file", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Successfully saved!", Toast.LENGTH_SHORT).show();
        saveToMemory_button.setEnabled(false);
        recordAPosition_button.setEnabled(false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString){}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        displaySensorData_textView.setText(magneticSensorEventValuesToString(smaRoundedValues, null));
    }
}