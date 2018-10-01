package com.labs.mplant.nytimes.models;

import com.labs.mplant.nytimes.constants.NYTimesJSON;
import com.labs.mplant.nytimes.constants.NYTimesAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Article {
    String webUrl;
    String headline;

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    String thumbnail;

    public Article(JSONObject jsonObject) throws JSONException{
        this.webUrl = jsonObject.getString(NYTimesJSON.WEB_URL);
        this.headline = jsonObject.getJSONObject(NYTimesJSON.HEADLINE)
                .getString(NYTimesJSON.MAIN);

        JSONArray multimedia = jsonObject.getJSONArray(NYTimesJSON.MULTIMEDIA);
        if(multimedia.length()> 0 ){
            JSONObject multimediaJson = multimedia.getJSONObject(0);
            this.thumbnail = NYTimesAPI.NY_TIMES + multimediaJson.getString(NYTimesJSON.URL);
        } else {
            this.thumbnail = "";
        }

    }
    public static ArrayList<Article> fromJSONArray(JSONArray array) throws JSONException{
        ArrayList<Article> results = new ArrayList<>();
        for (int i = 0; i < array.length(); i++){
            results.add(new Article(array.getJSONObject(i)));
        }
        return results;
    }
}
