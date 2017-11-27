package net.ericsson.emovs.playback.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

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
        getSupportActionBar().setTitle("");

        refresh();
        extractExtras();
        startPlayback();
    }

    LanguageAdapter audiosAdapter;
    LanguageAdapter subsAdapter;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);

        this.audiosAdapter = new LanguageAdapter(this, R.id.audio_tracks, null);
        this.subsAdapter = new LanguageAdapter(this, R.id.audio_tracks, null);

        MenuItem itemAudio = menu.findItem(R.id.audio_tracks);
        Spinner audioTrackSpinner = (Spinner) itemAudio.getActionView();
        /*audioTrackSpinner.setBackground(getResources().getDrawable(R.drawable.ic_audiotrack_white_24dp));
        audioTrackSpinner.setPadding(0,0,50,0);*/
        audioTrackSpinner.setAdapter(audiosAdapter);
        audioTrackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (final EMPPlayerView pView : playerViews) {
                    if(pView == null || pView.getPlayer() == null) {
                        continue;
                    }
                    pView.getPlayer().selectAudioTrack(audiosAdapter.getLangCode(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MenuItem itemText = menu.findItem(R.id.subs_tracks);
        Spinner textTrackSpinner = (Spinner) itemText.getActionView();
        //textTrackSpinner.setBackground(getResources().getDrawable(R.drawable.ic_subtitles_white_24dp));
        //textTrackSpinner.setPadding(0,0,50,0);
        textTrackSpinner.setAdapter(subsAdapter);
        textTrackSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                for (final EMPPlayerView pView : playerViews) {
                    if(pView == null || pView.getPlayer() == null) {
                        continue;
                    }
                    pView.getPlayer().selectTextTrack(subsAdapter.getLangCode(i));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

                    audiosAdapter.setLanguages(view.getPlayer().getAudioTracks());
                    if(view.getPlayer().getAudioTracks() != null) {
                        MenuItem item = toolbar.getMenu().getItem(0);
                        item.setVisible(true);
                    }

                    subsAdapter.setLanguages(view.getPlayer().getTextTracks());
                    if(view.getPlayer().getTextTracks() != null) {
                        MenuItem item = toolbar.getMenu().getItem(1);
                        item.setVisible(true);
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
