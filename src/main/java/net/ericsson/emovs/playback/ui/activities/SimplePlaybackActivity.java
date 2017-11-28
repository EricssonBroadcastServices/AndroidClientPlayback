package net.ericsson.emovs.playback.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import net.ericsson.emovs.playback.interfaces.ControllerVisibility;
import net.ericsson.emovs.playback.ui.adapters.LanguageAdapter;
import net.ericsson.emovs.utilities.interfaces.IPlayable;
import net.ericsson.emovs.utilities.ui.ViewHelper;

import net.ericsson.emovs.playback.EmptyPlaybackEventListener;
import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.playback.ui.views.EMPPlayerView;

import java.util.ArrayList;
import java.util.LinkedList;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * <p>
 *     This class is a base Activity that can be easily extended in order to achieve a quick and functional playback activity.
 *     To play an asset just pass the playable in the Intent when starting the activity.
 * </p>
 * <p>
 *     The default layout includes a EMPPlayerView that matches the parent layout dimensions.
 *     However it is possible to have custom layouts with as many players as you want
 * </p>
 *
 * Created by Joao Coelho on 2017-09-21.
 */
public class SimplePlaybackActivity extends AppCompatActivity {
    protected final String PLAYABLE_ARGUMENT_NAME = "playable";

    protected LinkedList<IPlayable> empPlaylist;
    protected ArrayList<EMPPlayerView> playerViews;

    protected Integer contentViewLayoutId;
    protected PlaybackProperties properties;

    /**
     * Default constructor
     */
    public SimplePlaybackActivity() {
        this.empPlaylist = new LinkedList<>();
        this.properties = PlaybackProperties.DEFAULT;
        this.contentViewLayoutId = null;
    }

    /**
     * Constructor with specific playback properties
     *
     * @param properties
     */
    public SimplePlaybackActivity(PlaybackProperties properties) {
        this.contentViewLayoutId = null;
        this.empPlaylist = new LinkedList<>();
        if (properties == null) {
            this.properties = PlaybackProperties.DEFAULT;
        }
        else {
            this.properties = properties;
        }
    }

