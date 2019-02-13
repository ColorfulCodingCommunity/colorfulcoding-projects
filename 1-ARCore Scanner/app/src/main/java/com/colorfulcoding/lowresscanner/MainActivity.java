package com.colorfulcoding.lowresscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.PointCloud;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;

import org.w3c.dom.Text;

import java.nio.FloatBuffer;

public class MainActivity extends AppCompatActivity {

    private final String TAG="LOW_RES_SCANNER";
    private ArFragment fragment;
    private TextView debugText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        findViewById(R.id.test_but).setOnClickListener(this::testingMethod);
        debugText = (TextView) findViewById(R.id.text_debug);
    }

    private void testingMethod(View v){
        if(fragment.getArSceneView().getSession() == null){
            Toast.makeText(this, "No session found", Toast.LENGTH_SHORT);
            return;
        }

        if(fragment.getArSceneView().getArFrame() == null){
            Toast.makeText(this, "No frame found!", Toast.LENGTH_SHORT);
        }

        PointCloud pC = fragment.getArSceneView().getArFrame().acquirePointCloud();
        //X Y Z confidence
        //84-604
        FloatBuffer points = pC.getPoints();

        Log.i(TAG, "" + points.limit());
        debugText.setText("" + points.limit());

        PointCloudNode pcNode = new PointCloudNode(getApplicationContext());
        fragment.getArSceneView().getScene().addChild(pcNode);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            PointCloud pc = fragment.getArSceneView().getArFrame().acquirePointCloud();
            pcNode.update(pc);
        });

    }


}
