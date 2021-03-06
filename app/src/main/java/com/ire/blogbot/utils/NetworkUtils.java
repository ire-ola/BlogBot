package com.ire.blogbot.utils;

import android.net.Uri;
import android.util.Log;

import com.ire.blogbot.model.News;
import com.ire.blogbot.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ire on 6/1/17.
 */

public class NetworkUtils {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static ArrayList<News> parseJSON(String source, String category) throws IOException {
        URL url = buildUrl(source, category);
        String jsonResult = getResponseFromHttpUrl(url);

        ArrayList<News> news = new ArrayList<>();

        try{
            JSONObject jsonObject = new JSONObject(jsonResult);
            JSONArray jsonArray = jsonObject.getJSONArray("articles");

            if (jsonArray != null){
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject article = jsonArray.getJSONObject(i);
                    String title = article.getString("title");
                    String image = article.getString("urlToImage");
                    String detailUrl = article.getString("url");
                    String time = article.getString("publishedAt");
                    news.add(new News(title, time, detailUrl, image));
                }
            }

        }catch (JSONException | NullPointerException e){
            e.printStackTrace();
            return null;
        }
        return news;
    }

    public static URL buildUrl(String entertainmentNewsSource, String category){
        final String ENTERTAINMENT_NEWS_BASE_URL = "https://newsapi.org/v1/articles";
        final String PARAM_SOURCE = "source";
        final String PARAM_SORT_BY = "sortBy";
        final String PARAM_API_KEY = "apiKey";
        final String KEY = "3431d57e51a04c1d967e2eb96c99fd1a";

        Uri builtUri = Uri.parse(ENTERTAINMENT_NEWS_BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_SOURCE, entertainmentNewsSource)
                .appendQueryParameter(PARAM_SORT_BY, category)
                .appendQueryParameter(PARAM_API_KEY, KEY)
                .build();

        Log.i(LOG_TAG, builtUri.toString());

        URL url = null;
        try{
            url = new URL(builtUri.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException{
        HttpURLConnection urlConnection = null;
        String newsJsonStr = null;
        BufferedReader reader = null;

        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.connect();

//                Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0){
                return null;
            }

            newsJsonStr = buffer.toString();

        }catch(IOException e){
            Log.e(LOG_TAG, "Error fetching data", e);
            return null;
        }finally{
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return newsJsonStr;
    }
}
