/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import repsaj.airstreamer.server.airplay.AirPlayJmDNSService;
import repsaj.airstreamer.server.streaming.MediaInfo;
import repsaj.airstreamer.server.model.Video;
import repsaj.airstreamer.server.streaming.FfmpegWrapper;
import repsaj.airstreamer.server.webserver.WebService;

/**
 *
 * @author jwesselink
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class);
    private static ServiceWrapper serviceWrapper;

    public static void main(String[] args) {

        BasicConfigurator.configure();

        LOGGER.info("Starting...");

        ApplicationSettings settings = new ApplicationSettings();
        settings.setPath("/Users/jasper/Documents/movie_tmp/");

        serviceWrapper = new ServiceWrapper(settings);

        Video video = new Video();
        video.setId("1");
        video.setName("Californication");
        video.setPath("/Users/jasper/Documents/movie_tmp/cali.mkv");
        VideoRegistry.getInstance().addVideo(video);

        video = new Video();
        video.setId("2");
        video.setName("Rob Dyrdek");
        video.setPath("/Users/jasper/Documents/movie_tmp/rob.mkv");
        VideoRegistry.getInstance().addVideo(video);

        video = new Video();
        video.setId("3");
        video.setName("Foo fighters");
        video.setPath("/Users/jasper/Documents/movie_tmp/foo.mkv");
        VideoRegistry.getInstance().addVideo(video);

        serviceWrapper.addService(new WebService());
        serviceWrapper.addService(new AirPlayJmDNSService());

        serviceWrapper.init();
        serviceWrapper.start();

        FfmpegWrapper ffmpegWrapper = new FfmpegWrapper(video);
        ffmpegWrapper.start(false);
        
        MediaInfo info = ffmpegWrapper.getMediaInfo();
        LOGGER.info("Stream count:" + info.getStreams().size());

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//        }

//        if (!DeviceRegistry.getInstance().getDevices().isEmpty()) {
//
//            //PlayCommand cmd = new PlayCommand("http://trailers.apple.com/movies/independent/stolen/stolen-tlr2_h720p.mov", 0);
//            PlayCommand cmd = new PlayCommand("http://192.168.1.13:8085/files/index.m3u8", 0);
//            DeviceConnection conn = new DeviceConnection((Device) DeviceRegistry.getInstance().getDevices().toArray()[0]);
//            DeviceResponse response = conn.sendCommand(cmd);
//
//            LOGGER.info("response: " + response.getResponseCode() + " " + response.getResponseMessage());
//        }


//        FfmpegWrapper ffmpegWrapper = new FfmpegWrapper();
//        ffmpegWrapper.start();


//        PlayListGenerator generator = new PlayListGenerator("/Users/jasper/Documents/movie_tmp/");
//        generator.start();
//         try {
//            Thread.sleep(1500);
//        } catch (InterruptedException ex) {
//        }
//        generator.finish();



        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                try {
                    serviceWrapper.stop();
                } catch (Exception e) {
                }
            }
        });

    }
}
