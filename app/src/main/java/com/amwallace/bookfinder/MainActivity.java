package com.amwallace.bookfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amwallace.bookfinder.Data.BookLoader;
import com.amwallace.bookfinder.Data.FetchBook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private EditText mSearchInput;
    private TextView mTitleTxt, mAuthorTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setup edit text and textViews
        mSearchInput = (EditText) findViewById(R.id.bookSearchEdt);
        mTitleTxt = (TextView) findViewById(R.id.titleTxt);
        mAuthorTxt = (TextView) findViewById(R.id.authorTxt);
        //init loader if it exists
        if(getSupportLoaderManager().getLoader(0) != null){
            getSupportLoaderManager().initLoader(0,null,this);
        }


    }
    //search books button onclick listener, perform search for user input
    public void searchBooks(View view){
        //get search input
        String searchString = mSearchInput.getText().toString();

        //check if input is empty
        if(searchString.equals("")){
            //empty/no input
            Toast.makeText(getApplicationContext(),
                    "Search field is empty, enter search term", Toast.LENGTH_SHORT).show();
        } else {
            //some input, execute background task
            //hide keyboard
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager != null){
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                        inputMethodManager.HIDE_NOT_ALWAYS);
            }
            //check network connection
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if(connectivityManager != null){
                networkInfo = connectivityManager.getActiveNetworkInfo();
            }

            //only execute search if network is connected
            if(networkInfo != null && networkInfo.isConnected()){
                //execute background task of searching for book
                //new FetchBook(mTitleTxt,mAuthorTxt).execute(searchString);
                Bundle queryBundle = new Bundle();
                queryBundle.putString("searchString",searchString);
                getSupportLoaderManager().restartLoader(0,queryBundle, this);
                //show loading text to user
                mTitleTxt.setText(R.string.loading);
                mAuthorTxt.setText("");
            } else {
                //update TextViews to show no network connection error message
                mAuthorTxt.setText("");
                mTitleTxt.setText(R.string.no_network);
            }
        }
    }

    //called when instantiate loader
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        String searchString = "";
        //if args not empty, get searchString
        if(args != null){
            searchString = args.getString("searchString");
        }
        //return instance of BookLoader class with context and searchString
        return new BookLoader(this, searchString);
    }
    //called when loader's task finishes
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        //check string, if null connection failed in FetchBook
        if(data == null){
            //connection failed
            Log.d("CONNECTION FAILED", "ERROR CONNECTING IN FETCHBOK");
        } else {
            //success, String s has response string
            try {
                //parse JSON data
                JSONObject jsonObject = new JSONObject(data);
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
                    mAuthorTxt.setText(author);
                    mTitleTxt.setText(title);
                } else {
                    //no results
                    mTitleTxt.setText(R.string.no_results);
                    mAuthorTxt.setText("");
                }
            } catch (JSONException e){
                //catch exception with JSON parsing
                //update TextViews to show results not found
                mTitleTxt.setText(R.string.no_results);
                mAuthorTxt.setText("");
                e.printStackTrace();
            }
        }
    }
    //cleans up remaining resources
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
