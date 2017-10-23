package net.ericsson.emovs.playback.ui.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import net.ericsson.emovs.playback.EMPPlayer;

/**
 * Created by Joao Coelho on 2017-09-29.
 */

public class EMPPlayerView extends RelativeLayout {
    EMPPlayer player;

    public EMPPlayerView(Context context) {
        super(context);
        this.player = new EMPPlayer((Activity) getContext(), this);
    }

    public EMPPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.player = new EMPPlayer((Activity) getContext(), this);
    }

    public EMPPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.player = new EMPPlayer((Activity) getContext(), this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.player == null) {
            //this.player = new EMPPlayer((Activity) getContext(), this);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EMPPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.player = new EMPPlayer((Activity) getContext(), this);
    }

    public EMPPlayer getPlayer() {
        return this.player;
    }
}
