package com.jwplayer.opensourcedemo;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.events.listeners.VideoPlayerEvents;

public class MainActivity extends FragmentActivity implements VideoPlayerEvents.OnFullscreenListener {

    /**
     * Reference to the JW Player View
     */
    JWPlayerView mPlayerView;

    /**
     * An instance of our event handling class
     */
    private JWEventHandler mEventHandler;
    private RelativeLayout mPlayerContainer;
    private View mViewLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayerView = (JWPlayerView) findViewById(R.id.jwplayer);
        mPlayerContainer = (RelativeLayout) findViewById(R.id.player_container);
        mViewLandscape = findViewById(R.id.landscape_view);
        TextView outputTextView = (TextView) findViewById(R.id.output);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            addPortraitView();
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            addLandScapeView();
        }

        // Handle hiding/showing of ActionBar
        mPlayerView.addOnFullscreenListener(this);

        // Instantiate the JW Player event handler class
        mEventHandler = new JWEventHandler(mPlayerView, outputTextView);

        // Load a media source
        PlayerConfig.Builder playerConfigBuild = new PlayerConfig.Builder(this)
                .autostart(true)
                .file(Consts.VIDEO_URL)
                .skinUrl(Consts.JWPLAYER_SKIN)
                .skinName(Consts.JWPLAYER_SKIN_NAME);
        mPlayerView.setup(playerConfigBuild.build());
        mPlayerView.play();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Set fullscreen when the device is rotated to landscape
        mPlayerView.setFullscreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE, true);
        super.onConfigurationChanged(newConfig);
        hideSystemUI();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            addPortraitView();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            addLandScapeView();
        }
    }

    private void addLandScapeView() {
        LinearLayout.LayoutParams playerParentLayoutParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        RelativeLayout.LayoutParams playerLayoutParam = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        mPlayerView.setFullscreen(true, true);
        mPlayerView.setLayoutParams(playerLayoutParam);
        mPlayerContainer.setLayoutParams(playerParentLayoutParam);
        showLandscapeView(true);
    }

    private void addPortraitView() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = (size.x * 9) / 16;
        LinearLayout.LayoutParams playerParentLayoutParam = new LinearLayout.LayoutParams(width, height);
        RelativeLayout.LayoutParams playerLayoutParam = new RelativeLayout.LayoutParams(width, height);
        if (mPlayerContainer != null) {
            mPlayerContainer.setLayoutParams(playerParentLayoutParam);
        }
        mPlayerView.setFullscreen(false, true);
        mPlayerView.setLayoutParams(playerLayoutParam);
        showLandscapeView(false);
    }

    private void showLandscapeView(boolean show) {
        mViewLandscape.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        mPlayerView.onResume();
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
        mPlayerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
        mPlayerView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Handles JW Player going to and returning from fullscreen by hiding the ActionBar
     *
     * @param fullscreen true if the player is fullscreen
     */
    @Override
    public void onFullscreen(boolean fullscreen) {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            if (fullscreen) {
//                actionBar.hide();
//            } else {
//                actionBar.show();
//            }
//        }
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        int visibilityOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            visibilityOptions = visibilityOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        getWindow().getDecorView().setSystemUiVisibility(visibilityOptions);
    }
}
