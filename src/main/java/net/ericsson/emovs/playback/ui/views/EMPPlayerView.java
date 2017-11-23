package net.ericsson.emovs.playback.ui.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import net.ericsson.emovs.playback.EMPPlayer;
import net.ericsson.emovs.playback.factories.EMPPlayerFactory;

/**
 * This view binds a EMPPlayer instance and is used to play content. You can use this in the layout XML. Example:
 * <p>
 *    <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *        android:id="@+id/empplayer_layout"
 *        android:layout_width="match_parent"
 *        android:layout_height="match_parent"
 *        android:background="@color/black"
 *        android:orientation="vertical">
 *            <net.ericsson.emovs.playback.ui.views.EMPPlayerView xmlns:android="http://schemas.android.com/apk/res/android"
 *                android:id="@+id/empplayer_layout_1"
 *                android:layout_width="match_parent"
 *                android:layout_height="0dp"
 *                android:layout_weight="1">
 *            </net.ericsson.emovs.playback.ui.views.EMPPlayerView>
 *    </LinearLayout>
 * </p>
 *
 * Created by Joao Coelho on 2017-09-29.
 */
public class EMPPlayerView extends RelativeLayout {
    EMPPlayer player;

    public EMPPlayerView(Context context) {
        super(context);
        this.player = EMPPlayerFactory.build((Activity) getContext(), this);
    }

    public EMPPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.player = EMPPlayerFactory.build((Activity) getContext(), this);
    }

    public EMPPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.player = EMPPlayerFactory.build((Activity) getContext(), this);
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
        this.player = EMPPlayerFactory.build((Activity) getContext(), this);
    }

    /**
     * Use this method if you need to get the EMPPlayer instance and call the api directly
     *
     * @return
     */
    public EMPPlayer getPlayer() {
        return this.player;
    }
}
