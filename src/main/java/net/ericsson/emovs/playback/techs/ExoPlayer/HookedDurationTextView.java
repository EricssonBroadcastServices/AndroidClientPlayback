package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.util.AttributeSet;

import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.time.DateTimeParser;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDurationTextView extends android.support.v7.widget.AppCompatTextView {
    public enum Mode { DURATION, TIME_LEFT };
    IPlayer player;

    public HookedDurationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (player != null && player instanceof IEntitledPlayer) {
            if (getMode() == Mode.DURATION) {
                setValueDuration(text);
            }
            else {
                setValueTimeLeft(text);
            }
            return;
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    protected void setValueDuration(CharSequence text){
        IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
        long[] seekableTimeRange = player.getSeekTimeRange();
        EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
        if (currentProgram != null && currentProgram.getDuration() != null && seekableTimeRange != null) {
            long liveDuration = seekableTimeRange[1] - currentProgram.startDateTime.getMillis();
            String newDuration = DateTimeParser.formatDisplayTime(Math.min(currentProgram.getDuration(), liveDuration));
            if (newDuration.equals(text) == false) {
                setText(newDuration);
                invalidate();
            }
        }
    }

    protected void setValueTimeLeft(CharSequence text) {
        IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
        long playheadTime = player.getPlayheadTime();
        EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
        if (currentProgram != null && currentProgram.getDuration() != null) {
            long timeLeft = currentProgram.endDateTime.getMillis() - playheadTime;
            if (timeLeft < 0) {
                timeLeft = 0;
            }
            String newDuration = "-" + DateTimeParser.formatDisplayTime(timeLeft);
            if (newDuration.equals(text) == false) {
                setText(newDuration);
                invalidate();
            }
        }
    }

    protected Mode getMode() {
        if (player != null && player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            if (currentProgram != null && currentProgram.liveNow()) {
                return Mode.TIME_LEFT;
            }
        }
        return Mode.DURATION;
    }
}
