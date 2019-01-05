/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity implements View.OnClickListener{
  private static final String TAG = HelloSceneformActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;
  private static final String DISTANCE_EQUALS = "Distance in cm: ";

  private static float x_screen;
  private static float y_screen;

  private ArFragment arFragment;
  private ModelRenderable andyRenderable;

  private static ArrayList<Pose> listOfPoses = new ArrayList<>();
  DistanceMeasurment distanceMeasurment = new DistanceMeasurment();

  private TextView position = null;
  private Button screenShotButton = null;
  private Button takeScrenshotButton = null;
  private Button tapTheScreen = null;
  private ConstraintLayout rootContent = null;
  private SurfaceView mSurfaceView = null;

  public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

  ImageView imageToDisplay = null;

    private int PICK_IMAGE_REQUEST = 1;

    private static final String REQUIRED_PERMISSIONS[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)//R.raw.andy)
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (andyRenderable == null) {
            return;
          }
          //Create the session
            Session session = arFragment.getArSceneView().getSession();

          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

            listOfPoses.add(anchor.getPose());
            measureAndShow(listOfPoses);

            screenShotButton = findViewById(R.id.screenshot);
            screenShotButton.setOnClickListener(this);

            takeScrenshotButton = findViewById(R.id.takescreen);
            takeScrenshotButton.setOnClickListener(this);

            tapTheScreen = findViewById(R.id.tapbutton);
            tapTheScreen.setOnClickListener(this);

            imageToDisplay = findViewById(R.id.imageView);
            imageToDisplay.setAlpha(0.5f);
            imageToDisplay.setVisibility(View.INVISIBLE);
            imageToDisplay.setImageResource(R.drawable.screen);

            x_screen = motionEvent.getX();
            y_screen = motionEvent.getY();


          // Create the transformable andy and add it to the anchor.
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
          //andy.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 0, 1f), 90f));
          andy.setParent(anchorNode);
          andy.setRenderable(andyRenderable);
          andy.select();
        });
  }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }


  private void measureAndShow(List<Pose> listOfPoses){
      position = findViewById(R.id.position);
      Double distance = distanceMeasurment.getDistance(listOfPoses);
      position.setText(DISTANCE_EQUALS + Double.toString(distance));
  }

  @Override
  public void onClick(View v) {
      if (v.getId() == R.id.screenshot && imageToDisplay.getVisibility() == View.INVISIBLE){
          //takeScreenShot();
          ArSceneView arSceneView = arFragment.getArSceneView();
          //takePhoto(arSceneView);
          takeImage(v.getContext());
          imageToDisplay.setVisibility(View.VISIBLE);
      }else if (v.getId() == R.id.takescreen){
          ArSceneView arSceneView = arFragment.getArSceneView();
          takePhoto(arSceneView);
      }
      else if (v.getId() == R.id.tapbutton){
          tapScreen();
      }
      else
          imageToDisplay.setVisibility(View.INVISIBLE);

  }

  private void takeScreenShot(){
      Date now = new Date();
      android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

      try {
          // image naming and path  to include sd card  appending name you choose for file
          String mPath = Environment.getRootDirectory().toString() + "/" + now + ".jpg";

          // create bitmap screen capture
          View v1 = getWindow().getDecorView().getRootView();
          v1.setDrawingCacheEnabled(true);
          Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
          v1.setDrawingCacheEnabled(false);

          File imageFile = new File(mPath);

          FileOutputStream outputStream = new FileOutputStream(imageFile);
          int quality = 100;
          bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
          outputStream.flush();
          outputStream.close();

          openScreenshot(imageFile);
      } catch (Throwable e) {
          // Several error may come out with file handling or DOM
          e.printStackTrace();
      }
  }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

  private void takeImage(Context context) {
      if (checkPermissionREAD_EXTERNAL_STORAGE(context)) {
          String[] projection = new String[]{
                  MediaStore.Images.ImageColumns._ID,
                  MediaStore.Images.ImageColumns.DATA,
                  MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                  MediaStore.Images.ImageColumns.DATE_TAKEN,
                  MediaStore.Images.ImageColumns.MIME_TYPE,
                  MediaStore.Images.ImageColumns.DISPLAY_NAME
          };
          System.out.print(MediaStore.Images.ImageColumns.DISPLAY_NAME);
          ContentResolver contentResolver = context.getContentResolver();
          Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                  null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

//          Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.ImageColumns.DISPLAY_NAME + " LIKE 'screenshot.jpg'",
//                  null, null);

          if (cursor.moveToFirst()) {
              final ImageView imageView = (ImageView) findViewById(R.id.imageView);
              String imageLocation = cursor.getString(1);
              File imageFile = new File(imageLocation);
              if (imageFile.exists()) {   // TODO: is there a better way to do this?
                  Bitmap bm = BitmapFactory.decodeFile(imageLocation);
                  imageView.setImageBitmap(bm);
              }
          }
      }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            }catch (IOException exception){
                exception.printStackTrace();
            }
        }
  }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(getBaseContext(), "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

//    ///////////////
    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Screenshots/"  + "screenshot.png";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private void takePhoto(ArSceneView arSceneView) {
        final String filename = generateFilename();
        ArSceneView view = arSceneView;

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(arSceneView.getContext(), e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved", Snackbar.LENGTH_LONG);
                snackbar.setAction("Open in Photos", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(arSceneView.getContext(),
                            arSceneView.getContext().getPackageName() + ".ar.codelab.name.provider",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(arSceneView.getContext(),
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }


    private void tapScreen(){
        try {
            Runtime.getRuntime().exec("input tap " + x_screen +" "+ y_screen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
