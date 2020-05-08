package com.amwallace.bookfinder.Data;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amwallace.bookfinder.MainActivity;
import com.amwallace.bookfinder.R;
import com.amwallace.bookfinder.Utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class FetchBook extends AsyncTask<String, Void, String> {
    //weakReferences to author & title text views (to prevent mem leaks)
    private WeakReference<TextView> mTitleTxt;
    private WeakReference<TextView> mAuthorTxt;

    //constructor with textviews, assign weakReferences
    public FetchBook(TextView mTitleTxt, TextView mAuthorTxt) {
        this.mTitleTxt = new WeakReference<>(mTitleTxt);
        this.mAuthorTxt = new WeakReference<>(mAuthorTxt);
    }

    //do in background
    @Override
    protected String doInBackground(String... strings) {
        //return results of getBookInfo with search string
        return NetworkUtils.getBookInfo(strings[0]);
    }
    //on complete
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        //check string, if null connection failed in FetchBook
        if(s == null){
            //connection failed
            Log.d("CONNECTION FAILED", "ERROR CONNECTING IN FETCHBOK");
        } else {
            //success, String s has response string
            try {
                //parse JSON data
                JSONObject jsonObject = new JSONObject(s);
                //get items JSON array from result string
                JSONArray itemsArray = jsonObject.getJSONArray("items");
                //vars for parsing loop
                int i = 0;
                String title = null;
                String author = null;
                //loop through items checking books for both author and title
                while (i < itemsArray.length() &&
                        (author == null && title == null)){
                    //get JSONObject of current book item
                    JSONObject book = itemsArray.getJSONObject(i);
                    //get JSON object of volumeInfo for book
                    JSONObject volumeInfo = book.getJSONObject("volumeInfo");
                    //try to get title and author from current book item's volumeInfo
                    try {
                        title = volumeInfo.getString("title");
                        author = volumeInfo.getString("authors");
                    } catch (Exception e){
                        //catch exception getting title and author info
                        e.printStackTrace();
                    }
                    //move to next item
                    i++;
                }
                //update TextViews with book title and author if found
                if(title != null && author != null){
                    //use get() method on WeakReferences to dereference
                    mAuthorTxt.get().setText(author);
                    mTitleTxt.get().setText(title);
                } else {
                    //no results
                    mTitleTxt.get().setText(R.string.no_results);
                    mAuthorTxt.get().setText("");
                }
            } catch (JSONException e){
                //catch exception with JSON parsing
                //update TextViews to show results not found
                mTitleTxt.get().setText(R.string.no_results);
                mAuthorTxt.get().setText("");
                e.printStackTrace();
            }
        }
    }
}
