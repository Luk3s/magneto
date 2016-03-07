package unipi.luk3s.magnetotest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import unipi.luk3s.magneto.IdentificationHelper;
import unipi.luk3s.magneto.PositionHelper;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startSelectedActivity(View view)
    {
        int viewId = view.getId();

        Intent intent = null;
        switch (viewId)
        {
            case R.id.MainActivity_button_startIdentificationExampleActivity:
                intent = new Intent(this, IdentificationExampleActivity.class);
                intent.putExtra(IdentificationHelper.NUMBER_OF_MAGNETS, 3);
                intent.putExtra(IdentificationHelper.THRESHOLD, 1.83F);
                break;
            case R.id.MainActivity_button_startPositionExampleActivity:
                intent = new Intent(this, PositionExampleActivity.class);
                intent.putExtra(PositionHelper.NUMBER_OF_POSITIONS, 2);
                intent.putExtra(PositionHelper.THRESHOLD, 2);
                break;
            case R.id.MainActivity_button_startLinearMotionExampleActivity:
                intent = new Intent(this, LinearMotionExampleActivity.class);
                // It does not need additional information
                break;
            case R.id.MainActivity_button_startVirtualTapExampleActivity:
                intent = new Intent(this, VirtualTapExampleActivity.class);
                // It does not need additional information
                break;
            case R.id.MainActivity_button_startRadialPositionExampleActivity:
                intent = new Intent(this, RadialPositionExampleActivity.class);
                // It does not need additional information
                break;
            case R.id.MainActivity_button_startRapidMovementsExampleActivity:
                intent = new Intent(this, RapidChangesExampleActivity.class);
                // It does not need additional information
                break;
            case R.id.MainActivity_button_startDisplaySensorDataActivity:
                intent = new Intent(this, DisplaySensorDataActivity.class);
                // It does not need additional information
                break;
            case R.id.MainActivity_button_startCollectSensorDataActivity:
                intent = new Intent(this, CollectSensorDataActivity.class);
                // It does not need additional information
                break;
            case R.id.MainActivity_button_startRadialCubeActivity:
                intent = new Intent(this, RadialCubeActivity.class);
                // It does not need additional information
                break;
        }

        if(intent != null)
            startActivity(intent);
    }
}
