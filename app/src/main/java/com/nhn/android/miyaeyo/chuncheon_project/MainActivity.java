package com.nhn.android.miyaeyo.chuncheon_project;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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


public class MainActivity extends Activity {

    private static final String TAG = "MovieParserActivity";

    private EditText mSearch_bar;
    //    private ImageButton mClear_button;
    private Button mSearch_button;
    //    private MovieAsyncTask mMovieAsyncTask=null;
    private ListView mMovieList;
    private ProgressBar mProgressbar;
    private TextView mEmpty;


    private Button movieSearch = null;
    private ListView movieSearchList = null;

    private static final String OPEN_API_KEY = "0cf5a445809d931f5d6e188651905b0a";
    private static final String MOVIE_SEARCH_URL = "http://openapi.naver.com/search?key=" + OPEN_API_KEY + "&display=20&start=1&target=movie";
    private static final int HTTP_CONNECT_TIMEOUT = 5000;
    private static final int HTTP_READ_TIMEOUT = 5000;
    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearch_bar = (EditText) findViewById(R.id.search_bar);
//        mClear_button = (ImageButton)findViewById(R.id.clear_button);
        mSearch_button = (Button) findViewById(R.id.search_button);
        mMovieList = (ListView) findViewById(R.id.movie_list);
        mProgressbar = (ProgressBar) findViewById(R.id.movie_progress);
        mEmpty = (TextView) findViewById(R.id.empty_view);
        mSearch_button.setEnabled(false);

        mSearch_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mSearch_button.setEnabled(s.toString().length() > 0);
            }
        });

        mMovieList.setOnItemClickListener(mItemClickListener);


    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_button:
                String searchKeyword = mSearch_bar.getText().toString().trim();
                String url = MOVIE_SEARCH_URL + "&query=" + Uri.encode(searchKeyword);
                new MovieAsyncTask().execute(url);
                break;
            case R.id.clear_button:
                ((EditText) findViewById(R.id.search_bar)).setText("");
                break;
        }
    }

    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MovieList movie = (MovieList) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, DetailViewActivity.class);
            intent.putExtra("movie", movie);
            startActivity(intent);
        }
    };


    public class MovieAsyncTask extends AsyncTask<String, Void, List<MovieList>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mEmpty.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);
            //mMovieList.setVisibility(View.GONE);

        }

        @Override
        protected List<MovieList> doInBackground(String... params) {
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
                return new MovieSearchParser().parse(connection.getInputStream());
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
        protected void onPostExecute(List<MovieList> movieLists) {
            super.onPostExecute(movieLists);

            mProgressbar.setVisibility(View.GONE);

            if (movieLists.isEmpty()) {

                mEmpty.setVisibility(View.VISIBLE);
            } else {
                mEmpty.setVisibility(View.GONE);
                mMovieList.setAdapter(new MovieAdapter(MainActivity.this, movieLists));

            }
        }
    }

    public static class MovieSearchParser {
        ArrayList<MovieList> movieInfoList = new ArrayList<MovieList>();
        MovieList movieInfo = null;
        boolean findflag = false;

        public List<MovieList> parse(InputStream is) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            String name = parser.getName();
                            if (name.equals("item")) {
                                movieInfo = new MovieList();
                                findflag = true;
                            } else if (findflag && name.equals("title")) {
                                movieInfo.title = parser.nextText();
                            } else if (findflag && name.equals("subtitle")) {
                                movieInfo.subtitle = parser.nextText();
                            } else if (findflag && name.equals("image")) {
                                movieInfo.image = parser.nextText();
                            } else if (findflag && name.equals("userRating")) {
                                movieInfo.userRating = Float.valueOf(parser.nextText());
                            } else if (findflag && name.equals("pubDate")) {
                                movieInfo.pubDate = parser.nextText();
                            } else if (findflag && name.equals("director")) {
                                movieInfo.director = parser.nextText();
                            } else if (findflag && name.equals("actor")) {
                                movieInfo.actor = parser.nextText();
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (parser.getName().equals("item")) {
                                movieInfoList.add(movieInfo);
                                findflag = false;
                                Log.d(TAG, "parsing result=" + movieInfo.toString());
                            }

                            break;
                    }
                    eventType = parser.next();
                }
                return movieInfoList;
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
            return movieInfoList;

        }
    }

    public static class MovieList implements Parcelable {
        public String title;
        public String image;
        public String subtitle;
        public float userRating;
        public String pubDate;
        public String director;
        public String actor;

        public MovieList() {
        }

        public MovieList(Parcel source) {
            title = source.readString();
            image = source.readString();
            subtitle = source.readString();
            userRating = source.readFloat();
            pubDate = source.readString();
            director = source.readString();
            actor = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

            dest.writeString(title);
            dest.writeString(image);
            dest.writeString(subtitle);
            dest.writeFloat(userRating);
            dest.writeString(pubDate);
            dest.writeString(director);
            dest.writeString(actor);
        }

        public static final Parcelable.Creator<MovieList> CREATOR = new Parcelable.Creator<MovieList>() {
            @Override
            public MovieList createFromParcel(Parcel source) {
                return new MovieList(source);
            }

            @Override
            public MovieList[] newArray(int size) {
                return new MovieList[size];
            }
        };


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
