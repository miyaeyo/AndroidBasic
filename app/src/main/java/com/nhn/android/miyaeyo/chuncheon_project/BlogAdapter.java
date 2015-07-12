package com.nhn.android.miyaeyo.chuncheon_project;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * jjj
 * Created by Naver on 2015. 7. 9..
 */
public class BlogAdapter extends ArrayAdapter<DetailViewActivity.Blog> {
    private final LayoutInflater mInflater;

    public BlogAdapter(Context context, List<DetailViewActivity.Blog> objects) {
        super(context, 0, objects);
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.blog_list,null);
            holder.mTitle = (TextView)convertView.findViewById(R.id.b_title);
            holder.mDescription = (TextView)convertView.findViewById(R.id.b_description);
            holder.mBlogger = (TextView)convertView.findViewById(R.id.b_blogger);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        DetailViewActivity.Blog item = getItem(position);

        if(item.title.isEmpty()){
            holder.mTitle.setText(R.string.no_title);
        }else{
            holder.mTitle.setText(Html.fromHtml(item.title));
        }
        if(item.description.isEmpty()){
            holder.mDescription.setText(R.string.no_description);
        }
        else{
            holder.mDescription.setText(Html.fromHtml(item.description));
        }
        if(item.blogger.isEmpty()){
            holder.mBlogger.setText(R.string.no_blogger);
        }
        else{
            holder.mBlogger.setText(item.blogger);
        }

        return convertView;
    }


    private class ViewHolder{
        private TextView mTitle, mDescription, mBlogger;
    }
}

