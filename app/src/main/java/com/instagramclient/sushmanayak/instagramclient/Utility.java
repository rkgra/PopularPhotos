package com.instagramclient.sushmanayak.instagramclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.instagramclient.sushmanayak.instagramclient.adapter.InstagramPhotosAdapter;
import com.instagramclient.sushmanayak.instagramclient.data.Comment;
import com.instagramclient.sushmanayak.instagramclient.data.InstagramPhoto;
import com.instagramclient.sushmanayak.instagramclient.data.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by SushmaNayak on 9/17/2015.
 */
public class Utility {

    public static final int POPULAR_PHOTOS = 0;
    public static final int TAGGED_PHOTOS = 1;
    public static final int USER_PHOTOS = 2;
    public static final int LOCATION_PHOTOS = 3;

    public static int displayWidth;

    public static final String TAGGED_PHOTO_BASE_URL = "https://api.instagram.com/v1/tags/";
    public static final String USER_PHOTO_BASE_URL = "https://api.instagram.com/v1/users/";
    public static final String USER_SEARCH_BASE_URL = "https://api.instagram.com/v1/users/search?q=";
    public static final String LOCATION_BASE_URL = "https://api.instagram.com/v1/locations/";
    public static final String POPULAR_PHOTOS_URL = "https://api.instagram.com/v1/media/popular?client_id=";
    public static final String COMMENTS_BASE_URL = "https://api.instagram.com/v1/media/";
    public static final String DATA_ARRAY = "data";
    public static final String USER_OBJ = "user";
    public static final String USERNAME = "username";
    public static final String PROFILE_PIC = "profile_picture";
    public static final String CAPTION_OBJ = "caption";
    public static final String TEXT = "text";
    public static final String TIMESTAMP = "created_time";
    public static final String TYPE = "type";
    public static final String IMAGES = "images";
    public static final String VIDEOS = "videos";
    public static final String STD_RESOLUTION = "standard_resolution";
    public static final String IMG_URL = "url";
    public static final String PHOTO_LIKES = "likes";
    public static final String COUNT = "count";
    public static final String COMMENTS_OBJ = "comments";
    public static final String FROM_USER = "from";
    public static final String ID = "id";
    public static final String LOCATION = "location";
    public static final String LOCATION_NAME = "name";
    public static final String LINK = "link";

    public static InstagramPhoto getInstagramPhoto(JSONObject photoJSON) {

        InstagramPhoto photo = new InstagramPhoto();

        try {

            photo.user = new User(photoJSON.getJSONObject(USER_OBJ).getString(ID), photoJSON.getJSONObject(USER_OBJ).getString(USERNAME),
                    photoJSON.getJSONObject(USER_OBJ).getString(PROFILE_PIC));

            if (photoJSON.get(LOCATION) != null && !photoJSON.get(LOCATION).toString().equals("null"))
            {
                photo.location = photoJSON.getJSONObject(LOCATION).getString(LOCATION_NAME);
                photo.locationID = photoJSON.getJSONObject(LOCATION).getString(ID);
            }

            if (photoJSON.get(CAPTION_OBJ) != null && !photoJSON.get(CAPTION_OBJ).toString().equals("null"))
                photo.caption = photoJSON.getJSONObject(CAPTION_OBJ).getString(TEXT);

            photo.timestamp = photoJSON.getInt(TIMESTAMP);
            photo.mediaType = photoJSON.getString(TYPE);
            photo.instagramLink = photoJSON.getString(LINK);
            if (photo.mediaType.equals("video")) {
                if (photoJSON.get(VIDEOS) != null)
                    photo.videoUrl = photoJSON.getJSONObject(VIDEOS).getJSONObject(STD_RESOLUTION).getString("url");
            }
            photo.imageId = photoJSON.getString(ID);
            photo.imageUrl = photoJSON.getJSONObject(IMAGES).getJSONObject(STD_RESOLUTION).getString(IMG_URL);
            photo.likesCount = photoJSON.getJSONObject(PHOTO_LIKES).getInt(COUNT);
            photo.commentsCount = photoJSON.getJSONObject(COMMENTS_OBJ).getInt(COUNT);
            JSONArray commentsJSON = photoJSON.getJSONObject(COMMENTS_OBJ).getJSONArray(DATA_ARRAY);

            photo.comments = new ArrayList<>();

            // Read only 3 comments
            for (int j = 0; j < 3; j++) {
                if (commentsJSON.length() <= j)
                    break;
                JSONObject commentJSON = commentsJSON.getJSONObject(j);
                Comment comment = new Comment();
                comment.commentText = commentJSON.getString(TEXT);
                comment.commentFrom = new User(commentJSON.getJSONObject(FROM_USER).getString(ID), commentJSON.getJSONObject(FROM_USER).getString(USERNAME), commentJSON.getJSONObject(FROM_USER).getString(PROFILE_PIC));
                photo.comments.add(comment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photo;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getUserId(JSONObject response) {
        String userId = null;
        try {
            if (response.get(DATA_ARRAY) != null) {
                JSONArray usersJSON = response.getJSONArray(DATA_ARRAY);
                if (usersJSON.length() > 0) {
                    userId = usersJSON.getJSONObject(0).getString(ID);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userId;
    }

    public void SetText(Context context, String username, String comment, TextView view) {

        String textToAdd = username + " " + comment;
        view.setTextColor(context.getResources().getColor(R.color.colorBlack));

        StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

        SpannableStringBuilder ssb = new SpannableStringBuilder(textToAdd);
        // Make the username bold and clickable
        ssb.setSpan(
                new CalloutLink(context),
                0,
                username.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(bss, 0, username.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        addSpans(context, textToAdd, '#', ssb);
        addSpans(context, textToAdd, '@', ssb);

        view.setText(ssb, TextView.BufferType.EDITABLE);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void addSpans(Context context, String body, char prefix, SpannableStringBuilder ssb) {

        Pattern pattern = Pattern.compile(prefix + "\\w+");
        Matcher matcher = pattern.matcher(body);

        // Check all occurrences
        while (matcher.find()) {
            ssb.setSpan(
                    new CalloutLink(context),
                    matcher.start(),
                    matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public class CalloutLink extends ClickableSpan {
        Context mContext;

        public CalloutLink(Context context) {
            super();
            mContext = context;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(mContext.getResources().getColor(R.color.userNameColor));
        }

        @Override
        public void onClick(View widget) {
            TextView tv = (TextView) widget;
            Spanned s = (Spanned) tv.getText();
            int start = s.getSpanStart(this);
            int end = s.getSpanEnd(this);
            String theWord = s.subSequence(start, end).toString();
            if (theWord.startsWith("#"))
                ((InstagramPhotosAdapter.PhotoClickHandlers) mContext).FetchNewPhotos(Utility.TAGGED_PHOTOS, theWord.substring(1), null);
            else if (theWord.startsWith("@"))
                ((InstagramPhotosAdapter.PhotoClickHandlers) mContext).FetchNewPhotos(Utility.USER_PHOTOS, theWord.substring(1), null);
            else
                ((InstagramPhotosAdapter.PhotoClickHandlers) mContext).FetchNewPhotos(Utility.USER_PHOTOS, theWord, null);
        }
    }

    public static String formatTime(String time) {
        time = time.replace("ago", "").replace(" weeks", "w").replace(" week", "w").replace(" hours", "h").replace(" hour", "h")
                .replace(" days", "d").replace(" day", "d").replace(" minutes", "m").replace(" minute", "m").replace(" seconds", "s").replace(" second", "s");
        return time;
    }
}
