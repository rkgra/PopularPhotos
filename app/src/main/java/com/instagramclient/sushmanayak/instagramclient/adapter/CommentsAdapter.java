package com.instagramclient.sushmanayak.instagramclient.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.instagramclient.sushmanayak.instagramclient.Utility;
import com.instagramclient.sushmanayak.instagramclient.data.Comment;
import com.instagramclient.sushmanayak.instagramclient.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by SushmaNayak on 9/18/2015.
 */
public class CommentsAdapter extends ArrayAdapter<Comment> {

    Context mContext;
    Utility mUtility;

    public static class CommentViewHolder {
        ImageView profilePhoto;
        TextView comment;
        TextView timestamp;
    }

    public CommentsAdapter(Context context, List<Comment> objects) {
        super(context, 0, objects);
        mContext = context;
        mUtility =  new Utility();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment comment = getItem(position);
        CommentViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
            viewHolder = new CommentViewHolder();
            viewHolder.profilePhoto = (ImageView) convertView.findViewById(R.id.profilePhoto);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.comment);
            viewHolder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (CommentViewHolder) convertView.getTag();

        Picasso.with(mContext).load(comment.commentFrom.userProfileUrl).placeholder(R.drawable.loading).into(viewHolder.profilePhoto);
        mUtility.SetText(mContext,comment.commentFrom.username ,comment.commentText,viewHolder.comment);
        viewHolder.timestamp.setText(DateUtils.getRelativeTimeSpanString((long) Double.parseDouble(comment.commentTime) * 1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));
        return convertView;
    }
}
