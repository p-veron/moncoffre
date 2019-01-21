package fr.toulon.masterdapm.MonCoffre.lib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper {

	public static final String TABLE_PASSWORDS = "PASSWORDS";
	public static final String COLUMN_SITE_NAME = "SITE_NAME";
	public static final String COLUMN_CRYPTO = "CRYPTO";

	private static final String DATABASE_NAME = "passwords.db";
	private static final int DATABASE_VERSION = 1;

	// Requête SQL pour la création de la base de données
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_PASSWORDS + "(" + COLUMN_SITE_NAME
			+ " text primary key, " + COLUMN_CRYPTO
			+ " text not null);";

	public DatabaseManager(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.w("masterdapm.MonCoffre","Database create");
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		Log.w(DatabaseManager.class.getName(),
		Log.w("masterdapm.MonCoffre",
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old datas");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORDS);
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		Log.w(DatabaseManager.class.getName(),
		Log.w("masterdapm.Moncoffre",
				"Downgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old datas");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORDS);
			onCreate(db);
	}
}
