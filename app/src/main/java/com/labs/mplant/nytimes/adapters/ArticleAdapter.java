package com.labs.mplant.nytimes.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.labs.mplant.nytimes.R;
import com.labs.mplant.nytimes.activities.ArticleActivity;
import com.labs.mplant.nytimes.constants.ArticleConstants;
import com.labs.mplant.nytimes.models.Article;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.MovieViewHolder> {
    private ArrayList<Article> mDataset;
    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mArticleView;
        ImageView mThumbnail;
        TextView mHeadline;
        private Context context;
        public MovieViewHolder(Context context, View v) {
            super(v);
            mArticleView = v;
            this.context = context;
            v.setOnClickListener( MovieViewHolder.this);
            setUpViews();
        }
        private void setUpViews(){
            mThumbnail = mArticleView.findViewById(R.id.thumbnail);
            mHeadline = mArticleView.findViewById(R.id.headline);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Article article = mDataset.get(position);
                Intent i = new Intent(context, ArticleActivity.class);
                i.putExtra(ArticleConstants.WEB_URL, article.getWebUrl());
                context.startActivity(i);
            }
        }
    }

    public ArticleAdapter(ArrayList<Article> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ArticleAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        Context ctx = parent.getContext();
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.article_view, parent, false);

        MovieViewHolder vh = new MovieViewHolder(ctx, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, int position) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.t_logo)
                .error(R.drawable.t_logo);
        Article article = mDataset.get(position);
        Glide.with(holder.mThumbnail.getContext())
                .load(article.getThumbnail())
                .transition(withCrossFade())
                .apply(options)
                .into(holder.mThumbnail);
        holder.mHeadline.setText(article.getHeadline());

    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }



}
