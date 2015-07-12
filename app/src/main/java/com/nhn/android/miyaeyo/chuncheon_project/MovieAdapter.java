package com.nhn.android.miyaeyo.chuncheon_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naver on 2015. 7. 8..
 */
public class MovieAdapter extends ArrayAdapter<MainActivity.MovieList> {

    private final LayoutInflater mInflater;

    public class AsyncUrlImage extends AsyncTask<String, Void, Bitmap>{
        private ViewHolder holder;

        public AsyncUrlImage(){}
        public AsyncUrlImage(ViewHolder holder){
            this.holder = holder;
        }

        //public Bitmap bitmap = null;

        @Override
        protected Bitmap doInBackground(String... params) {
            try{
                InputStream input = new java.net.URL(params[0]).openStream();
                return BitmapFactory.decodeStream(input);
            }catch(Exception e){
                Log.e("Error", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null){ // setTag

                holder.mImageView.setImageBitmap(bitmap);
            }

        }
    }

    public MovieAdapter(Context context, List<MainActivity.MovieList> objects) {
        super(context, 0, objects);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("aaa", "currentPosition=" + position);

        ViewHolder holder;


//view holder pattern
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.movie_list,null);
            holder.mImageView = (ImageView)convertView.findViewById(R.id.movie_image);
            holder.mTitle = (TextView)convertView.findViewById(R.id.title);
            holder.mSubTitle = (TextView)convertView.findViewById(R.id.subtitle);
            holder.mDirector = (TextView)convertView.findViewById(R.id.director);

            convertView.setTag(holder);
        } else{
            holder = (ViewHolder)convertView.getTag();
        }

        MainActivity.MovieList item = getItem(position);

        if (item.image.isEmpty()) {
            holder.mImageView.setImageResource(R.drawable.ic_clear_white_24dp);
        } else{

            new AsyncUrlImage(holder).execute(item.image);

        }
        if (item.title.isEmpty()) {
            holder.mTitle.setText(R.string.no_title);
        } else {
            holder.mTitle.setText(Html.fromHtml(item.title));
        }
        if(item.subtitle.isEmpty()){
            holder.mSubTitle.setText(R.string.no_subtitle);
        } else{
            holder.mSubTitle.setText(Html.fromHtml(item.subtitle));
        }
        if(item.director.isEmpty()){
            holder.mDirector.setText(R.string.no_director);
        } else{
            int textLen = item.director.length();
            if(textLen == 0){
                textLen = 1;
            }
            //TextUtils.join(",", item.director.split("\\|"));
            holder.mDirector.setText(item.director.replaceAll("\\|", ", ").substring(0, textLen - 1));
        }

        return convertView;
    }

    private class ViewHolder{
        private ImageView mImageView;
        private TextView mTitle, mSubTitle, mDirector;
    }
}


