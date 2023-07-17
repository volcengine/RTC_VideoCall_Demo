package com.volcengine.vertcdemo.videocall.contact;

import android.app.Application;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.volcengine.vertcdemo.common.AppExecutors;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.videocall.R;
import com.volcengine.vertcdemo.videocall.VideoCallRTSClient;
import com.volcengine.vertcdemo.videocall.model.Contact;
import com.volcengine.vertcdemo.videocall.model.GetUsersResponse;
import com.volcengine.vertcdemo.videocall.util.Callback;
import com.volcengine.vertcdemo.videocall.util.Constant;
import com.volcengine.vertcdemo.videocall.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ContactViewModel extends AndroidViewModel {
    private static final String TAG = "ContactViewModel";

    public MutableLiveData<List<Contact>> searchResult = new MutableLiveData<>();
    public MutableLiveData<List<Contact>> searchHistory = new MutableLiveData<>();

    private final SearchHistoryDao mSearchHistoryDao;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        SearchHistoryDataBase searchHistoryDataBase = Room
                .databaseBuilder(application, SearchHistoryDataBase.class, "mSearchHistoryDao")
                .build();
        mSearchHistoryDao = searchHistoryDataBase.mSearchHistoryDao();
    }

    /**
     * 增加搜索历史记录
     *
     * @param contact 联系人
     */
    public void insertHistoryContact(Contact contact) {
        if (contact == null || mSearchHistoryDao == null) {
            return;
        }
        contact.setOwner(SolutionDataManager.ins().getUserId());
        contact.setUpdateTs(SystemClock.elapsedRealtime());
        AppExecutors.networkIO().execute(() -> {
            String localUid = SolutionDataManager.ins().getUserId();
            //最多保存10条记录
            List<Contact> historicalRecords = mSearchHistoryDao.getAllHistoryRecords(localUid);
            int historicalRecordSize = historicalRecords.size();
            int value = (historicalRecordSize >= 10) ? (historicalRecordSize - 9) : 0;
            if (value > 0) {
                for (int i = 0; i < value; i++) {
                    Contact delItem = historicalRecords.get(i);
                    if (delItem == null) continue;
                    mSearchHistoryDao.deleteContactRecord(delItem);
                }
            }
            mSearchHistoryDao.insertContactRecord(contact);
        });
    }

    /**
     * 加载搜索历史
     */
    public void loadHistoryContact() {
        if (mSearchHistoryDao == null) {
            return;
        }
        AppExecutors.networkIO().execute(() -> {
            String localUid = SolutionDataManager.ins().getUserId();
            List<Contact> historicalRecords = mSearchHistoryDao.getAllHistoryRecords(localUid);
            if (historicalRecords != null && historicalRecords.size() > 0) {
                searchHistory.postValue(historicalRecords);
            }
        });
    }

    /**
     * 清除搜索结果
     */
    public void clearSearchResult() {
        searchResult.setValue(null);
    }

    /**
     * 查找用户
     */
    public void searchUser(VideoCallRTSClient videoCallRTS, String keyWord, Callback callback) {
        if (videoCallRTS == null || TextUtils.isEmpty(keyWord)) {
            return;
        }
        videoCallRTS.getUserList(keyWord, new IRequestCallback<GetUsersResponse>() {
            @Override
            public void onSuccess(GetUsersResponse response) {
                if (response.errorNo != 0) {
                    String errorTip = response.errorTip;
                    if (response.errorNo == Constant.ERROR_CODE_419){
                        errorTip = Util.getString(R.string.user_not_exist);
                    }
                    SolutionToast.show(errorTip);
                    return;
                }
                if (response.data != null && response.data.size() > 0) {
                    //精准搜索，只会返回一个数据
                    Object contact = ((List<?>) response.data).get(0);
                    if (contact instanceof Contact) {
                        List<Contact> result = searchResult.getValue();
                        if (result == null || !result.contains(contact)) {
                            ArrayList<Contact> contacts = new ArrayList<>();
                            contacts.add((Contact) contact);
                            searchResult.setValue(contacts);
                        }
                    }
                } else {
                    Log.d(TAG, "invoke 'videooneGetUserList' result is null");
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                callback.onResult(new Callback.Result<>(false, message));
            }
        });
    }

}
