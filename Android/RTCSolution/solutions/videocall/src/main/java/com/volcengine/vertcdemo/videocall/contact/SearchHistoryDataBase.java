package com.volcengine.vertcdemo.videocall.contact;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.volcengine.vertcdemo.videocall.model.Contact;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class SearchHistoryDataBase extends RoomDatabase {
    public abstract SearchHistoryDao mSearchHistoryDao();
}
