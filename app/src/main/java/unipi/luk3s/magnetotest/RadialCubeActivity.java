package unipi.luk3s.magnetotest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import unipi.luk3s.magneto.MagnetoFragment;
import static unipi.luk3s.magneto.MagnetoUtils.computeAzimuth;

/**
 * <p>A basic usage of the {@link unipi.luk3s.magneto.MagnetoFragment} and the {@link
 * unipi.luk3s.magneto.RadialPositionHelper}.
 * Displays a 3D cube and rotates it using as input the radial position of a magnet around the
 * compass sensor.</p>
 */
public class RadialCubeActivity extends AppCompatActivity
        implements MagnetoFragment.MagneticSensorEventListener
{
    private float azimuth;

    public class DemoRenderer implements Renderer
    {
        private Cube cube = new Cube();

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
            // Depth buffer setup.
            gl.glClearDepthf(1.0f);
            // Enables depth testing.
            gl.glEnable(GL10.GL_DEPTH_TEST);
            // The type of depth testing to do.
            gl.glDepthFunc(GL10.GL_LEQUAL);
            // Really nice perspective calculations.
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height)
        {
            // Sets the current view port to the new size.
            gl.glViewport(0, 0, width, height);
            // Select the projection matrix
            gl.glMatrixMode(GL10.GL_PROJECTION);
            // Reset the projection matrix
            gl.glLoadIdentity();
            // Calculate the aspect ratio of the window
            GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
            // Select the modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            // Reset the modelview matrix
            gl.glLoadIdentity();
        }

        @Override
        public void onDrawFrame(GL10 gl)
        {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, 0.0f, -10.0f);
            // RadialCube explanation 1/2
            // The first argument specifies how many degrees you want to rotate around an axis.
            // The other three let you specify the rotation axis.
            gl.glRotatef(azimuth, 1.0f, 1.0f, 1.0f);
            cube.draw(gl);
            gl.glLoadIdentity();
        }
    }

    class Cube
    {
        private FloatBuffer vertexBuffer;  // Buffer for vertex-array
        private int numFaces = 6;

        // Vertices of the 6 faces
        private float[] vertices =
                {   // FRONT
                    -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
                    1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
                    -1.0f,  1.0f,  1.0f,  // 2. left-top-front
                    1.0f,  1.0f,  1.0f,  // 3. right-top-front
                    // BACK
                    1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
                    -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
                    1.0f,  1.0f, -1.0f,  // 7. right-top-back
                    -1.0f,  1.0f, -1.0f,  // 5. left-top-back
                    // LEFT
                    -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
                    -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
                    -1.0f,  1.0f, -1.0f,  // 5. left-top-back
                    -1.0f,  1.0f,  1.0f,  // 2. left-top-front
                    // RIGHT
                    1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
                    1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
                    1.0f,  1.0f,  1.0f,  // 3. right-top-front
                    1.0f,  1.0f, -1.0f,  // 7. right-top-back
                    // TOP
                    -1.0f,  1.0f,  1.0f,  // 2. left-top-front
                    1.0f,  1.0f,  1.0f,  // 3. right-top-front
                    -1.0f,  1.0f, -1.0f,  // 5. left-top-back
                    1.0f,  1.0f, -1.0f,  // 7. right-top-back
                    // BOTTOM
                    -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
                    1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
                    -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
                    1.0f, -1.0f,  1.0f   // 1. right-bottom-front
                };

        // Colors of the 6 faces
        private float[][] colors =
                {
                    {1.0f, 0.5f, 0.0f, 1.0f},  // 0. orange
                    {1.0f, 0.0f, 1.0f, 1.0f},  // 1. violet
                    {0.0f, 1.0f, 0.0f, 1.0f},  // 2. green
                    {0.0f, 0.0f, 1.0f, 1.0f},  // 3. blue
                    {1.0f, 0.0f, 0.0f, 1.0f},  // 4. red
                    {1.0f, 1.0f, 0.0f, 1.0f}   // 5. yellow
                };

        private byte indices[] =
                {
                    0, 4, 5, 0, 5, 1,
                    1, 5, 6, 1, 6, 2,
                    2, 6, 7, 2, 7, 3,
                    3, 7, 4, 3, 4, 0,
                    4, 7, 6, 4, 6, 5,
                    3, 0, 1, 3, 1, 2
                };

        public Cube()
        {
            // Setup vertex-array buffer. Vertices in float. An float has 4 bytes
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder()); // Use native byte order
            vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
            vertexBuffer.put(vertices);         // Copy data into buffer
            vertexBuffer.position(0);           // Rewind
        }

        public void draw(GL10 gl)
        {
            gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
            gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
            gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

            // Render all the faces
            for (int face = 0; face < numFaces; face++)
            {
                // Set the color for each of the faces
                gl.glColor4f(colors[face][0], colors[face][1], colors[face][2], colors[face][3]);
                // Draw the primitive from the vertex-array directly
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face*4, 4);
            }
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisable(GL10.GL_CULL_FACE);
        }
    }

    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        GLSurfaceView view = new GLSurfaceView(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view.setRenderer(new DemoRenderer());
        setContentView(view);

        // In order to sense magnetic changes you need to add this UI-less fragment
        // and implement the related MagneticSensorEventListener
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(MagnetoFragment.newInstance(true), "MagnetoFragment");
        fragmentTransaction.commit();

        getSupportActionBar().setTitle(R.string.RadialCubeActivity_title);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy, String accuracyToString){}

    public void onSensorChanged(SensorEvent sensorEvent, float[] smaValues, float[] smaRoundedValues, float[] highPassValues)
    {
        // RadialCube explanation 2/2
        // Rotating the cube as the magnet moves around the compass sensor is achieved by
        // updating the first argument of glRotatef(angle, x, y, z) as the magnet moves.
        // A simple assignment does the job, no further computation is required.
        azimuth = computeAzimuth(sensorEvent.values);
    }

}