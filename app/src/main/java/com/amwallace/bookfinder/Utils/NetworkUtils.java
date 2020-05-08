package com.amwallace.bookfinder.Utils;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    //LOG_TAG var for logging
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();
    //base URL for google books API
    private static final String BOOK_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
    //parameter for the search string
    private static final String QUERY_PARAM = "q";
    //param to limit search results
    private static final String MAX_RESULTS = "maxResults";
    //param to filter by print type
    private static final String PRINT_TYPE = "printType";

    public static String getBookInfo(String searchString){
        //local vars for connecting to Books API and getting book info
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJSONString = null;

        try {
            //build URI and issue query to API
            //build URI to query for search string, 15 results, and only books
            Uri searchURI = Uri.parse(BOOK_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, searchString)
                    .appendQueryParameter(MAX_RESULTS, "15")
                    .appendQueryParameter(PRINT_TYPE, "books")
                    .build();
            //convert URI to URL
            URL requestUrl = new URL(searchURI.toString());
            //make request. open connection with GET method
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //set up response from connection
            //get inputstream
            InputStream inputStream = urlConnection.getInputStream();
            //buffered reader from input stream
            reader = new BufferedReader(new InputStreamReader(inputStream));
            //receive response in stringBuilder
            StringBuilder responseBuilder = new StringBuilder();
            //read response input line by line
            String line;
            //loop through lines until no more lines (reader.readLine returns null)
            while((line = reader.readLine()) != null){
                //append line to responseBuilder
                responseBuilder.append(line);
                //append newline char for easier debugging/reading
                responseBuilder.append("\n");
            }
            //check if response was empty, return null if empty
            if (responseBuilder.length() == 0){
                return null;
            }
            //store built response string in bookJSONString
            bookJSONString = responseBuilder.toString();

        } catch (IOException e){
            //catch exceptions
            e.printStackTrace();
        } finally {
            //close network connection after receiving JSON data
            if(urlConnection != null){
                //disconnect if still connected
                urlConnection.disconnect();
            }
            //close BufferedReader
            if(reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    //catch exceptions with closing reader
                    e.printStackTrace();
                }
            }
        }
        //log value of response string
        Log.d(LOG_TAG, bookJSONString);

        return bookJSONString;
    }
}
