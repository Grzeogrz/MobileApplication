package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.ar.core.Pose;

import java.util.List;

public class DistanceMeasurment extends Activity {

    private static TextView positionTextView;
    private static final String DISTANCE_EQUALS = "Distance in meters: ";

    void measureDistance(List<Pose> listOfPoses){
        Double distance = getDistance(listOfPoses);
        positionTextView = findViewById(R.id.position);
        positionTextView.setText(DISTANCE_EQUALS + distance);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public Double getDistance(List<Pose> listOfPoses){
        if (listOfPoses.size() >= 2) {
            Pose poseOne = listOfPoses.get(listOfPoses.size()-2);
            Pose poseTwo = listOfPoses.get(listOfPoses.size()-1);

            Float poseOneTx = poseOne.tx();
            Float poseOneTy = poseOne.ty();
            Float poseOneTz = poseOne.tz();

            Float poseTwoTx = poseTwo.tx();
            Float poseTwoTy = poseTwo.ty();
            Float poseTwoTz = poseTwo.tz();

            Float distance = (poseTwoTx - poseOneTx) * (poseTwoTx - poseOneTx) +
                    (poseTwoTy - poseOneTy) * (poseTwoTy - poseOneTy) +
                    (poseTwoTz - poseOneTz) * (poseTwoTz - poseOneTz);
            return Math.sqrt(distance)*100;
        }
        return 0d;

    }

}
