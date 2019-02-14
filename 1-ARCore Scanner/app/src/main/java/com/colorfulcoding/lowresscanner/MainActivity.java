package com.colorfulcoding.lowresscanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.PointCloud;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String TAG="LOW_RES_SCANNER";
    private ArFragment fragment;
    private TextView debugText;

    private WorldToScreenTranslator worldToScreenTranslator;

    private List<Float> positions3D;
    private List<Byte> colorsYUV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        findViewById(R.id.test_but).setOnClickListener(this::testingMethod);
        debugText = (TextView) findViewById(R.id.text_debug);

        worldToScreenTranslator = new WorldToScreenTranslator();

        positions3D = new ArrayList<>();
        colorsYUV = new ArrayList<>();
    }

    private void testingMethod(View v){
        if(fragment.getArSceneView().getSession() == null){
            Toast.makeText(this, "No session found", Toast.LENGTH_SHORT);
            return;
        }

        if(fragment.getArSceneView().getArFrame() == null){
            Toast.makeText(this, "No frame found!", Toast.LENGTH_SHORT);
        }

        //X Y Z confidence
        //84-604
        PointCloudNode pcNode = new PointCloudNode(getApplicationContext());
        fragment.getArSceneView().getScene().addChild(pcNode);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            PointCloud pc = fragment.getArSceneView().getArFrame().acquirePointCloud();
            pcNode.update(pc);

            try {
                FloatBuffer points = pc.getPoints();
                Log.i(TAG, "" + points.limit());

                getScreenPixel(new float[]{points.get(0), points.get(1), points.get(2)});
            } catch (NotYetAvailableException e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    void getScreenPixel(float[] worldPos) throws NotYetAvailableException {
        int[] dims = fragment.getArSceneView().getArFrame().getCamera().getImageIntrinsics().getImageDimensions();
        Image img = fragment.getArSceneView().getArFrame().acquireCameraImage();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        double[] pos2D = worldToScreenTranslator.worldToScreen(img.getWidth(), img.getHeight(), fragment.getArSceneView().getArFrame().getCamera(), worldPos);
        ByteBuffer yBuffer = img.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = img.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = img.getPlanes()[2].getBuffer();

        //TODO: Image -> get pixel color at pos2D[0], pos2D[1]

        //Otherwise the CPU will overload and crash
        img.close();

        positions3D.add(worldPos[0]);
        positions3D.add(worldPos[1]);
        positions3D.add(worldPos[2]);

        debugText.setText("" + positions3D.size());

        return;

    }
}
