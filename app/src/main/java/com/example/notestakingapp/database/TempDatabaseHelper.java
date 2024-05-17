package com.example.notestakingapp.database;
import com.example.notestakingapp.database.DatabaseHandler;
import com.example.notestakingapp.database.NoteComponent.Note;
import com.example.notestakingapp.database.NoteComponent.TextSegment;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class TempDatabaseHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "test";
	public static final int DB_VERSION = 2;

	public TempDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	//Merge Note Table
	public static void mergeNoteTable(Context context) {
		SQLiteOpenHelper tempDatabaseHelper = new TempDatabaseHelper(context);
		SQLiteDatabase tempDb = tempDatabaseHelper.getReadableDatabase();

		//get all records from test's db note table
		String query = "SELECT * FROM " + DatabaseHandler.NOTE_TABLE;
		Cursor cursor = tempDb.rawQuery(query, null);
		while (cursor.moveToNext()) {
			Note note = new Note(cursor.getInt(0),  //noteId
					cursor.getString(1),            //title
					cursor.getLong(2),              //createAt
					cursor.getString(3) );          //color
			long firebaseNoteId = cursor.getInt(0);
			//Note với created at này chưa tồn tại
			if (!checkExistByCreateAt(context, DatabaseHandler.NOTE_TABLE, Long.toString(note.getCreateAt()))) {
				long newInsertedNoteId = DatabaseHandler.insertNote(context, note.getTitle(), note.getColor());
				mergeTextSegmentTable(context, firebaseNoteId, newInsertedNoteId);
				mergeAudioTable(context, firebaseNoteId, newInsertedNoteId);
				mergeImageTable(context, firebaseNoteId, newInsertedNoteId);
				//TODO: IMPLEMENT TAG FOR NEW DATABASE (NO MORE TAG_ID IN NOTE TABLE)

				long tagId = getTempNoteTagId(context, firebaseNoteId);
				Log.d("TAG ID", Integer.toString((int) tagId));

				if (tagId != 0) {
//					//get firebase tag name
					String tagName = getTempTagById(context, tagId);

					int dbTagId = checkTagExistByName(context, tagName);
					if (dbTagId == -1) {
						//Tag name not exist in local database
						//Insert new tag with firebase's name and get the inserted id
						long newInsertedTadId = DatabaseHandler.createNewTag(context, tagName);
						DatabaseHandler.setTagForNote(context, (int) newInsertedNoteId, (int) newInsertedTadId);
					}else {
						DatabaseHandler.setTagForNote(context, (int) newInsertedNoteId, dbTagId);
					}
				}
			}
		}
	}

	public static void mergeTextSegmentTable(Context context, long firebaseNoteId, long newInsertedNoteId) {
		SQLiteOpenHelper tempDatabaseHelper = new TempDatabaseHelper(context);
		SQLiteDatabase tempDb = tempDatabaseHelper.getReadableDatabase();

		String query = "SELECT * FROM " + DatabaseHandler.TEXTSEGMENT_TABLE + " WHERE " + DatabaseHandler.COLUMN_NOTE_ID + " = ?";
		Cursor cursor = tempDb.rawQuery(query, new String[] {Long.toString(firebaseNoteId)});
		while (cursor.moveToNext()) {
			DatabaseHandler.insertTextSegment(context, (int) newInsertedNoteId, cursor.getString(2));
		}
	}
	@SuppressLint("Range")
	public static void mergeAudioTable(Context context, long firebaseNoteId, long newInsertedNoteId) {
		SQLiteOpenHelper tempDatabaseHelper = new TempDatabaseHelper(context);
		SQLiteDatabase tempDb = tempDatabaseHelper.getReadableDatabase();

		String query = "SELECT * FROM " + DatabaseHandler.AUDIO_TABLE + " WHERE " + DatabaseHandler.COLUMN_NOTE_ID + " = ?";
		Cursor cursor = tempDb.rawQuery(query, new String[] {Long.toString(firebaseNoteId)});
		while (cursor.moveToNext()) {
			DatabaseHandler.insertAudio(context, (int) newInsertedNoteId, cursor.getBlob(cursor.getColumnIndex(DatabaseHandler.COLUMN_AUDIO_DATA)));
		}
	}
	public static void mergeImageTable(Context context, long firebaseNoteId, long newInsertedNoteId) {
		SQLiteOpenHelper tempDatabaseHelper = new TempDatabaseHelper(context);
		SQLiteDatabase tempDb = tempDatabaseHelper.getReadableDatabase();

		String query = "SELECT * FROM " + DatabaseHandler.IMAGE_TABLE + " WHERE " + DatabaseHandler.COLUMN_NOTE_ID + " = ?";
		Cursor cursor = tempDb.rawQuery(query, new String[] {Long.toString(firebaseNoteId)});
		while (cursor.moveToNext()) {
			DatabaseHandler.insertImage(context, (int) newInsertedNoteId, cursor.getBlob(1));
		}
	}


	public static boolean checkExistByCreateAt(Context context, String tableName, String CreateAt) {
		SQLiteOpenHelper noteTakingDatabaseHelper = new NoteTakingDatabaseHelper(context);
		SQLiteDatabase db = noteTakingDatabaseHelper.getReadableDatabase();

		String query = "SELECT * FROM "+ tableName + " WHERE CREATE_AT = ?";
		Cursor cursor = db.rawQuery(query, new String[] {CreateAt});
		Log.d("CREATED AT", CreateAt);
		Log.d("CREATED AT CHECK", Integer.toString(cursor.getCount()));
		return cursor.getCount() >= 1;
	}

	@SuppressLint("Range")
	public static String getTempTagById(Context context, long tagId) {

		SQLiteOpenHelper tempDatabaseHelper = new TempDatabaseHelper(context);
		SQLiteDatabase tempDb = tempDatabaseHelper.getReadableDatabase();

		String query = "SELECT TAG_NAME FROM " + DatabaseHandler.TAG_TABLE + " WHERE " + DatabaseHandler.COLUMN_TAG_ID + " = ?";
		Cursor cursor = tempDb.rawQuery(query, new String[] {Long.toString(tagId)});
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_TAG_NAME));
		}
		return "TAG ID NOT FOUND";
	}

	/**
	 Check if tag already exist in local database
	 @return return tag_id if tag exists and -1 if doesn't exist
	 */
	public static int checkTagExistByName(Context context, String tagName) {
		SQLiteOpenHelper noteTakingDatabaseHelper = new NoteTakingDatabaseHelper(context);
		SQLiteDatabase db = noteTakingDatabaseHelper.getReadableDatabase();

		String query = "SELECT * FROM " + DatabaseHandler.TAG_TABLE + " WHERE " + DatabaseHandler.COLUMN_TAG_NAME + " = ?";
		Cursor cursor = db.rawQuery(query, new String[] {tagName});
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getInt(0); //tag id
		}
		return -1;
	}
	public static int getTempNoteTagId(Context context, long noteId) {
		SQLiteOpenHelper tempDatabaseHelper = new TempDatabaseHelper(context);
		SQLiteDatabase tempDb = tempDatabaseHelper.getReadableDatabase();

		String query = "SELECT TAG_ID FROM " + DatabaseHandler.NOTE_TAG_TABLE + " WHERE " + DatabaseHandler.COLUMN_NOTE_ID + " = ?";
		Cursor cursor = tempDb.rawQuery(query, new String[] {Long.toString(noteId)});
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getInt(0);
		}

		return 0;
	}
}
