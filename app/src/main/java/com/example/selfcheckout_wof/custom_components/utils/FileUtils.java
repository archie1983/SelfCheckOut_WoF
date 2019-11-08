package com.example.selfcheckout_wof.custom_components.utils;

import android.content.Context;
import android.os.Environment;

import com.example.selfcheckout_wof.R;

import java.io.File;

/**
 * A utility class for filesystem access.
 */
public class FileUtils {

    /**
     * Retursn the directory of the application specified by its context in the parameter.
     *
     * @param applicationContext
     * @return
     */
    public static String getAppDir(Context applicationContext){
        //Environment.getExternalStorageDirectory();
        return applicationContext.getExternalFilesDir(null) + "/" + applicationContext.getString(R.string.app_name);
    }

    public static File createDirIfNotExist(String path){
        File dir = new File(path);
        if( !dir.exists() ){
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}