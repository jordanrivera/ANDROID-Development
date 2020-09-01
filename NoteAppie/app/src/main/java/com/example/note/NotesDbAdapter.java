package com.example.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simpele noteappie database access helper class. Definieert de basis CRUD operaties
 * en geeft de mogelijkheid om een ​​lijst van alle aantekeningen
 * Op te halen of een specifieke aantekening te wijzigen.
 * Dit is verbeterd ten opzichte van de eerste versie van deze tutorial door de
 * toevoeging van een betere foutafhandeling en ook met behulp van het retourneren van een Cursor in de plaats van, 
 * het gebruiken van een verzameling van inwendige klassen (die minder schaalbaar en niet
 * aanbevolen is).
 */

/* Het bevat originele code van de notepad oefening, maar
 * ik heb het iets uitgebreid met de date parameter.  
 */

public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_DATE = "date";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creatie sql statement
     */
    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "title text not null, body text not null, date text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - neemt de context om de database te mogen
     * openen of te worden aangemaakt.
     * 
     * @param ctx the Context waar ingewerkt wordt
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open de notes database. Als het niet geopend kan worden, probeert een nieuwe instance van database
     * aan te maken. Als het niet aangemaakt kna worden, throw een exception 
     * om de fout te laten zien.
     * @return this (Automatische zelf verwijzing, waardoor deze geketend wordt in een
     * initialization call)
     * @throws SQLException als de database niet kan worden geopend of gecreëerd
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Maak een nieuwe notitie met behulp van de titel en body. Als de notitie
     * met succec is gecreerd return de nieuwe rowId voor die notitie, anders return
     * een -1 om een mislukking oan te geven.
     * 
     * @param title de title van de notitie
     * @param body de body van de notitie
     * @return rowId of -1 als het mislukt
     */
    public long createNote(String title, String body, String date) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, date);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Verwijder de notitie met de gegeven rowId
     * 
     * @param rowId id van de notitie om te verwijderen
     * @return true als het verwijder is, anders false 
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return van een cursor over de lijst van alle notities in de database
     * 
     * @return Cursor over alle notities
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY,KEY_DATE}, null, null, null, null, null);
    }

    /**
     * Return een cursor gepositioneerd op de notitie die overeenkomt met de opgegeven rowId
     * 
     * @param rowId id van de op te halen notitie
     * @return Cursor gepositioneerd op de gvraagde notitie, als het gevonden is
     * @throws SQLException als notitie niet gevonden of opgehaald kan worden.
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_BODY,KEY_DATE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update de notitie met de gegeven gegevens. Wanneer de notie wordt bijgewerkt,
     * wordt dat gedaan door de rowId te specificeren, en wordt gewijzigd door de 
     * aangegeven titel en body.
     * 
     * @param rowId id van de notitie om bij te werken
     * @param title waarde om de notitie titel te geven
     * @param body waarde om de nititie body te geven
     * @return true als de notite met succes bijgewerkt is, anders false
     */
    public boolean updateNote(long rowId, String title, String body,String date) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        
        // Waarde voor de datum
        args.put(KEY_DATE, date);
        
        //Een  parameter meer voor de datum
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
