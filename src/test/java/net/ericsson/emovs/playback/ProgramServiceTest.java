package net.ericsson.emovs.playback;

import android.app.Activity;

import junit.framework.Assert;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEMPEntitlementProvider;
import net.ericsson.emovs.playback.helpers.FakeEMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEntitledPlayer;
import net.ericsson.emovs.playback.helpers.FakeEntitlementProvider;
import net.ericsson.emovs.playback.helpers.FakeTech;
import net.ericsson.emovs.playback.services.ProgramService;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.models.EmpChannel;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.test.TestUtils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

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
public class ProgramServiceTest {
    @Mock
    EmpProgram live_program1, live_program2;
//    @Mock
//    EmpProgram catchup_program;
//    @Mock
//    EmpChannel live_channel;
//    @Mock
//    TechFactory techFactory;
//    @Mock
//    Activity dummyActivity;

//    Entitlement entitlement_no_bookmark;
    Entitlement entitlement_with_bookmark_emup;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(live_program1.liveNow()).thenReturn(true);
        live_program1.assetId = "@id/1";
        when(live_program2.liveNow()).thenReturn(true);
        live_program2.assetId = "@id/2";
//        when(catchup_program.liveNow()).thenReturn(false);
//        when(techFactory.build()).thenReturn(new FakeTech());

        live_program1.startDateTime = DateTime.now();
        live_program1.endDateTime = new DateTime(live_program1.startDateTime.getMillis() + 1000L);

        live_program2.startDateTime = new DateTime(live_program1.endDateTime.getMillis());
        live_program2.endDateTime = new DateTime(live_program2.startDateTime.getMillis() + 1000L);

//        catchup_program.startDateTime = DateTime.now();
//        catchup_program.endDateTime = DateTime.now();

//        entitlement_no_bookmark = new Entitlement();
        entitlement_with_bookmark_emup = new Entitlement();
        entitlement_with_bookmark_emup.channelId = "12345";
        entitlement_with_bookmark_emup.mediaLocator = ".isml";
        entitlement_with_bookmark_emup.lastViewedOffset = 1234L;
        entitlement_with_bookmark_emup.lastViewedTime = 54321L;
        entitlement_with_bookmark_emup.liveTime = 67890L;
        entitlement_with_bookmark_emup.isLive = true;
        entitlement_with_bookmark_emup.isUnifiedStream = true;

    }

    @Test
    public void live_program_boundary_crossing_test() throws Exception {
        //TODO: test no finished
        FakeEMPMetadataProvider fakeMetadataProvider = new FakeEMPMetadataProvider();
        TestUtils.mockProvider(EMPMetadataProvider.class, fakeMetadataProvider);

        FakeEMPEntitlementProvider fakeEntitlementProvider = new FakeEMPEntitlementProvider();
        TestUtils.mockProvider(EMPEntitlementProvider.class, fakeEntitlementProvider);

        ArrayList<EmpProgram> singleProgramEpg1 = new ArrayList<>();
        singleProgramEpg1.add(live_program1);

        ArrayList<EmpProgram> singleProgramEpg2 = new ArrayList<>();
        singleProgramEpg2.add(live_program2);

        FakeEntitledPlayer player = new FakeEntitledPlayer();
        player.mockIsPlaying(true);

        // Test Case 1: Normal playback and program ends and starts a new program (User is ENTITLED to watch next program)
        ProgramService service = new ProgramService(player, entitlement_with_bookmark_emup);

        fakeMetadataProvider.mockEpg(singleProgramEpg1);
        player.mockPlayHeadTime(live_program1.startDateTime.getMillis() + 1L);

        service.start();

        while(service.getCurrentProgram() == null) {
            Thread.sleep(10);
        }


        EmpProgram currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/1".equals(currentProgram.assetId));

        fakeMetadataProvider.mockEpg(singleProgramEpg2);
        player.mockPlayHeadTime(live_program2.startDateTime.getMillis() + 1L);

        Thread.sleep(2000);

        currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/2".equals(currentProgram.assetId));


        // Test Case 2: Normal playback and program ends and starts a new program (User is NOT ENTITLED to watch next program)

        // Test Case 3: Normal playback and program ends but there is a gap in EPG (Entitlement check cannot be done)

        // Test Case 4: Normal playback and program ends and starts a new program (Server crashes when performing entitlement check)

        service.interrupt();
    }



}