/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server.db;

import java.util.List;
import org.junit.Test;
import repsaj.airstreamer.server.metadata.TheTvDbApi;
import repsaj.airstreamer.server.metadata.TvShowDirectoryIndexer;
import repsaj.airstreamer.server.model.TvShowEpisode;
import repsaj.airstreamer.server.model.TvShowSerie;
import repsaj.airstreamer.server.model.Video;
import repsaj.airstreamer.server.model.VideoTypeFactory;

/**
 *
 * @author jasper
 */
public class DbTest {

    public void setup() {
    }

    //@Test
    public void test1Series() {
        MongoDatabase db = new MongoDatabase();
        TheTvDbApi tvDbApi = new TheTvDbApi();

        db.init();
        db.start();


        String path = this.getClass().getResource("/directory_indexer/tvshows/").getPath();
        System.out.println(path);

        TvShowDirectoryIndexer indexer = new TvShowDirectoryIndexer();
        List<TvShowSerie> shows = indexer.indexTvShows(path);

        for (TvShowSerie serie : shows) {
            Video vid = db.getVideoByPath(serie.getPath());
            if (vid == null) {
                System.out.println("INSERT " + serie.getName());
                tvDbApi.updateSerie(serie);
                db.save(serie);
            } else {
                System.out.println("IGNORE " + serie.getName());
            }
        }

        db.stop();

    }

    //@Test
    public void test2Episodes() {
        MongoDatabase db = new MongoDatabase();
        TheTvDbApi tvDbApi = new TheTvDbApi();

        db.init();
        db.start();

        String path = this.getClass().getResource("/directory_indexer/tvshows/").getPath();
        System.out.println(path);

        TvShowDirectoryIndexer indexer = new TvShowDirectoryIndexer();

        List<Video> series = db.getVideosByType(VideoTypeFactory.SERIE_TYPE);
        for (Video video : series) {
            if (video instanceof TvShowSerie) {
                TvShowSerie serie = (TvShowSerie) video;

                List<TvShowEpisode> episodes = indexer.indexTvShow(serie);
                for (TvShowEpisode episode : episodes) {
                    Video tmpepisode = db.getVideoByPath(episode.getPath());
                    if (tmpepisode == null) {
                        System.out.println("INSERT " + episode.getName());
                        tvDbApi.updateEpisode(serie, episode);
                        db.save(episode);
                    } else {
                        System.out.println("IGNORE " + episode.getName());
                    }
                }
            }
        }


        db.stop();
    }
}
