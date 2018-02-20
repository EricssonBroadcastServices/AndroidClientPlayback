package net.ericsson.emovs.playback.techs.ExoPlayer;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import net.ericsson.emovs.playback.R;
import net.ericsson.emovs.utilities.interfaces.IEntitledPlayer;
import net.ericsson.emovs.utilities.interfaces.IPlayer;
import net.ericsson.emovs.utilities.models.EmpProgram;

/**
 * Created by Joao Coelho on 2018-01-24.
 */

public class HookedDurationTextView extends android.support.v7.widget.AppCompatTextView {
    IPlayer player;

    public HookedDurationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindPlayer(IPlayer player) {
        this.player = player;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (player instanceof IEntitledPlayer) {
            IEntitledPlayer entitledPlayer = (IEntitledPlayer) player;
            EmpProgram currentProgram = entitledPlayer.getCurrentProgram();
            if (currentProgram != null && currentProgram.getDuration() != null ) {
                String newDuration = currentProgram.getDurationAsString();
                if (newDuration.equals(text) == false) {
                    setText(newDuration);
                }
                else {
                    super.onTextChanged(text, start, lengthBefore, lengthAfter);
                }
                return;
            }
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }
}
