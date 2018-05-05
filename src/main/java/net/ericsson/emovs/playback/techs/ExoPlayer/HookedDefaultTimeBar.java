package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ui.DefaultTimeBar;

import net.ericsson.emovs.playback.Player;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.system.Utils;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDefaultTimeBar extends DefaultTimeBar {
    public static int LIVE_COLOR_THRESHOLD = 3000;
    protected final Paint liveScrubberPaint = new Paint();
    protected final Paint defaultScrubberPaint = new Paint();

    IPlayer player;
    View liveLine;

    public HookedDefaultTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.defaultScrubberPaint.setColor(getDefaultScrubberColor(-1));
        this.liveScrubberPaint.setARGB(0xFF, 0xAA, 0x22, 0x22);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    public void setPosition(long position) {
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            updateScrubberColor();
            if (currentProgram != null && currentProgram.getDuration() != null) {
                long duration = currentProgram.getDuration();
                long newPosition = Math.min(duration, Math.max(0, player.getPlayheadTime() - currentProgram.startDateTime.getMillis()));
                super.setPosition(newPosition);
                return;
            }
        }
        super.setPosition(position);
    }

    @Override
    public void setBufferedPosition(long bufferedPosition) {
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            long[] bufferedTimeRange = player.getBufferedTimeRange();
            if (currentProgram != null && currentProgram.getDuration() != null && bufferedTimeRange != null) {
                long duration = currentProgram.getDuration();
                long newPosition = Math.min(duration, Math.max(0, bufferedTimeRange[1] - currentProgram.startDateTime.getMillis()));
                super.setBufferedPosition(newPosition);
                return;
            }
        }
        super.setBufferedPosition(bufferedPosition);
    }

    @Override
    public void setDuration(long duration) {
        double MAX_WEIGHT = 10000.0;
        if (player instanceof IEntitledPlayer) {
            updateScrubberColor();
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            long[] seekableTimeRange = player.getSeekTimeRange();
            if (currentProgram != null && currentProgram.getDuration() != null && seekableTimeRange != null) {
                long liveDuration = seekableTimeRange[1] - currentProgram.startDateTime.getMillis();
                long programDuration = currentProgram.getDuration();
                super.setDuration(Math.min(liveDuration, programDuration));

                if (currentProgram.liveNow()) {
                    long w = Math.round(liveDuration * MAX_WEIGHT / programDuration);
                    if (w < MAX_WEIGHT * 0.07) {
                        w = (long) (MAX_WEIGHT * 0.07);
                    }
                    if (w >= MAX_WEIGHT) {
                        this.liveLine.setVisibility(View.GONE);
                    }
                    else {
                        setWeight(w, (long) MAX_WEIGHT);
                    }
                }
                else {
                    this.liveLine.setVisibility(View.GONE);
                }
                return;
            }
        }
        super.setDuration(duration);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    public void setWeight(long w, long max) {
        // Set weight for seekbar
        ViewGroup.LayoutParams p = getLayoutParams();
        if (p instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) p;
            linearParams.weight = w;
        }
        this.setLayoutParams(p);

        if (this.liveLine != null) {
            // Set weight for live line
            p = this.liveLine.getLayoutParams();
            if (p instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) p;
                linearParams.weight = max - w;
            }
            this.liveLine.setLayoutParams(p);
            this.liveLine.setVisibility(View.VISIBLE);
        }
    }

    public void updateScrubberColor() {
        if (isNearLiveEdge()) {
            Utils.setPrivate(this, this.getClass().getSuperclass(), "scrubberPaint", liveScrubberPaint);
        }
        else {
            Utils.setPrivate(this, this.getClass().getSuperclass(), "scrubberPaint", defaultScrubberPaint);
        }
        // Utils.setPrivate(this, this.getClass().getSuperclass(), "scrubberPadding", 20);
    }

    public boolean isNearLiveEdge() {
        IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
        long[] seekableTimeRange = player.getSeekTimeRange();
        if (seekableTimeRange != null) {
            long playheadTime = entitledPlayer.getPlayheadTime();
            return seekableTimeRange[1] - playheadTime < LIVE_COLOR_THRESHOLD + Player.SAFETY_LIVE_DELAY;
        }
        return false;
    }

    public void bindLiveLine(View liveLine) {
        this.liveLine = liveLine;
    }
}
