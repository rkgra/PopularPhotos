package com.instagramclient.sushmanayak.instagramclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.instagramclient.sushmanayak.instagramclient.adapter.CommentsAdapter;
import com.instagramclient.sushmanayak.instagramclient.data.Comment;
import com.instagramclient.sushmanayak.instagramclient.data.User;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment {

    String mPhotoId;
    Context mContext;
    ListView lvComments;
    CommentsAdapter commentsAdapter;
    ArrayList<Comment> comments;
    final static String PHOTOID = "InstagramPhotoID";

    public CommentsFragment() {
    }

    public static CommentsFragment newInstance(Context context, String id) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(PHOTOID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        lvComments = (ListView) view.findViewById(R.id.lvComments);
        comments = new ArrayList<>();

        Bundle args = getArguments();
        if(args != null)
            mPhotoId = args.getString(PHOTOID);

        commentsAdapter = new CommentsAdapter(mContext, comments);
        lvComments.setAdapter(commentsAdapter);
        fetchComments();
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setTitle("Comments");
        return view;
    }

    private void fetchComments() {
        String url = Utility.COMMENTS_BASE_URL + mPhotoId + "/comments?client_id=" + getString(R.string.api_key);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(mContext, url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {
                try {
                    JSONArray commentsJSON = response.getJSONArray(Utility.DATA_ARRAY);
                    int commentsCount =commentsJSON.length();
                    // Load only 20 comments. TODO: Add a button to load 20 more comments like real instagram
                    if(commentsCount > 20)
                        commentsCount = 20;

                    comments.clear();
                    for (int i = 0; i < commentsCount; i++) {
                        JSONObject commentJSON = commentsJSON.getJSONObject(i);
                        Comment comment = new Comment();
                        comment.commentText = commentJSON.getString(Utility.TEXT);
                        comment.commentTime = commentJSON.getString(Utility.TIMESTAMP);
                        comment.commentFrom = new User(commentJSON.getJSONObject(Utility.FROM_USER).getString(Utility.ID),
                                commentJSON.getJSONObject(Utility.FROM_USER).getString(Utility.USERNAME),
                                commentJSON.getJSONObject(Utility.FROM_USER).getString(Utility.PROFILE_PIC));
                        comments.add(comment);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                commentsAdapter.notifyDataSetChanged();
                // TODO: scroll to the end of the list
                //lvComments.smoothScrollToPosition(comments.size());
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject object) {
            }
        });
    }
}
