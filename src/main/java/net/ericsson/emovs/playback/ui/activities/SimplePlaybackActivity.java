package net.ericsson.emovs.playback.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.ebs.android.exposure.interfaces.IPlayable;
import com.ebs.android.utilities.ViewHelper;

import net.ericsson.emovs.playback.EmptyPlaybackEventListener;
import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.playback.ui.views.EMPPlayerView;

import java.util.ArrayList;
import java.util.LinkedList;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * Created by Joao Coelho on 2017-09-21.
 */

public class SimplePlaybackActivity extends Activity {
    protected final String PLAYABLE_ARGUMENT_NAME = "playable";

    protected LinkedList<IPlayable> empPlaylist;
    protected ArrayList<EMPPlayerView> playerViews;

    protected Integer contentViewLayoutId;
    protected PlaybackProperties properties;

    public SimplePlaybackActivity() {
        this.empPlaylist = new LinkedList<>();
        this.properties = PlaybackProperties.DEFAULT;
        this.contentViewLayoutId = null;
    }

    public SimplePlaybackActivity(PlaybackProperties properties) {
        this.contentViewLayoutId = null;
        this.empPlaylist = new LinkedList<>();
        if (properties == null) {
            this.properties = PlaybackProperties.DEFAULT;
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

        refresh();
        extractExtras();
        startPlayback();
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

    protected void addPlayable(IPlayable playable) {
        empPlaylist.add(playable);
    }

    protected void clearPlaylist() {
        empPlaylist.clear();
    }

    protected void bindContentView(int contentViewLayoutId) {
        this.contentViewLayoutId = contentViewLayoutId;
    }

    protected void refresh() {
        this.playerViews = ViewHelper.getViewsFromViewGroup(getWindow().getDecorView(), EMPPlayerView.class);
    }

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

    protected void startPlayback() {
        if (this.playerViews == null || empPlaylist.size() == 0) {
            return;
        }
        for (EMPPlayerView view : this.playerViews) {
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
