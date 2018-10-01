package com.labs.mplant.nytimes.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.labs.mplant.nytimes.R;
import com.labs.mplant.nytimes.adapters.ArticleAdapter;
import com.labs.mplant.nytimes.constants.FilterOptions;
import com.labs.mplant.nytimes.constants.NYTimesAPI;
import com.labs.mplant.nytimes.constants.NYTimesJSON;
import com.labs.mplant.nytimes.constants.NYTimesParams;
import com.labs.mplant.nytimes.constants.NewDeskValue;
import com.labs.mplant.nytimes.fragments.OptionsDialog;
import com.labs.mplant.nytimes.listeners.EndlessRecyclerViewScrollListener;
import com.labs.mplant.nytimes.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity  implements OptionsDialog.OnDismissListener {
    private ArrayList<Article> articles;
    int numberOfColumns = 2;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    EditText mEditText;
    Button mButtonSearch;
    ProgressBar mProgress;
    private EndlessRecyclerViewScrollListener scrollListener;
    private Calendar cal;
    private String mSortBy;
    private HashMap<String, Boolean> mNewDeskValue;
    String mQuery = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_acticity);
        setUpViews();
        loadNextDataFromApi(0);
    }

    private void setUpViews() {
        mNewDeskValue = new HashMap<>();
        mNewDeskValue.put(NewDeskValue.SPORTS, false);
        mNewDeskValue.put(NewDeskValue.FASHION_STYLE, false);
        mNewDeskValue.put(NewDeskValue.ART, false);
        mSortBy = NYTimesParams.NEWEST;
        mRecyclerView = findViewById(R.id.articles);
        mEditText = findViewById(R.id.article_search_bar);
        mButtonSearch = findViewById(R.id.search_button);
        mProgress = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        GridLayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        mRecyclerView.setLayoutManager(layoutManager);
        articles = new ArrayList<>();
        mAdapter = new ArticleAdapter(articles);
        mRecyclerView.setAdapter(mAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };

        mRecyclerView.addOnScrollListener(scrollListener);
    }


    public void loadNextDataFromApi(int page){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = getRequestParams(page);
        client.get(NYTimesAPI.BASE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                JSONArray articleJSONRsponse = null;
                try {
                    articleJSONRsponse = response.getJSONObject(NYTimesJSON.RESPONSE)
                            .getJSONArray(NYTimesJSON.DOCS);
                    articles.addAll(Article.fromJSONArray(articleJSONRsponse));
                    mProgress.setVisibility(View.INVISIBLE);
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }
    public String getNewsDeskParams(){
        StringBuilder sb = new StringBuilder("(");
        for (String key : mNewDeskValue.keySet()){
            if(mNewDeskValue.get(key)){
                if(sb.length() > 1){
                    sb.append(" ");
                }
                sb.append(key);
            }
        }
        sb.append(")");
        return sb.toString();
    }
    public RequestParams getRequestParams(int page){
        RequestParams params = new RequestParams();
        params.put(NYTimesParams.PAGE, page);
        String newsDeskParams = getNewsDeskParams();
        params.put(NYTimesParams.SORT_ORDER, mSortBy);
        if(!newsDeskParams.equals("()")){
            params.put(NYTimesParams.NEWS_DESK, newsDeskParams);
        }
        if(cal != null){
            params.put(NYTimesParams.BEGIN_DATE, formatDateAsString());
        }
        params.put(NYTimesParams.QUERY, mQuery);
        params.put(NYTimesParams.API_KEY, NYTimesAPI.API_KEY);
        return params;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                newSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    public String formatDateAsString(){
        SimpleDateFormat dateString = new SimpleDateFormat("yyyyMMdd", Locale.US);
        return dateString.format(cal.getTime());
    }

    public void newSearch(){
        mProgress.setVisibility(View.VISIBLE);
        articles.clear();
        scrollListener.resetState();
        loadNextDataFromApi(0);
    }


    public void onShowOptions(MenuItem mi){
        FragmentManager fragmentManager = getSupportFragmentManager();
        OptionsDialog options = new OptionsDialog();
        Bundle args = new Bundle();
        args.putString(NYTimesParams.SORT_ORDER, mSortBy);
        String beginDate = cal != null ? formatDateAsString() : "";
        args.putString(FilterOptions.BEGIN_DATE, beginDate);
        args.putBoolean(NewDeskValue.SPORTS, mNewDeskValue.get(NewDeskValue.SPORTS));
        args.putBoolean(NewDeskValue.FASHION_STYLE, mNewDeskValue.get(NewDeskValue.FASHION_STYLE));
        args.putBoolean(NewDeskValue.ART, mNewDeskValue.get(NewDeskValue.ART));
        options.setArguments(args);
        options.show(fragmentManager, "dialog");
        options.setDismissListener(MainActivity.this);
    }

    public void onDismiss(OptionsDialog optionsFilters) {
        cal = optionsFilters.getCal();
        mNewDeskValue = optionsFilters.getmNewDeskValue();
        mSortBy = optionsFilters.getmSortBy();
        newSearch();
    }
}
