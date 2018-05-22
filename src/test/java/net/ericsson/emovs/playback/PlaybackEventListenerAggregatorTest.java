package net.ericsson.emovs.playback;

import android.app.Activity;
import android.view.ViewGroup;

import junit.framework.Assert;

import net.ericsson.emovs.exposure.clients.exposure.ExposureClient;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.exposure.utils.MonotonicTimeService;
import net.ericsson.emovs.playback.helpers.FakeEMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEntitlementProvider;
import net.ericsson.emovs.playback.helpers.FakeExposureClient;
import net.ericsson.emovs.playback.helpers.FakeTech;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.errors.Warning;
import net.ericsson.emovs.utilities.interfaces.ControllerVisibility;
import net.ericsson.emovs.utilities.interfaces.IPlaybackEventListener;
import net.ericsson.emovs.utilities.models.EmpChannel;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.system.RunnableThread;
import net.ericsson.emovs.utilities.test.TestUtils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static net.ericsson.emovs.utilities.errors.WarningCodes.INVALID_START_TIME;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


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
public class PlaybackEventListenerAggregatorTest {
    boolean exceptionRaised = false;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

    }

    @Test
    public void listener_concurrency_test() throws Exception {
        final PlaybackEventListenerAggregator agg = new PlaybackEventListenerAggregator();
        final IPlaybackEventListener listener = new EmptyPlaybackEventListener(null);
        exceptionRaised = false;

        RunnableThread writer = new RunnableThread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10000; ++i) {
                        agg.clearListeners();
                        for (int j = 0; j < 20; ++j) {
                            agg.addListener(listener);
                        }
                    }
                }
                catch (Exception e) {
                    exceptionRaised = true;
                }

            }
        });

        RunnableThread reader = new RunnableThread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100000; ++i) {
                        agg.onEntitlementChange();
                    }
                }
                catch (Exception e) {
                    exceptionRaised = true;
                }
            }
        });

        writer.start();
        reader.start();
        reader.join();
        writer.join();

        Assert.assertTrue(exceptionRaised == false);
    }
}