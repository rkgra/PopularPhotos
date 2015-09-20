package com.instagramclient.sushmanayak.instagramclient.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.instagramclient.sushmanayak.instagramclient.VideoPlayerActivity;
import com.instagramclient.sushmanayak.instagramclient.data.Comment;
import com.instagramclient.sushmanayak.instagramclient.data.InstagramPhoto;
import com.instagramclient.sushmanayak.instagramclient.R;
import com.instagramclient.sushmanayak.instagramclient.Utility;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SushmaNayak on 9/15/2015.
 */
public class InstagramPhotosAdapter extends ArrayAdapter<InstagramPhoto> {

    public interface PhotoClickHandlers {

        void ViewComments(String photoId);

        void FetchNewPhotos(int type, String param1, String param2);
    }

    static class ViewHolder {
        ImageView photoView;
        TextView caption;
        TextView tvUsername;
        TextView tvLocation;
        ImageView profilePhoto;
        ImageView btnLike;
        ImageView comment;
        ImageView setting;
        TextView addComment;
        TextView timestamp;
        TextView allComments;
        TextView photoLikes;
        ImageView videoIcon;
        LinearLayout commentsContainer;
    }

    Context mContext;
    Utility mUtility;

    public InstagramPhotosAdapter(Context context, List<InstagramPhoto> objects) {
        super(context, 0, objects);
        mContext = context;
        mUtility = new Utility();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InstagramPhoto photo = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_photo, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.photoView = (ImageView) convertView.findViewById(R.id.ImageViewPhoto);
            viewHolder.photoView.setOnClickListener(videoClickListener);
            viewHolder.videoIcon = (ImageView) convertView.findViewById(R.id.videoIcon);
            viewHolder.caption = (TextView) convertView.findViewById(R.id.caption);
            viewHolder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsername);
            viewHolder.tvUsername.setOnClickListener(userNameClickListener);
            viewHolder.tvLocation = (TextView) convertView.findViewById(R.id.location);
            viewHolder.tvLocation.setOnClickListener(locationClickListener);
            viewHolder.profilePhoto = (ImageView) convertView.findViewById(R.id.profilePhoto);
            viewHolder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
            viewHolder.allComments = (TextView) convertView.findViewById(R.id.allComments);
            viewHolder.commentsContainer = (LinearLayout) convertView.findViewById(R.id.commentsContainer);
            viewHolder.allComments.setOnClickListener(allCommentsListener);
            viewHolder.comment = (ImageView) convertView.findViewById(R.id.comment);
            viewHolder.comment.setOnClickListener(allCommentsListener);
            viewHolder.addComment = (TextView) convertView.findViewById(R.id.addComment);
            viewHolder.addComment.setOnClickListener(allCommentsListener);
            viewHolder.setting = (ImageView) convertView.findViewById(R.id.setting);
            viewHolder.setting.setOnClickListener(overflowMenuListener);
            viewHolder.btnLike = (ImageView) convertView.findViewById(R.id.btnLike);
            viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateHeartButton((ImageView) view, true);
                }
            });
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        Picasso.with(mContext).load(photo.user.userProfileUrl).resize(40, 40).centerCrop().into(viewHolder.profilePhoto);

        // Do not display the 'View all comments' button if there are less than 3 comments
        if (photo.commentsCount > 3) {
            viewHolder.allComments.setText("View all " + photo.commentsCount + " comments");
            viewHolder.allComments.setVisibility(View.VISIBLE);
            viewHolder.allComments.setTag(photo.imageId);
        } else {
            viewHolder.allComments.setVisibility(View.GONE);
        }
        viewHolder.comment.setTag(photo.imageId);
        viewHolder.addComment.setTag(photo.imageId);
        viewHolder.setting.setTag(photo.instagramLink);

        viewHolder.btnLike.setImageResource(R.drawable.heart);

        // If the media is a video, overlay the video icon
        if (photo.mediaType.equals("video")) {
            viewHolder.videoIcon.setVisibility(View.VISIBLE);
            viewHolder.photoView.setTag(photo.videoUrl);
        } else {
            viewHolder.videoIcon.setVisibility(View.GONE);
            viewHolder.photoView.setTag(null);
        }

        // Display the number of likes
        viewHolder.photoLikes = (TextView) convertView.findViewById(R.id.photoLikes);
        viewHolder.photoLikes.setText(" " +
                NumberFormat.getNumberInstance(mContext.getResources().getConfiguration().locale).format(photo.likesCount)
                + " likes");

        viewHolder.tvUsername.setText(photo.user.username);
        viewHolder.tvUsername.setTag(photo.user.userId);

        // Display the location when available
        if (photo.location == null)
            viewHolder.tvLocation.setVisibility(View.GONE);
        else {
            viewHolder.tvLocation.setVisibility(View.VISIBLE);
            viewHolder.tvLocation.setText(photo.location);
            viewHolder.tvLocation.setTag(photo.locationID);
        }
        // Resize to device's width and set 0 for height to preserve aspect ratio
        Picasso.with(mContext).load(photo.imageUrl).placeholder(R.drawable.loading).resize(Utility.displayWidth, 0).into(viewHolder.photoView);
        String time = DateUtils.getRelativeTimeSpanString((long) photo.timestamp * 1000, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        viewHolder.timestamp.setText(Utility.formatTime(time));

        // Add the caption
        mUtility.SetText(mContext, photo.user.username, photo.caption, viewHolder.caption);

        // Add the last 3 comments
        viewHolder.commentsContainer.removeAllViews();
        if (photo.comments != null) {
            for (Comment comment : photo.comments) {
                TextView view = new TextView(mContext);
                mUtility.SetText(mContext, comment.commentFrom.username, comment.commentText, view);
                viewHolder.commentsContainer.addView(view);
            }
        }
        return convertView;
    }

    View.OnClickListener overflowMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            PopupMenu popup = new PopupMenu(mContext, view);
            popup.getMenuInflater().inflate(R.menu.popupmenu,
                    popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    // Copy the photo's link to clipboard
                    ClipboardManager clipboard = (ClipboardManager) ((Activity) mContext).getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Instagram Link", view.getTag().toString()));

                    Toast.makeText(mContext, mContext.getResources().getString(R.string.copytoClipboard), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            popup.show();
        }
    };

    View.OnClickListener videoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null) {
                Intent intent = new Intent(mContext.getApplicationContext(), VideoPlayerActivity.class);
                intent.putExtra("url", view.getTag().toString());
                mContext.startActivity(intent);
            }
        }
    };

    private View.OnClickListener userNameClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((PhotoClickHandlers) mContext).FetchNewPhotos(Utility.USER_PHOTOS, ((TextView) view).getText().toString(), view.getTag().toString());
        }
    };

    private View.OnClickListener allCommentsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((PhotoClickHandlers) mContext).ViewComments(view.getTag().toString());
        }
    };

    private View.OnClickListener locationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((PhotoClickHandlers) mContext).FetchNewPhotos(Utility.LOCATION_PHOTOS, ((TextView) view).getText().toString(), view.getTag().toString());
        }
    };

    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
    private final Map<View, AnimatorSet> likeAnimations = new HashMap<>();

    private void resetLikeAnimationState(ImageView holder) {
        likeAnimations.remove(holder);
        //holder.setVisibility(View.GONE);
    }

    private void updateHeartButton(final ImageView holder, boolean animated) {
        if (animated) {
            if (!likeAnimations.containsKey(holder)) {
                AnimatorSet animatorSet = new AnimatorSet();
                likeAnimations.put(holder, animatorSet);

                ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder, "rotation", 0f, 360f);
                rotationAnim.setDuration(300);
                rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder, "scaleX", 0.2f, 1f);
                bounceAnimX.setDuration(300);
                bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder, "scaleY", 0.2f, 1f);
                bounceAnimY.setDuration(300);
                bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
                bounceAnimY.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        holder.setImageResource(R.drawable.heart_red);
                    }
                });

                animatorSet.play(rotationAnim);
                animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetLikeAnimationState(holder);
                    }
                });

                animatorSet.start();
            }
        }
    }

}
