package com.instagramclient.sushmanayak.instagramclient;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.instagramclient.sushmanayak.instagramclient.adapter.InstagramPhotosAdapter;

public class PhotosActivity extends AppCompatActivity implements InstagramPhotosAdapter.PhotoClickHandlers {

    FrameLayout photoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        photoContainer = (FrameLayout) findViewById(R.id.photoContainer);
        CalculateScreenWidth();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.photoContainer) == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.photoContainer, PhotoFragment.newInstance(this, Utility.POPULAR_PHOTOS, null, null))
                    .commit();
        }

    }

    public void CalculateScreenWidth() {
        // Picasso uses the width in pixels, set it to the device's width
        Utility.displayWidth = getResources().getDisplayMetrics().widthPixels;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photos, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void ViewComments(String photoId) {
        CommentsFragment fragment = CommentsFragment.newInstance(this, photoId);
        AddTransaction(fragment);
    }

    @Override
    public void FetchNewPhotos(int type, String param1, String param2) {
        PhotoFragment frag = PhotoFragment.newInstance(this, type, param1, param2);
        AddTransaction(frag);
    }

    private void AddTransaction(Fragment fragment)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.replace(R.id.photoContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        fm.executePendingTransactions();
    }
}
