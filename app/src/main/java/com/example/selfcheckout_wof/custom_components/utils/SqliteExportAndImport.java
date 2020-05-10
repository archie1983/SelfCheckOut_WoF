package com.example.selfcheckout_wof.custom_components.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.selfcheckout_wof.custom_components.exceptions.DataImportExportException;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class SqliteExportAndImport {
    private static final String TAG = SqliteExportAndImport.class.getSimpleName();

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
    public static String export(Context context, SupportSQLiteDatabase db) throws DataImportExportException {
        File backupDir = StorageHelper.getBackupDir(true, context);
        String fileName = createBackupFileName();
        File backupFile = new File(backupDir, fileName);

        boolean success = false;
        try {
            success = backupFile.createNewFile();
        } catch (IOException exc) {
            throw new DataImportExportException("Failed to create the backup file. IOException thrown.");
        }

        if(!success){
            throw new DataImportExportException("Failed to create the backup file");
        }
        List<String> tables = getTablesOnDataBase(db);
        Log.d(TAG, "Started to fill the backup file in " + backupFile.getAbsolutePath());
        long starTime = System.currentTimeMillis();
        writeCsv(backupFile, db, tables);
        long endTime = System.currentTimeMillis();
        Log.d(TAG, "Creating backup took " + (endTime - starTime) + "ms.");

        MediaScannerConnection.scanFile(context, new String[] {backupFile.getAbsolutePath()}, null, null);

        return backupFile.getAbsolutePath();
    }

    private static String createBackupFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "self_checkout_db_backup_" + sdf.format(new Date()) + ".csv";
    }

    /**
     * Get all the table names we have in db
     *
     * @param db
     * @return
     */
    public static List<String> getTablesOnDataBase(SupportSQLiteDatabase db){
        List<String> tables = new ArrayList<>();
        tables.add("SalesItems");

        /**
         * The below code will return all tables in the database, but we may not
         * need all of them. If we don't, then the variable "tables" above should
         * be explicitly initialised with an arraylist that contains the names of
         * the required tables.
         */
//        Cursor c = null;
//        try{
//            c = db.query("SELECT name FROM sqlite_master WHERE type='table'");
//            if (c.moveToFirst()) {
//                while ( !c.isAfterLast() ) {
//                    tables.add(c.getString(0));
//                    c.moveToNext();
//                }
//            }
//        }
//        catch(Exception throwable){
//            Log.e(TAG, "Could not get the table names from db", throwable);
//        }
//        finally{
//            if(c!=null)
//                c.close();
//        }
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

    /**
     * Imports data from a CSV file into the database.
     *
     * @param context
     * @param db
     */
    public static void importData(Context context, SupportSQLiteDatabase db) {
        try {
            File[] files = StorageHelper.getBackupDir(false, context).listFiles();
            File csv_data = null;
            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                if (files[i].getName().endsWith(".csv")) {
                    csv_data = files[i];
                }
            }

            if (csv_data != null) {
                readCSVData(csv_data, db);
            }

        } catch (DataImportExportException exc) {
            Log.d("Files", exc.toString());
        }
    }

    /**
     * Reads in CSV file and prepares SQL statements with the data.
     *
     */
    private static void readCSVData(File csvFile, SupportSQLiteDatabase db) {
        //String csvFileNameToImport = "csv/twoColumn.csv";
        Reader reader = null;
        CSVReader csvReader = null;

        try {
            //Reader reader = new BufferedReader(new FileReader(csvFileNameToImport));
            reader = new BufferedReader(new FileReader(csvFile));
            csvReader = new CSVReader(reader);

            /*
             * Reading Records One by One in a String array and generating SQL insert statements
             * to put that data into the DB.
             */
            String[] nextRecord;
            boolean salesItemsRecords = false;
            boolean tableHeaderRead = false;
            String tableHeaderSQL = "", insertSQL = "";
            int maxSalesItemsID = 0;

            while ((nextRecord = csvReader.readNext()) != null) {
                /*
                 * Catching the start of the SalesItems table and the end of it.
                 */
                if (nextRecord.length >= 1 && nextRecord[0].equals("table=SalesItems")) {
                    salesItemsRecords = true;
                    tableHeaderRead = false;
                    db.execSQL("delete from SalesItems");
                    continue;
                } else if(nextRecord.length >= 1 && nextRecord[0].length() >= 6 && nextRecord[0].substring(0, 5).equals("table=")) {
                    salesItemsRecords = false;
                    tableHeaderRead = false;
                }

                if (salesItemsRecords && nextRecord.length == 8) {
                    /*
                     * Reading the table header and creating the first part of the SQL query.
                     */
                    if (!tableHeaderRead) {
                        tableHeaderSQL = "insert into SalesItems (";
                        for (String columnName : nextRecord) {
                            tableHeaderSQL += columnName + ", ";
                        }
                        tableHeaderSQL = tableHeaderSQL.substring(0, tableHeaderSQL.length() - 2) + ") values (";
                        tableHeaderRead = true;
                    } else {
                        insertSQL = tableHeaderSQL;

                        for (String columnValue : nextRecord) {
                            insertSQL += "\"" + columnValue + "\", ";
                        }
                        insertSQL = insertSQL.substring(0, insertSQL.length() - 2) + ")";

                        db.execSQL(insertSQL);
                        System.out.println(insertSQL);

                        try {
                            if (maxSalesItemsID < Integer.parseInt(nextRecord[0])) {
                                maxSalesItemsID = Integer.parseInt(nextRecord[0]);
                            }
                        } catch (NumberFormatException exc) {
                            Log.e(TAG, exc.getMessage(), exc);
                        }
                    }
                }
            }

            /*
             * Now the last step - to update the sequence of the table. And to make sure that the
             * sequence record is there even if it wasn't before, we'll do a delete and then
             * insert instead of an update statement.
             */
            db.execSQL("delete from sqlite_sequence where name='SalesItems'");
            db.execSQL("insert into sqlite_sequence (name, seq) values('SalesItems', " + maxSalesItemsID + ")");
        } catch (java.io.IOException exc) {
            Log.e(TAG, exc.getMessage(), exc);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (java.io.IOException exc) {
                    exc.printStackTrace();
                }
            }
        }
    }
}
