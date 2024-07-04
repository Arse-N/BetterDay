package com.example.betterday.ui.block;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BlockViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BlockViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is blocking fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}