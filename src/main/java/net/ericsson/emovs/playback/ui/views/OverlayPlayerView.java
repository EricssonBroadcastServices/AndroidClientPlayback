package net.ericsson.emovs.playback.ui.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ebs.android.exposure.interfaces.IPlayable;

import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.playback.PlaybackProperties;
import net.ericsson.emovs.playback.R;

/**
 * Created by Joao Coelho on 2017-09-29.
 */

/* TODO: finish implementation with following features:
   TODO:     * swipe left to close player
   TODO:     * click to expand player
   TODO:     * back button to make player small again
   TODO:     * hide controls when player is small and show controls when player is big */
public class OverlayPlayerView extends FrameLayout {
    boolean isPlayerInjected = false;
    View overlayPlayerView;

    public OverlayPlayerView(Context context) {
        super(context);
    }

    public OverlayPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OverlayPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isPlayerInjected) {
            createInnerView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EMPPlayerView playerView = (EMPPlayerView) findViewById(R.id.empplayer_layout);
        if (playerView != null) {
            playerView.getPlayer().release();
        }
    }

    public void createInnerView() {
        isPlayerInjected = true;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.overlayPlayerView = inflater.inflate(R.layout.overlay_player, null);
        addView(overlayPlayerView);
    }

    public void play(IPlayable playable) {
        if (this.overlayPlayerView == null) {
            return;
        }
        RelativeLayout holderLayout = findViewById(R.id.empplayer_overlay_layout);
        holderLayout.setVisibility(View.VISIBLE);
        holderLayout.bringToFront();
        EMPPlayerView playerView = (EMPPlayerView) findViewById(R.id.empplayer_layout);
        if (playerView != null) {
            playerView.getPlayer().play(playable, PlaybackProperties.DEFAULT);
        }
    }
}
