/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import repsaj.airstreamer.server.model.Movie;

/**
 *
 * @author jasper
 */
public class MovieDirectoryIndexer {

    private static final Pattern MOVIE_WITH_YEAR = Pattern.compile("([a-zA-Z\\s0-9]*)\\(?\\s?([0-9]{4})\\)?\\s?.*");
    private static final Pattern MOVIE = Pattern.compile("([a-zA-Z\\s0-9]*).*");
    private static final String[] REMOVE_TAGS = {"HD", "X264", "1080P", "720P", "BluRay", "DTS"};
    private static final String[] IGNORED_EXT = {"iso", "srt"};

    public List<Movie> indexDirectory(String path) {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        File movies_path = new File(path);

        File[] files = movies_path.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("dir:" + file.getName());
                List<Movie> tmpMovies = indexSubDirectory(file);
                movies.addAll(tmpMovies);
            } else {
                System.out.println("file:" + file.getName());
                Movie movie = matchFile(file);
                if (movie != null) {
                    movies.add(movie);
                }
            }
        }

        return movies;
    }

    private List<Movie> indexSubDirectory(File dir) {
        //TODO this method can be refactored into a recursive method

        ArrayList<Movie> movies = new ArrayList<Movie>();
        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("dir [IGNORED]" + file.getName());
            } else {
                System.out.println("file:" + file.getName());
                Movie movie = matchFile(file);
                if (movie != null) {
                    movies.add(movie);
                }
            }
        }
        return movies;
    }

    private Movie matchFile(File file) {
        Matcher matcher;

        String extenstion = FilenameUtils.getExtension(file.getName());
        String name = FilenameUtils.removeExtension(file.getName());

        for (String ignoredext : IGNORED_EXT) {
            if (ignoredext.equalsIgnoreCase(extenstion)) {
                return null;
            }
        }

        //replace dots with spaces
        name = name.replace(".", " ");

        for (String remove : REMOVE_TAGS) {
            name = name.replaceAll("(?i)" + remove, "");
        }

        matcher = MOVIE_WITH_YEAR.matcher(name);
        if (matcher.matches()) {
            System.out.println(">>Found Movie:[" + matcher.group(1).trim() + "] Year:[" + matcher.group(2) + "]");
            Movie movie = new Movie();
            movie.setName(matcher.group(1).trim());
            movie.setPath(file.getPath());
            movie.setYear(Integer.valueOf(matcher.group(2)));
            return movie;
        }

        matcher = MOVIE.matcher(name);
        if (matcher.matches()) {
            System.out.println(">>Found Movie:[" + matcher.group(1).trim() + "]");
            Movie movie = new Movie();
            movie.setName(matcher.group(1).trim());
            movie.setPath(file.getPath());
            return movie;
        }

        return null;
    }
}
