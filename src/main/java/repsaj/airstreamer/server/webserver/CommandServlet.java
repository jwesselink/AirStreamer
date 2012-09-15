/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server.webserver;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import repsaj.airstreamer.server.DeviceRegistry;
import repsaj.airstreamer.server.VideoRegistry;
import repsaj.airstreamer.server.airplay.DeviceConnection;
import repsaj.airstreamer.server.airplay.DeviceResponse;
import repsaj.airstreamer.server.airplay.PlayCommand;
import repsaj.airstreamer.server.airplay.StopCommand;
import repsaj.airstreamer.server.model.Device;
import repsaj.airstreamer.server.model.Video;
import repsaj.airstreamer.server.streaming.FfmpegWrapper;
import repsaj.airstreamer.server.streaming.MediaInfo;
import repsaj.airstreamer.server.streaming.StreamInfo;

/**
 *
 * @author jasper
 */
public class CommandServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CommandServlet.class);
    private String path;

    public CommandServlet(String path) {
        this.path = path;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String command = request.getParameter("command");

        if ("test".equals(command)) {
            String videoId = request.getParameter("id");

            if (!DeviceRegistry.getInstance().getDevices().isEmpty()) {

                PlayCommand cmd = new PlayCommand("http://192.168.1.13:8085/files/video/" + videoId + "/index.m3u8", 0);
                DeviceConnection conn = new DeviceConnection((Device) DeviceRegistry.getInstance().getDevices().toArray()[0]);
                DeviceResponse tvresponse = conn.sendCommand(cmd);

                LOGGER.info("response: " + tvresponse.getResponseCode() + " " + tvresponse.getResponseMessage());
            }


        }
        if ("play".equals(command)) {
            String videoId = request.getParameter("id");
            Video video = VideoRegistry.getInstance().getVideo(videoId);

            if (video != null) {

                //TODO check if movie is already remuxed, skip the following steps if so.

                FfmpegWrapper ffmpegInfo = new FfmpegWrapper(video);
                ffmpegInfo.start(false);
                MediaInfo mediaInfo = ffmpegInfo.getMediaInfo();

                for (StreamInfo stream : mediaInfo.getStreams()) {
                    switch (stream.getMediaType()) {
                        case Audio:
                            if (stream.getCodec().equals(StreamInfo.AAC)) {
                                FfmpegWrapper ffmpegWrapper = new FfmpegWrapper(path, video, stream);
                                ffmpegWrapper.start();
                            } else if (stream.getCodec().equals(StreamInfo.AC3)) {
                                //Extract ac3 stream:
                                FfmpegWrapper ffmpegWrapper1 = new FfmpegWrapper(path, video, stream);
                                ffmpegWrapper1.start();

                                //Convert ac3 to aac stream:
                                FfmpegWrapper ffmpegWrapper2 = new FfmpegWrapper(path, video, stream);
                                ffmpegWrapper2.setToCodec("libfaac");
                                ffmpegWrapper2.start();
                            } else {
                                throw new UnsupportedOperationException("Codec not supported:" + stream.getCodec());
                            }
                            break;

                        case Video:
                            if (!stream.getCodec().equals(StreamInfo.H264)) {
                                throw new UnsupportedOperationException("Codec not supported:" + stream.getCodec());
                            }
                            FfmpegWrapper ffmpegVideoWrapper = new FfmpegWrapper(path, video, stream);
                            ffmpegVideoWrapper.start();
                            break;

                        case Subtitle:
                            if (!stream.getCodec().equals(StreamInfo.SUBRIP)) {
                                throw new UnsupportedOperationException("Codec not supported:" + stream.getCodec());
                            }

                            if (stream.getLanguage().equals("eng")) {
                                FfmpegWrapper ffmpegSubWrapper = new FfmpegWrapper(path, video, stream);
                                ffmpegSubWrapper.start();
                            }
                            break;
                    }
                }



//                if (!DeviceRegistry.getInstance().getDevices().isEmpty()) {
//
//                    PlayCommand cmd = new PlayCommand("http://192.168.1.13:8085/files/rob.mp4", 0);
//                    DeviceConnection conn = new DeviceConnection((Device) DeviceRegistry.getInstance().getDevices().toArray()[0]);
//                    DeviceResponse tvresponse = conn.sendCommand(cmd);
//
//                    LOGGER.info("response: " + tvresponse.getResponseCode() + " " + tvresponse.getResponseMessage());
//                }
            }

        }
        if ("stop".equals(command)) {
            String videoId = request.getParameter("id");
            Video video = VideoRegistry.getInstance().getVideo(videoId);
            if (video != null) {
                if (!DeviceRegistry.getInstance().getDevices().isEmpty()) {

                    StopCommand cmd = new StopCommand();
                    DeviceConnection conn = new DeviceConnection((Device) DeviceRegistry.getInstance().getDevices().toArray()[0]);
                    DeviceResponse tvresponse = conn.sendCommand(cmd);

                    LOGGER.info("response: " + tvresponse.getResponseCode() + " " + tvresponse.getResponseMessage());
                }
            }
        }
    }
}