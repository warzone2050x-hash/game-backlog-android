package com.pixel.empire1.gamebacklog;

import org.json.JSONException;
import org.json.JSONObject;

public class GameEntry {
    public static final String[] STATUSES = {"Playing Now","Completed","Dropped","Wishlist"};
    public long id;
    public String title, platform, year, status, notes;

    public GameEntry(long id, String title, String platform, String year, String status, String notes) {
        this.id = id; this.title = title; this.platform = platform; this.year = year; this.status = status; this.notes = notes;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id); o.put("title", title); o.put("platform", platform); o.put("year", year);
        o.put("status", status); o.put("notes", notes);
        return o;
    }

    public static GameEntry fromJson(JSONObject o) {
        return new GameEntry(o.optLong("id", System.currentTimeMillis()), o.optString("title",""),
                o.optString("platform",""), o.optString("year",""), o.optString("status","Wishlist"),
                o.optString("notes",""));
    }
}
