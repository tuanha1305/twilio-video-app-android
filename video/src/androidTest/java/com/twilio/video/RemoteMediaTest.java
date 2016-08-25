package com.twilio.video;

import android.content.Context;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.AccessManager;
import com.twilio.video.helper.AccessTokenHelper;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RemoteMediaTest {
    private final static String TEST_USER  = "TEST_USER";
    private final static String TEST_USER2  = "TEST_USER2";

    private Context context;
    private LocalMedia actor1LocalMedia;
    private LocalMedia actor2LocalMedia;
    private FakeVideoCapturer fakeVideoCapturer;
    private VideoClient actor1VideoClient;
    private VideoClient actor2VideoClient;
    private AccessManager actor1AccessManager;
    private AccessManager actor2AccessManager;
    private Room room;
    private Participant participant;
    private String testRoom;


    private Room connectClient(VideoClient videoClient, LocalMedia localMedia,
                               CallbackHelper.FakeRoomListener roomListener) {
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        return room;
    }

    private Participant getParticipantFromRoom(Room room) {
        for (Participant participant : room.getParticipants().values()) {
            // Grab first participant
            return participant;
        }
        return null;
    }

    @Before
    public void setup() throws InterruptedException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testRoom = RandUtils.generateRandomString(10);
        fakeVideoCapturer = new FakeVideoCapturer();
        actor1LocalMedia = LocalMedia.create(context);
        actor1AccessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        actor1VideoClient = new VideoClient(context, actor1AccessManager);
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        room = connectClient(actor1VideoClient, actor1LocalMedia, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        actor2LocalMedia = LocalMedia.create(context);
        actor2AccessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        actor2VideoClient = new VideoClient(context, actor2AccessManager);
        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        connectClient(actor2VideoClient, actor2LocalMedia, roomListener2);
        assertTrue(roomListener2.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener2.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        participant = getParticipantFromRoom(room);
        assertNotNull(participant);
    }

    @After
    public void teardown(){
        actor1LocalMedia.release();
        actor2LocalMedia.release();
    }

    // Audio

    @Test
    public void media_onAudioTrackAdded() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onAudioTrackRemoved() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackRemovedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        actor2LocalMedia.removeAudioTrack(audioTrack);
        assertTrue(mediaListener.onAudioTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore
    public void media_onAudioTrackEnabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackEnabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(false);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        audioTrack.enable(true);
        assertTrue(mediaListener.onAudioTrackEnabledLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore
    public void media_onAudioTrackDisabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        audioTrack.enable(false);
        assertTrue(mediaListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));
    }

    // Video
    @Test
    public void media_onVideoTrackAdded() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onVideoTrackRemoved() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        actor2LocalMedia.removeLocalVideoTrack(videoTrack);
        assertTrue(mediaListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore
    public void media_onVideoTrackEnabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(false, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        videoTrack.enable(true);
        assertTrue(mediaListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore
    public void media_onVideoTrackDisabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackDisabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        videoTrack.enable(false);
        assertTrue(mediaListener.onVideoTrackDisabledLatch.await(20, TimeUnit.SECONDS));
    }

}
