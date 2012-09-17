/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server.streaming;

import java.io.File;
import org.apache.log4j.Logger;
import repsaj.airstreamer.server.ApplicationSettings;
import repsaj.airstreamer.server.model.Session;
import repsaj.airstreamer.server.model.Video;

/**
 *
 * @author jasper
 */
public abstract class StreamPlayer {

    private static final Logger LOGGER = Logger.getLogger(StreamPlayer.class);
    protected String path = ApplicationSettings.getPath();
    protected Session session;
    protected Video video;
    protected MediaInfo mediaInfo;

    public void play() {

        StreamAnalyzer analyzer = new StreamAnalyzer();
        mediaInfo = analyzer.analyze(video);

        //TODO make this check more robust
        File file = new File(path + "video/" + video.getId());
        if (!file.isDirectory()) {
            doPrepare();
        }

        doPlay();
    }

    public void stop() {
        doStop();
    }

    protected abstract void doPrepare();

    protected abstract void doPlay();

    protected abstract void doStop();

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return the video
     */
    public Video getVideo() {
        return video;
    }

    /**
     * @param video the video to set
     */
    public void setVideo(Video video) {
        this.video = video;
    }
}
