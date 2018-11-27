package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenshotUtils {


    public static Bitmap getScreenShot(View view){
        View screeView = view.getRootView();
        screeView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screeView.getDrawingCache());
        screeView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static File getMainDirectoryName(Context context){
        File mainDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), "AM");

        if (!mainDir.exists()){
            if (mainDir.mkdir())
                Log.e("Create Direcotry", "Main Direcory Created: " + mainDir);
        }
        return mainDir;
    }

    public static File storeScreenShot(Bitmap bitmap, String filename, File savedFilePath){
        File dir = new File(savedFilePath.getAbsolutePath());
        if (!dir.exists())
            dir.mkdir();
        File file = new File(savedFilePath.getAbsolutePath(), filename);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return file;
    }


}
