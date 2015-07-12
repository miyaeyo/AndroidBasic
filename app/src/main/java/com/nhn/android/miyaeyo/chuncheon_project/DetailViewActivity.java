package com.nhn.android.miyaeyo.chuncheon_project;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Naver on 2015. 7. 9..
 */
public class DetailViewActivity extends Activity{
    private ImageView mMovieImage;
    private TextView mTitle;
    private TextView mSubtitle;
    private TextView mUserRating;
    private TextView mPubDate;
    private TextView mDirector;
    private TextView mActor;
    private ListView mBlogList;
    private ProgressBar mProgressbar;
    private TextView mEmpty;

    //private static final String OPEN_API_KEY="c1b406b32dbbbbeee5f2a36ddc14067f";
    private static final String TAG = "BlogParserActivity";
    private static final String OPEN_API_KEY="0cf5a445809d931f5d6e188651905b0a";
    private static final String MOVIE_SEARCH_URL="http://openapi.naver.com/search?key="+OPEN_API_KEY+"&display=10&start=1&target=blog&sort=sim";
    private static final int HTTP_CONNECT_TIMEOUT = 5000;
    private static final int HTTP_READ_TIMEOUT = 5000;
    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_mode);

        MainActivity.MovieList movie = (MainActivity.MovieList)getIntent().getParcelableExtra("movie");

        String searchKeyword = movie.title.replaceAll("\\><.*?\",","");
        String url = MOVIE_SEARCH_URL+"&query="+ Uri.encode(searchKeyword);

        mMovieImage = (ImageView)findViewById(R.id.d_movie_image);
        mTitle = (TextView)findViewById(R.id.d_title);
        mSubtitle = (TextView)findViewById(R.id.d_subtitle);
        mUserRating = (TextView)findViewById(R.id.d_user_rating);
        mPubDate = (TextView)findViewById(R.id.d_pub_date);
        mDirector = (TextView)findViewById(R.id.d_director);
        mActor = (TextView)findViewById(R.id.d_actor);
        mBlogList = (ListView)findViewById(R.id.d_blog_list);
        mBlogList.setOnItemClickListener(mItemClickListener);

        mProgressbar = (ProgressBar)findViewById(R.id.d_progress);
        mEmpty = (TextView)findViewById(R.id.d_empty_view);

        new AsyncUrlImage().execute(movie.image);
        mTitle.setText(Html.fromHtml(movie.title));
        mSubtitle.setText(Html.fromHtml(movie.subtitle));
        mUserRating.setText(String.valueOf(movie.userRating));
        mPubDate.setText(movie.pubDate);

        int directorLen = movie.director.length();;
        if(directorLen == 0) {
            directorLen = 1;
        }
        mDirector.setText(Html.fromHtml(movie.director.replaceAll("\\|",", ").substring(0,directorLen-1)));
        int actorLen = movie.actor.length();
        if(actorLen == 0) {
            actorLen = 1;
        }

        mActor.setText(Html.fromHtml(movie.actor.replaceAll("\\|",", ").substring(0,actorLen-1)));

        new BlogAsyncTask().execute(url);


    }
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Blog blog = (Blog)parent.getItemAtPosition(position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(blog.bloggerlink));



            Log.d("bbb", "link: "+blog.bloggerlink);

            startActivity(intent);

        }
    };


    public class AsyncUrlImage extends AsyncTask<String, Void, Bitmap> {

        public AsyncUrlImage(){}
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
                mMovieImage.setImageBitmap(bitmap);
            }

        }
    }

    public class BlogAsyncTask extends AsyncTask<String, Void, List<Blog>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressbar.setVisibility(View.VISIBLE);

        }

        @Override
        protected List<Blog> doInBackground(String... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
                connection.setReadTimeout(HTTP_READ_TIMEOUT);
                connection.setRequestMethod("GET"); // 디폴트는 GET
                connection.setRequestProperty(HTTP_HEADER_USER_AGENT, "Android 5.1");

                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("response code=" + responseCode);
                }
                return new BlogSearchParser().parse(connection.getInputStream());
            } catch (IOException e) {
                Log.e(TAG, "IOException occurred", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Blog> BlogList) {
            super.onPostExecute(BlogList);

            mProgressbar.setVisibility(View.GONE);

            if(BlogList.isEmpty()){
                mEmpty.setVisibility(View.VISIBLE);
            }
            else{
                mEmpty.setVisibility(View.GONE);
                mBlogList.setAdapter(new BlogAdapter(DetailViewActivity.this, BlogList));
            }
        }
    }

    public static class BlogSearchParser{
        ArrayList<Blog> blogList = new ArrayList<Blog>();
        Blog blog = null;
        boolean findflag = false;

        public List<Blog> parse(InputStream is) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch(eventType) {
                        case XmlPullParser.START_TAG :
                            String name = parser.getName();
                            if (name.equals("item")) {
                                blog = new Blog();
                                findflag=true;
                            } else if (findflag && name.equals("title")) {
                                blog.title = parser.nextText();
                            } else if (findflag && name.equals("link")) {
                                blog.bloggerlink = parser.nextText();
                            } else if (findflag && name.equals("description")) {
                                blog.description = parser.nextText();
                            } else if (findflag && name.equals("bloggername")) {
                                blog.blogger = parser.nextText();
                            }
                            break;
                        case XmlPullParser.END_TAG :
                            if(parser.getName().equals("item")) {
                                blogList.add(blog);
                                findflag=false;
                                Log.d(TAG, "parsing result=" + blog.toString());
                            }

                            break;
                    }
                    eventType = parser.next();
                }
                return blogList;
            } catch (XmlPullParserException e) {
                Log.e(TAG, "IOException occurred", e);
            } catch (IOException e) {
                Log.e(TAG, "IOException occurred", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            return blogList;

        }
    }

    public static class Blog{
        public String title;
        public String bloggerlink;
        public String description;
        public String blogger;
    }



}