    public ArrayList<EMPPlayerView> getPlayerViews() {
        return playerViews;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(FEATURE_NO_TITLE);

        if (this.contentViewLayoutId == null) {
            setContentView(R.layout.empview);
        }
        else {
            setContentView(this.contentViewLayoutId);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle("");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xcc000000));

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        refresh();
        extractExtras();
        startPlayback();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            View decorView = getWindow().getDecorView();
            int uiOptions = decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION & ~View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);

        MenuItem itemAudio = menu.findItem(R.id.audio_tracks);
        final Spinner audioTrackSpinner = (Spinner) itemAudio.getActionView();
        LanguageAdapter audiosAdapter = new LanguageAdapter(audioTrackSpinner, this, LanguageAdapter.TrackType.AUDIO, null);
        audioTrackSpinner.setAdapter(audiosAdapter);

        MenuItem itemText = menu.findItem(R.id.subs_tracks);
        final Spinner textTrackSpinner = (Spinner) itemText.getActionView();
        LanguageAdapter subsAdapter = new LanguageAdapter(textTrackSpinner, this, LanguageAdapter.TrackType.SUBS, null);
        textTrackSpinner.setAdapter(subsAdapter);

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAllEmpClients();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        pauseAllEmpClients();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseAllEmpClients();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllEmpClients();
    }

    /**
     * This method adds a playable to the playback queue
     *
     * @param playable
     */
    protected void addPlayable(IPlayable playable) {
        empPlaylist.add(playable);
    }

    /**
     * This method clears the playback queue
     */
    protected void clearPlaylist() {
        empPlaylist.clear();
    }

    /**
     * Use this method to bind a custom layout (note that the xml must have at least one reference to a EMPPlayerView)
     *
     * @param contentViewLayoutId
     */
    protected void bindContentView(int contentViewLayoutId) {
        this.contentViewLayoutId = contentViewLayoutId;
    }

    /**
     * Use this method to get and cache a list of EMPPlayerView instances present in the current layout
     */
    protected void refresh() {
        this.playerViews = ViewHelper.getViewsFromViewGroup(getWindow().getDecorView(), EMPPlayerView.class);
    }

    /**
     * This method gets the playables sent to the activity as extras of the Intent and queues them for playback
     */
    protected void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(PLAYABLE_ARGUMENT_NAME)) {
            Object playableCandidate = extras.get(PLAYABLE_ARGUMENT_NAME);
            if (playableCandidate instanceof IPlayable) {
                empPlaylist.add((IPlayable) playableCandidate);
            }
            else if (playableCandidate instanceof ArrayList) {
                ArrayList<Object> playlist = (ArrayList<Object>) playableCandidate;
                for (Object item : playlist) {
                    if (item instanceof IPlayable) {
                        empPlaylist.add((IPlayable) item);
                    }
                }
            }
        }
    }

    /**
     * This method assigns a playable (if available) for each EMPPlayerView available in the current layout and starts playback
     */
    protected void startPlayback() {
        if (this.playerViews == null || empPlaylist.size() == 0) {
            return;
        }
        for (final EMPPlayerView view : this.playerViews) {
            if(view == null || view.getPlayer() == null) {
                continue;
            }
            final Context self = getApplicationContext();
            view.getPlayer().clearListeners();
            view.getPlayer().addListener(new EmptyPlaybackEventListener(view.getPlayer()) {
                @Override
                public void onError(int errorCode, String errorMessage) {
                    Toast.makeText(self, errorMessage, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoad() {
                    Toolbar toolbar = findViewById(R.id.toolbar);
                    int visibleCount = 0;
                    MenuItem audioMenu = toolbar.getMenu().findItem(R.id.audio_tracks);
                    Spinner audioSpinner = (Spinner) audioMenu.getActionView();
                    if(view.getPlayer().getAudioTracks() != null && view.getPlayer().getAudioTracks().length > 1) {
                        ((LanguageAdapter) audioSpinner.getAdapter()).setLanguages(view.getPlayer().getAudioTracks());
                        audioMenu.setVisible(true);
                        visibleCount++;
                    }

                    MenuItem subsMenu = toolbar.getMenu().findItem(R.id.subs_tracks);
                    Spinner subsSpinner = (Spinner) subsMenu.getActionView();
                    if (view.getPlayer().getTextTracks() != null && view.getPlayer().getAudioTracks().length > 0) {
                        ((LanguageAdapter) subsSpinner.getAdapter()).setLanguages(view.getPlayer().getTextTracks());
                        subsMenu.setVisible(true);
                        visibleCount++;
                    }

                    if (visibleCount == 0){
                       getSupportActionBar().hide();
                    }
                }


                @Override
                public void onControllerVisibility(ControllerVisibility visibility) {
                    if (visibility == ControllerVisibility.Hidden) {
                        getSupportActionBar().hide();
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            View decorView = getWindow().getDecorView();
                            int uiOptions = decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                    }
                    else if (view.getPlayer().getAudioTracks() != null && view.getPlayer().getAudioTracks().length > 1  &&
                             view.getPlayer().getTextTracks() != null && view.getPlayer().getAudioTracks().length > 0) {
                        getSupportActionBar().show();
                    }
                }
            });
            view.getPlayer().play(empPlaylist.poll(), this.properties);
            if (empPlaylist.size() == 0) {
                break;
            }
        }
    }

    private void releaseAllEmpClients() {
        if (this.playerViews == null) {
            return;
        }
        for (EMPPlayerView view : this.playerViews) {
            if(view == null || view.getPlayer() == null) {
                continue;
            }
            view.getPlayer().release();
        }
    }

    protected void pauseAllEmpClients() {
        if (this.playerViews == null) {
            return;
        }
        for (EMPPlayerView view : this.playerViews) {
            if(view == null || view.getPlayer() == null) {
                continue;
            }
            view.getPlayer().pause();
        }
    }

}
