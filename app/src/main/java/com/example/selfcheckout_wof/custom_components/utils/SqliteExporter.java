package com.example.selfcheckout_wof.custom_components.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.selfcheckout_wof.custom_components.exceptions.DataExportException;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Can export an sqlite databse into a csv file.
 *
 * The file has on the top dbVersion and on top of each table data the name of the table
 *
 * Inspired by
 * https://stackoverflow.com/questions/31367270/exporting-sqlite-database-to-csv-file-in-android
 * and some other SO threads as well.
 *
 */
public class SqliteExporter {
    private static final String TAG = SqliteExporter.class.getSimpleName();

    public static final String DB_BACKUP_DB_VERSION_KEY = "dbVersion";
    public static final String DB_BACKUP_TABLE_NAME = "table";

    /**
     * Exports SQLite db from the current Android app into a CSV file.
     *
     * @param context Application context of the current app.
     * @param db Database to export.
     * @return
     * @throws IOException
     */
    public static String export(Context context, SupportSQLiteDatabase db) throws DataExportException {
        if( !FileUtils.isExternalStorageWritable() ){
            throw new DataExportException("Cannot write to external storage");
        }
        File backupDir = FileUtils.createDirIfNotExist(FileUtils.getAppDir(context) + "/backup");
        String fileName = createBackupFileName();
        File backupFile = new File(backupDir, fileName);

        boolean success = false;
        try {
            success = backupFile.createNewFile();
        } catch (IOException exc) {
            throw new DataExportException("Failed to create the backup file. IOException thrown.");
        }

        if(!success){
            throw new DataExportException("Failed to create the backup file");
        }
        List<String> tables = getTablesOnDataBase(db);
        Log.d(TAG, "Started to fill the backup file in " + backupFile.getAbsolutePath());
        long starTime = System.currentTimeMillis();
        writeCsv(backupFile, db, tables);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Creating backup took " + (endTime - starTime) + "ms.");

        return backupFile.getAbsolutePath();
    }

    private static String createBackupFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "db_backup_" + sdf.format(new Date()) + ".csv";
    }

    /**
     * Get all the table names we have in db
     *
     * @param db
     * @return
     */
    public static List<String> getTablesOnDataBase(SupportSQLiteDatabase db){
        Cursor c = null;
        List<String> tables = new ArrayList<>();
        try{
            c = db.query("SELECT name FROM sqlite_master WHERE type='table'");
            if (c.moveToFirst()) {
                while ( !c.isAfterLast() ) {
                    tables.add(c.getString(0));
                    c.moveToNext();
                }
            }
        }
        catch(Exception throwable){
            Log.e(TAG, "Could not get the table names from db", throwable);
        }
        finally{
            if(c!=null)
                c.close();
        }
        return tables;
    }

    private static void writeCsv(File backupFile, SupportSQLiteDatabase db, List<String> tables){
        CSVWriter csvWrite = null;
        Cursor curCSV = null;
        try {
            csvWrite = new CSVWriter(new FileWriter(backupFile));
            writeSingleValue(csvWrite, DB_BACKUP_DB_VERSION_KEY + "=" + db.getVersion());
            for(String table: tables){
                writeSingleValue(csvWrite, DB_BACKUP_TABLE_NAME + "=" + table);
                curCSV = db.query("SELECT * FROM " + table);
                csvWrite.writeNext(curCSV.getColumnNames());
                while(curCSV.moveToNext()) {
                    int columns = curCSV.getColumnCount();
                    String[] columnArr = new String[columns];
                    for( int i = 0; i < columns; i++){
                        columnArr[i] = curCSV.getString(i);
                    }
                    csvWrite.writeNext(columnArr);
                }
            }
        }
        catch(Exception sqlEx) {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }finally {
            if(csvWrite != null){
                try {
                    csvWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if( curCSV != null ){
                curCSV.close();
            }
        }
    }

    private static void writeSingleValue(CSVWriter writer, String value){
        writer.writeNext(new String[]{value});
    }
}
