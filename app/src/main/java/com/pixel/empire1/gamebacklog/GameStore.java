package com.pixel.empire1.gamebacklog;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class GameStore {
    private static final String PREFS = "game_backlog_store";
    private static final String KEY = "games_json";

    public static List<GameEntry> load(Context c) {
        List<GameEntry> out = new ArrayList<>();
        String raw = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray a = new JSONArray(raw);
            for (int i=0;i<a.length();i++) {
                JSONObject o = a.optJSONObject(i);
                if (o != null) out.add(GameEntry.fromJson(o));
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static void save(Context c, List<GameEntry> games) {
        JSONArray a = new JSONArray();
        for (GameEntry g : games) {
            try { a.put(g.toJson()); } catch (Exception ignored) {}
        }
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, a.toString()).apply();
    }
}
