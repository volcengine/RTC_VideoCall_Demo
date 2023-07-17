package com.volcengine.vertcdemo.videocall.model;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

@Entity(tableName = "search_history")
public class Contact {
    @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    private String userId;
    @SerializedName("user_name")
    @ColumnInfo(name = "user_name")
    private String userName;
    @ColumnInfo(name = "owner")
    private String owner;
    @ColumnInfo(name = "update_ts")
    @PrimaryKey
    @NonNull
    private Long updateTs;

    public String getPrefix() {
        if (TextUtils.isEmpty(userName)) {
            return "";
        }
        return userName.substring(0, 1);
    }

    public Contact(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.updateTs = SystemClock.elapsedRealtime();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setUpdateTs(@NonNull Long ts) {
        updateTs = ts;
    }

    @NonNull
    public Long getUpdateTs() {
        return updateTs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(updateTs, contact.updateTs)
                && userId.equals(contact.userId)
                && userName.equals(contact.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName, owner, updateTs);
    }
}
