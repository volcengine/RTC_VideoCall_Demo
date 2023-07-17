package com.volcengine.vertcdemo.videocall.contact;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.volcengine.vertcdemo.videocall.databinding.ItemVideoCallContactBinding;
import com.volcengine.vertcdemo.videocall.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {
    private final List<Contact> mContacts = new ArrayList<>();
    private final ContactsActivity.TriggerCallListener mTriggerListener;

    public ContactAdapter(ContactsActivity.TriggerCallListener triggerListener) {
        this.mTriggerListener = triggerListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VH vh = new VH(ItemVideoCallContactBinding.inflate(LayoutInflater.from(parent.getContext())));
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Contact item = mContacts.get(position);
        if (item == null) {
            return;
        }
        holder.bind(item);
    }

    /**
     * 根据uid获取联系人
     */
    public Contact getContact(String uid) {
        if (TextUtils.isEmpty(uid)) {
            return null;
        }
        for (Contact contact : mContacts) {
            if (TextUtils.equals(uid, contact.getUserId())) {
                return contact;
            }
        }
        return null;
    }


    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Contact> contacts) {
        mContacts.addAll(contacts);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void replace(List<Contact> contacts) {
        mContacts.clear();
        if (contacts != null) {
            mContacts.addAll(contacts);
        }
        notifyDataSetChanged();
    }

    class VH extends RecyclerView.ViewHolder {
        private final ItemVideoCallContactBinding mBinding;
        private Contact mContact;


        public VH(@NonNull ItemVideoCallContactBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.callBtn.setOnClickListener(v -> {
                if (mTriggerListener != null && mContact != null) {
                    String uid = mContact.getUserId();
                    String uname = mContact.getUserName();
                    if (!TextUtils.isEmpty(uid)) {
                        mTriggerListener.onTrigger(uid, uname);
                    }
                }
            });
        }

        public void bind(Contact contact) {
            mContact = contact;
            mBinding.namePrefixTv.setText(contact.getPrefix());
            mBinding.idTv.setText(contact.getUserId());
            mBinding.nameTv.setText(contact.getUserName());
        }
    }
}
