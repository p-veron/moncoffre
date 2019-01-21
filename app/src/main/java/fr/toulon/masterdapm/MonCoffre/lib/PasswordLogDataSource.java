package fr.toulon.masterdapm.MonCoffre.lib;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.DatabaseUtils;
import android.util.Log;

public class PasswordLogDataSource {

  private SQLiteDatabase database;
  private DatabaseManager dbHelper;
  private String[] allColumns = { DatabaseManager.COLUMN_SITE_NAME,
      DatabaseManager.COLUMN_CRYPTO };

  public PasswordLogDataSource(Context context) {
    dbHelper = new DatabaseManager(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public boolean insert(PasswordLog passwordLog) {
    ContentValues values = new ContentValues();
 //   Log.d("fr.toulon.masterdapm.fr.toulon.masterdapm.fr.toulon.masterdapm.MonCoffre","insert "+passwordLog.getSiteName()+" "+Base64.encodeToString(passwordLog.getCrypto(),Base64.DEFAULT|Base64.NO_WRAP|Base64.NO_PADDING));
    values.put(DatabaseManager.COLUMN_SITE_NAME, passwordLog.getSiteName());
    values.put(DatabaseManager.COLUMN_CRYPTO, passwordLog.getCrypto());
    long insertId = database.insert(DatabaseManager.TABLE_PASSWORDS, null,values);
    return insertId > 0;
  }
  
  public void update(String site,PasswordLog passwordLog) {

	  ContentValues values= new ContentValues();
	    values.put(DatabaseManager.COLUMN_SITE_NAME,passwordLog.getSiteName());
	    values.put(DatabaseManager.COLUMN_CRYPTO,passwordLog.getCrypto());
	    Log.d("masterdapm.MonCoffre", "PasswordLog updated with id: " + site);
	    database.update(DatabaseManager.TABLE_PASSWORDS, values, DatabaseManager.COLUMN_SITE_NAME+"=\""+site+"\"", null);
	  }

  public void delete(PasswordLog passwordLog) {
    String id = passwordLog.getSiteName();
    database.delete(DatabaseManager.TABLE_PASSWORDS, DatabaseManager.COLUMN_SITE_NAME
        + " = \"" + id + "\"", null);
    Log.d("masterdapm.MonCoffre", "PasswordLog deleted with id: " + id);
  }
  
  public void deleteAll() {
	    database.delete(DatabaseManager.TABLE_PASSWORDS, null, null);
	  }
  
  public boolean isempty() {
	  this.open();
	  long nbrecords = DatabaseUtils.queryNumEntries(database, DatabaseManager.TABLE_PASSWORDS,null);
	  this.close();
	  return (nbrecords == 0);
  }

  public List<PasswordLog> getAllFrom(int pos) {
    List<PasswordLog> passwordLogs = new ArrayList<PasswordLog>();
    PasswordLog passwordLog;

    Cursor cursor = database.query(DatabaseManager.TABLE_PASSWORDS,
        allColumns, null, null, null, null, DatabaseManager.COLUMN_SITE_NAME);

    cursor.moveToPosition(pos);
    while (!cursor.isAfterLast()) {
      passwordLog = cursorToPasswordLog(cursor);
      passwordLogs.add(passwordLog);
      cursor.moveToNext();
    }
    cursor.close();
    return passwordLogs;
  }


  private PasswordLog cursorToPasswordLog(Cursor cursor) {
    PasswordLog passwordLog = new PasswordLog(cursor.getString(0),cursor.getBlob(1));
    return passwordLog;
  }
  
}
