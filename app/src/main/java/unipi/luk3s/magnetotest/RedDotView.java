package unipi.luk3s.magnetotest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Additional View showing a Cartesian coordinate system and a red dot on it.
 */
public class RedDotView extends View
{

    private Paint paint_circle;
    private Paint paint_rectangle;
    private Paint paint_text;
    private Paint paint_redDot;

    private final int strokeWidth = 8;
    public final int maxCircleRadius = 250;
    public final int minCircleRadius = 20;
    private int currentCircleRadius = 200;

    private float redDotX;
    private float redDotY;
    private float redDotRadius;

    public RedDotView(Context context)
    {
        super(context);
        init();
    }

    public RedDotView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public RedDotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    private void init()
    {
        paint_circle = new Paint();
        paint_circle.setAntiAlias(true);
        paint_circle.setStyle(Paint.Style.STROKE);
        paint_circle.setStrokeWidth(strokeWidth);
        paint_circle.setColor(Color.DKGRAY);

        paint_rectangle = new Paint();
        paint_rectangle.setAntiAlias(true);
        paint_rectangle.setStyle(Paint.Style.FILL);
        paint_rectangle.setColor(Color.LTGRAY);

        paint_text = new Paint();
        paint_text.setAntiAlias(true);
        paint_text.setStyle(Paint.Style.FILL);
        paint_text.setTextSize(40);
        paint_text.setColor(Color.DKGRAY);

        paint_redDot = new Paint();
        paint_redDot.setAntiAlias(true);
        paint_redDot.setStyle(Paint.Style.FILL_AND_STROKE);
        paint_redDot.setStrokeWidth(20);
        paint_redDot.setColor(Color.RED);

        redDotX = -100;
        redDotY = -100;
        redDotRadius = 10;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // topleftx,toplefty,bottomrightx,bottomrighty
        canvas.drawRect(height / 2 - 5, 0, height / 2 + 5, height, paint_rectangle);
        canvas.drawRect(0, height / 2 - 5, width, height / 2 + 5, paint_rectangle);

        canvas.drawCircle(width / 2, height / 2, currentCircleRadius, paint_circle);

        canvas.drawText("0", width - 35, height / 2 - 10, paint_text);
        canvas.drawText("180", 0, height / 2 - 10, paint_text);
        canvas.drawText("90", width / 2 - 22, 40, paint_text);
        canvas.drawText("-90", width / 2 - 32, height - 10, paint_text);

        canvas.drawCircle(redDotX, redDotY, redDotRadius, paint_redDot);
    }

    public void setRedDot(float angle)
    {
        // Get the coordinates from the angle relative to the circle of circleRadius radius
        redDotX = (float) (currentCircleRadius * Math.cos(Math.toRadians(angle)));
        redDotY = (float) (currentCircleRadius * Math.sin(Math.toRadians(angle)));

        // Adjust the coordinates to set the correct origin point (the center of the circle)
        redDotX += getWidth()/2;
        redDotY += getHeight()/2;
    }

    public void setRadius(int radius)
    {
        if(radius<minCircleRadius)
            radius = minCircleRadius;
        else
            if(radius>maxCircleRadius)
                radius = maxCircleRadius;

        currentCircleRadius = radius;
    }

}
