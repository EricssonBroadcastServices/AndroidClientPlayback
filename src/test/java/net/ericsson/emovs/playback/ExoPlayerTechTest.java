package net.ericsson.emovs.playback;

import net.ericsson.emovs.playback.drm.AnalyticsDrmCallbackListener;
import net.ericsson.emovs.playback.drm.IDrmCallbackListener;
import net.ericsson.emovs.playback.helpers.FakeEntitledPlayer;
import net.ericsson.emovs.playback.techs.ExoPlayer.ExoPlayerTech;
import net.ericsson.emovs.utilities.test.TestUtils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ExoPlayerTechTest {

    @Test
    public void ensureAnalyticsDrmGetsInstantiatedCorrectly() throws Exception {
        IDrmCallbackListener drmCallbackListener = (IDrmCallbackListener) TestUtils.callPrivateStaticMethod(ExoPlayerTech.class, "createMediaDrmCallbackListener", new FakeEntitledPlayer());
        Assert.assertThat(drmCallbackListener, isInstance(AnalyticsDrmCallbackListener.class));
    }

    private static Matcher<Object> isInstance(final Class<?> type) {
        return new BaseMatcher<Object>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("That the object is instanceof "+type.getName());
            }

            @Override
            public boolean matches(Object o) {
                return type.isInstance(o);
            }
        };
    }
}
