package net.ericsson.emovs.playback;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;


/*
 * Copyright (c) 2017 Ericsson. All Rights Reserved
 *
 * This SOURCE CODE FILE, which has been provided by Ericsson as part
 * of an Ericsson software product for use ONLY by licensed users of the
 * product, includes CONFIDENTIAL and PROPRIETARY information of Ericsson.
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS OF
 * THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH
 * THE PRODUCT.
 */

@RunWith(RobolectricTestRunner.class)
public class EMPPlayerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void playbackPropertiesTest() throws Exception {
        PlaybackProperties props = new PlaybackProperties();
        props.withAutoplay(true).withNativeControls(false);

        Assert.assertFalse(props.hasNativeControls());
        Assert.assertTrue(props.isAutoplay());
    }

}