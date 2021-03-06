/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import repsaj.airstreamer.server.Service;
import repsaj.airstreamer.server.model.Video;
import repsaj.airstreamer.server.model.VideoTypeFactory;

/**
 *
 * @author jasper
 */
public class MongoDatabase extends Service implements Database, Runnable {

    private static final Logger LOGGER = Logger.getLogger(MongoDatabase.class);
    private DB db;
    private DBCollection videocollection;
    private Mongo mongo;
    private Thread starupThread = new Thread(this);
    private boolean connected = false;

    @Override
    public void init() {
        starupThread.start();
    }

    @Override
    public void run() {

        do {
            try {
                mongo = new Mongo("192.168.1.199", 27017);
                db = mongo.getDB("airstreamer");
                videocollection = db.getCollection("videodb");

                videocollection.ensureIndex("id");
                videocollection.ensureIndex("type");
                videocollection.ensureIndex("path");
                videocollection.ensureIndex("name");
                connected = true;

            } catch (UnknownHostException uhex) {
                LOGGER.error("unable to connect to database, retry in 5 seconds.", uhex);
            }

            if (!connected) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException iex) {
                    break;
                }
            }


        } while (!connected);
    }

    @Override
    public void stop() {

        if (starupThread.isAlive()) {
            starupThread.interrupt();
        }

        if (mongo != null) {
            mongo.close();
        }
    }

    @Override
    public void save(Video video) {
        BasicDBObject query = new BasicDBObject();
        query.put("id", video.getId());
        videocollection.update(query, new BasicDBObject(video.toMap()), true, false);
    }

    @Override
    public Video getVideoById(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("id", id);
        return findOne(query);
    }

    @Override
    public Video getVideoByPath(String path) {
        BasicDBObject query = new BasicDBObject();
        query.put("path", path);
        return findOne(query);
    }

    @Override
    public Video searchVideoByPath(String partOfPath) {
        partOfPath = partOfPath.replace("(", "\\(");
        partOfPath = partOfPath.replace(")", "\\)");
        BasicDBObject query = new BasicDBObject();
        Pattern regex = Pattern.compile(partOfPath);
        query.put("path", regex);
        return findOne(query);
    }

    @Override
    public List<Video> getVideosByType(String type) {
        BasicDBObject query = new BasicDBObject();
        query.put("type", type);
        BasicDBObject sort = new BasicDBObject();
        sort.put("name", 1);
        return find(query, sort);
    }

    @Override
    public List<Video> getEpisodesOfSerie(String serieId) {
        BasicDBObject query = new BasicDBObject();
        query.put("serieId", serieId);
        return find(query);
    }

    @Override
    public List<Video> getEpisodes(String serieId, int season) {
        BasicDBObject query = new BasicDBObject();
        query.put("serieId", serieId);
        query.put("season", season);

        BasicDBObject sort = new BasicDBObject();
        sort.put("episode", 1);

        return find(query, sort);
    }

    @Override
    public List<Video> getLatestVideo(int max, String type) {
        BasicDBObject query = new BasicDBObject();
        query.put("type", type);

        BasicDBObject sort = new BasicDBObject();
        sort.put("added", -1);

        return find(query, sort, max);
    }

    @Override
    public void remove(Video video) {
        BasicDBObject query = new BasicDBObject();
        query.put("id", video.getId());
        videocollection.remove(query);
    }

    private Video findOne(BasicDBObject query) {
        DBObject videoObj = videocollection.findOne(query);
        if (videoObj == null) {
            return null;
        }
        Map map = videoObj.toMap();
        Video video = VideoTypeFactory.videoForType((String) map.get("type"));
        video.fromMap(map);
        return video;
    }

    private List<Video> find(BasicDBObject query) {
        return find(query, null);
    }

    private List<Video> find(BasicDBObject query, DBObject sort) {
        return find(query, sort, null);
    }

    private List<Video> find(BasicDBObject query, DBObject sort, Integer limit) {
        ArrayList<Video> videos = new ArrayList<Video>();
        DBCursor cursor;
        if (limit != null && sort != null) {
            cursor = videocollection.find(query).sort(sort).limit(limit);
        } else if (sort != null) {
            cursor = videocollection.find(query).sort(sort);
        } else {
            cursor = videocollection.find(query);
        }

        try {
            while (cursor.hasNext()) {
                DBObject videoObj = cursor.next();
                Map map = videoObj.toMap();
                Video video = VideoTypeFactory.videoForType((String) map.get("type"));
                video.fromMap(map);
                videos.add(video);
            }
        } catch (RuntimeException e) {
            LOGGER.error("error getting data from db", e);
            throw e;
        } finally {
            cursor.close();
        }
        return videos;
    }
}
