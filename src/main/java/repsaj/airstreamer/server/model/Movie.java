/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repsaj.airstreamer.server.model;

import java.util.Map;

/**
 *
 * @author jasper
 */
public class Movie extends Video {

    private int year;

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("year", year);
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        super.fromMap(map);
        year = (Integer) map.get("year");
    }

    @Override
    public String getType() {
        return "movie";
    }
}
