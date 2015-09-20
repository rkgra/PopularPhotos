package com.instagramclient.sushmanayak.instagramclient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.instagramclient.sushmanayak.instagramclient.adapter.InstagramPhotosAdapter;
import com.instagramclient.sushmanayak.instagramclient.data.InstagramPhoto;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import org.apache.http.Header;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends Fragment {

    Context mContext;
    ArrayList<InstagramPhoto> photos;
    InstagramPhotosAdapter instagramAdapter;
    ListView lvPhotos;
    SwipeRefreshLayout swipeContainer;
    int mFetchType = Utility.POPULAR_PHOTOS;
    String mFetchParameter1;
    String mFetchParameter2;
    final static String FETCH_TYPE = "FetchType";
    final static String PARAM1 = "FetchParameter1";
    final static String PARAM2 = "FetchParameter2";

    public PhotoFragment(){}

    public static PhotoFragment newInstance(Context context, int fetchType, String parameter1, String parameter2) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt(FETCH_TYPE, fetchType);
        args.putString(PARAM1, parameter1);
        args.putString(PARAM2, parameter2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mContext = getActivity();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        lvPhotos = (ListView) view.findViewById(R.id.lvPhotos);
        photos = new ArrayList<>();

        Bundle args = getArguments();
        if(args != null)
        {
            mFetchType = args.getInt(FETCH_TYPE);
            mFetchParameter1 = args.getString(PARAM1);
            mFetchParameter2 = args.getString(PARAM2);
        }

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchInstagramPhotos();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        fetchInstagramPhotos();
        instagramAdapter = new InstagramPhotosAdapter(mContext, photos);
        lvPhotos.setAdapter(instagramAdapter);

        return view;
    }

    private void fetchInstagramPhotos() {

        if (!Utility.isNetworkAvailable(mContext)) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.noNetwork), Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Utility.POPULAR_PHOTOS_URL + getString(R.string.api_key);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);

        if (mFetchType == Utility.TAGGED_PHOTOS) {
            url = Utility.TAGGED_PHOTO_BASE_URL + mFetchParameter1 + "/media/recent?client_id=" + getString(R.string.api_key);
            actionBar.setTitle("#" + mFetchParameter1);
        } else if (mFetchType == Utility.USER_PHOTOS) {
            if (mFetchParameter2 != null) {
                url = Utility.USER_PHOTO_BASE_URL + mFetchParameter2 + "/media/recent?client_id=" + getString(R.string.api_key);
            } else {
                fetchUserDetails(mFetchParameter1);
                return;
            }
            actionBar.setTitle("@" + mFetchParameter1);
        } else if (mFetchType == Utility.LOCATION_PHOTOS) {
            url = Utility.LOCATION_BASE_URL + mFetchParameter2 + "/media/recent?client_id=" + getString(R.string.api_key);
            actionBar.setTitle("Location: " + mFetchParameter1);
        } else {
            // Home page, display logo
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(mContext.getResources().getString(R.string.app_name));
            actionBar.setDisplayUseLogoEnabled(true);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new JsonHttpResponseHandler() {

                    //Success
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            photos.clear();
                            JSONArray photosJSon = response.getJSONArray(Utility.DATA_ARRAY);

                            for (int i = 0; i < photosJSon.length(); i++) {
                                JSONObject photoJSON = photosJSon.getJSONObject(i);
                                InstagramPhoto photo = Utility.getInstagramPhoto(photoJSON);
                                photos.add(photo);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        swipeContainer.setRefreshing(false);
                        instagramAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                        swipeContainer.setRefreshing(false);
                        Toast.makeText(mContext,"User account is private", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void fetchUserDetails(String userName) {

        String url = Utility.USER_SEARCH_BASE_URL + userName + "&count=1&client_id=" + getString(R.string.api_key);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, JSONObject response) {

                // Get the user account Id
                mFetchParameter2 = Utility.getUserId(response);
                if (mFetchParameter2 != null)
                    fetchInstagramPhotos();
                else
                    Toast.makeText(mContext, "User is private", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, Throwable throwable, JSONObject object) {
            }
        });
    }
}
