package net.ericsson.emovs.playback;

import junit.framework.Assert;

import net.ericsson.emovs.exposure.entitlements.EMPEntitlementProvider;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEMPEntitlementProvider;
import net.ericsson.emovs.playback.helpers.FakeEMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEntitledPlayer;
import net.ericsson.emovs.playback.services.ProgramService;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.errors.ErrorCodes;
import net.ericsson.emovs.utilities.errors.WarningCodes;
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
        ProgramService.FUZZY_ENTITLEMENT_MAX_DELAY = 0;

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
    public void entitled_live_program_boundary_crossing_test() throws Exception {
        // Test Case 1: Normal playback and program ends and starts a new program (User is ENTITLED to watch next program)

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


        ProgramService service = new ProgramService(player, entitlement_with_bookmark_emup, null);

        fakeEntitlementProvider.mockIsEntitled(true);
        fakeEntitlementProvider.forgetEntitlementCheck();
        fakeMetadataProvider.mockEpg(singleProgramEpg1);
        player.mockPlayHeadTime(live_program1.startDateTime.getMillis() + 1L);

        service.start();

        while(service.getCurrentProgram() == null) {
            Thread.sleep(10);
        }

        EmpProgram currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/1".equals(currentProgram.assetId));
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == true);
        Assert.assertTrue(currentProgram.startDateTime.getMillis() == live_program1.startDateTime.getMillis());
        Assert.assertTrue(currentProgram.endDateTime.getMillis() == live_program1.endDateTime.getMillis());

        fakeEntitlementProvider.mockIsEntitled(true);
        fakeEntitlementProvider.forgetEntitlementCheck();
        fakeMetadataProvider.mockEpg(singleProgramEpg2);
        player.mockPlayHeadTime(live_program2.startDateTime.getMillis() + 1L);

        Thread.sleep(2000);

        currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/2".equals(currentProgram.assetId));
        Assert.assertTrue(currentProgram.startDateTime.getMillis() == live_program2.startDateTime.getMillis());
        Assert.assertTrue(currentProgram.endDateTime.getMillis() == live_program2.endDateTime.getMillis());
        Assert.assertTrue(player.isPlaying());
        Assert.assertTrue(player.lastErrorCode == 0);
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == true);

        service.interrupt();
    }

    @Test
    public void not_entitled_live_program_boundary_crossing_test() throws Exception {
        // Test Case 2: Normal playback and program ends and starts a new program (User is NOT ENTITLED to watch next program)

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

        ProgramService service = new ProgramService(player, entitlement_with_bookmark_emup, null);

        fakeEntitlementProvider.mockIsEntitled(true);
        fakeEntitlementProvider.forgetEntitlementCheck();
        fakeMetadataProvider.mockEpg(singleProgramEpg1);
        player.mockPlayHeadTime(live_program1.startDateTime.getMillis() + 1L);

        service.start();

        while(service.getCurrentProgram() == null) {
            Thread.sleep(10);
        }

        EmpProgram currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/1".equals(currentProgram.assetId));
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == true);

        fakeMetadataProvider.mockEpg(singleProgramEpg2);
        player.mockPlayHeadTime(live_program2.startDateTime.getMillis() + 1L);
        fakeEntitlementProvider.mockIsEntitled(false);
        fakeEntitlementProvider.forgetEntitlementCheck();

        Thread.sleep(2000);

        currentProgram = service.getCurrentProgram();
        Assert.assertTrue(currentProgram == null);

        Assert.assertTrue(player.isPlaying() == false);
        Assert.assertTrue(player.lastErrorCode == ErrorCodes.PLAYBACK_NOT_ENTITLED);
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == true);

        service.interrupt();
    }

    @Test
    public void gap_in_epg_live_program_boundary_crossing_test() throws Exception {
        // Test Case 3: Normal playback and program ends but there is a gap in EPG (Entitlement check cannot be done)

        FakeEMPMetadataProvider fakeMetadataProvider = new FakeEMPMetadataProvider();
        TestUtils.mockProvider(EMPMetadataProvider.class, fakeMetadataProvider);

        FakeEMPEntitlementProvider fakeEntitlementProvider = new FakeEMPEntitlementProvider();
        TestUtils.mockProvider(EMPEntitlementProvider.class, fakeEntitlementProvider);

        ArrayList<EmpProgram> singleProgramEpg1 = new ArrayList<>();
        singleProgramEpg1.add(live_program1);

        FakeEntitledPlayer player = new FakeEntitledPlayer();
        player.mockIsPlaying(true);

        ProgramService service = new ProgramService(player, entitlement_with_bookmark_emup, null);

        fakeEntitlementProvider.mockIsEntitled(true);
        fakeEntitlementProvider.forgetEntitlementCheck();
        fakeMetadataProvider.mockEpg(singleProgramEpg1);
        player.mockPlayHeadTime(live_program1.startDateTime.getMillis() + 1L);

        service.start();

        while(service.getCurrentProgram() == null) {
            Thread.sleep(10);
        }

        EmpProgram currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/1".equals(currentProgram.assetId));
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == true);

        ArrayList<EmpProgram> empty_epg = new ArrayList<>();
        fakeMetadataProvider.mockEpg(empty_epg);
        player.mockPlayHeadTime(live_program1.endDateTime.getMillis() + 1L);
        fakeEntitlementProvider.mockIsEntitled(false);
        fakeEntitlementProvider.forgetEntitlementCheck();

        Thread.sleep(2000);

        currentProgram = service.getCurrentProgram();
        Assert.assertTrue(currentProgram == null);
        Assert.assertTrue(player.isPlaying() == true);
        Assert.assertTrue(player.lastErrorCode == 0);
        Assert.assertTrue(player.lastWarning != null && player.lastWarning.getCode() == WarningCodes.PROGRAM_SERVICE_GAPS_IN_EPG);
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == false);

        service.interrupt();
    }

    @Test
    public void backend_down_live_program_boundary_crossing_test() throws Exception {
        // Test Case 4: Normal playback and program ends and starts a new program (Server crashes when performing entitlement check)

        FakeEMPMetadataProvider fakeMetadataProvider = new FakeEMPMetadataProvider();
        TestUtils.mockProvider(EMPMetadataProvider.class, fakeMetadataProvider);

        FakeEMPEntitlementProvider fakeEntitlementProvider = new FakeEMPEntitlementProvider();
        TestUtils.mockProvider(EMPEntitlementProvider.class, fakeEntitlementProvider);

        ArrayList<EmpProgram> singleProgramEpg1 = new ArrayList<>();
        singleProgramEpg1.add(live_program1);

        FakeEntitledPlayer player = new FakeEntitledPlayer();
        player.mockIsPlaying(true);

        ProgramService service = new ProgramService(player, entitlement_with_bookmark_emup, null);

        fakeEntitlementProvider.mockIsEntitled(true);
        fakeEntitlementProvider.forgetEntitlementCheck();
        fakeMetadataProvider.mockEpg(singleProgramEpg1);
        player.mockPlayHeadTime(live_program1.startDateTime.getMillis() + 1L);

        service.start();

        while(service.getCurrentProgram() == null) {
            Thread.sleep(10);
        }

        EmpProgram currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/1".equals(currentProgram.assetId));
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == true);

        ArrayList<EmpProgram> empty_epg = new ArrayList<>();
        fakeMetadataProvider.mockEpg(empty_epg);
        player.mockPlayHeadTime(live_program1.endDateTime.getMillis() + 1L);
        fakeMetadataProvider.mockBackendAvailability(false);
        fakeEntitlementProvider.forgetEntitlementCheck();

        Thread.sleep(2000);

        currentProgram = service.getCurrentProgram();
        Assert.assertTrue("@id/1".equals(currentProgram.assetId));

        Assert.assertTrue(player.isPlaying() == true);
        Assert.assertTrue(player.lastErrorCode == 0);
        Assert.assertTrue(player.lastWarning != null && player.lastWarning.getCode() == WarningCodes.PROGRAM_SERVICE_ENTITLEMENT_CHECK_NOT_POSSIBLE);
        Assert.assertTrue(fakeEntitlementProvider.wasEntitlementCheckDone == false);

        service.interrupt();
    }

}