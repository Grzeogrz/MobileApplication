//package com.google.ar.sceneform.samples.hellosceneform;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.provider.MediaStore;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.google.ar.sceneform.ArSceneView;
//
//import java.io.File;
//
//public class ExternalOnClickListener implements View.OnClickListener {
//
//    private ImageView imageToDisplay = null;
//
//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.screenshot && imageToDisplay.getVisibility() == View.INVISIBLE){
//            //takeScreenShot();
//            ArSceneView arSceneView = arFragment.getArSceneView();
//            //takePhoto(arSceneView);
//            takeImage(v.getContext());
//            imageToDisplay.setVisibility(View.VISIBLE);
//        }
//        else
//            imageToDisplay.setVisibility(View.INVISIBLE);
//
//        //takeScreenShot();
//    }
//    }
//
//    private void takeImage(Context context) {
////      Intent intent = new Intent();
////      intent.setType("image/*");
////      intent.setAction(Intent.ACTION_GET_CONTENT);
////      startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//        if (checkPermissionREAD_EXTERNAL_STORAGE(context)) {
//            String[] projection = new String[]{
//                    MediaStore.Images.ImageColumns._ID,
//                    MediaStore.Images.ImageColumns.DATA,
//                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
//                    MediaStore.Images.ImageColumns.DATE_TAKEN,
//                    MediaStore.Images.ImageColumns.MIME_TYPE
//            };
//            ContentResolver contentResolver = context.getContentResolver();
//            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
//                    null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
//
//            if (cursor.moveToFirst()) {
//                final ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                String imageLocation = cursor.getString(1);
//                File imageFile = new File(imageLocation);
//                if (imageFile.exists()) {   // TODO: is there a better way to do this?
//                    Bitmap bm = BitmapFactory.decodeFile(imageLocation);
//                    imageView.setImageBitmap(bm);
//                }
//            }
//        }
//    }
//}
