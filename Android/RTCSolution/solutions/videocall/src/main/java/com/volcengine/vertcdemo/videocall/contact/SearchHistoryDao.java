package com.volcengine.vertcdemo.videocall.contact;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.volcengine.vertcdemo.videocall.model.Contact;

import java.util.List;

@Dao
public interface SearchHistoryDao {
    /**
     * 获取所有搜索历史记录
     *
     * @return 本登录账号所属历史记录
     */
    @Query("select * from search_history where owner = :localUid order by update_ts desc")
    List<Contact> getAllHistoryRecords(@NonNull String localUid);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertContactRecord(Contact contact);

    @Delete
    int deleteContactRecord(Contact contact);
}
