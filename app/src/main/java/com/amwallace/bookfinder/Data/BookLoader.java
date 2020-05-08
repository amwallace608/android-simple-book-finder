package com.amwallace.bookfinder.Data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.amwallace.bookfinder.Utils.NetworkUtils;

public class BookLoader extends AsyncTaskLoader<String> {
    private String mSearchString;
    //constructor
    public BookLoader(@NonNull Context context, String searchString){
        super(context);
        mSearchString = searchString;
    }
    //load in background
    @Nullable
    @Override
    public String loadInBackground() {
        //perform book search, return result
        return NetworkUtils.getBookInfo(mSearchString);
    }

    //on start loading, forceLoad to start loadInBackground
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

}
