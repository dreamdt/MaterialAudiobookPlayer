package de.ph1b.audiobook.model;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class JSONHelper {

    private static final String JSON_TIME = "time";
    private static final String JSON_BOOKMARK_TIME = "time";
    private static final String JSON_BOOKMARK_TITLE = "title";
    private static final String JSON_SPEED = "speed";
    private static final String JSON_NAME = "name";
    private static final String JSON_BOOKMARKS = "bookmarks";
    private static final String JSON_REL_PATH = "relPath";
    private static final String JSON_BOOKMARK_REL_PATH = "relPath";
    private static final String JSON_USE_COVER_REPLACEMENT = "useCoverReplacement";

    @NonNull
    private final File configFile;
    @NonNull
    private final File backupFile;
    @NonNull
    private JSONObject playingInformation = new JSONObject();

    public JSONHelper(@NonNull String root,
                      @NonNull ArrayList<Chapter> chapters,
                      @NonNull Book.Type type) {
        this.configFile = Book.getConfigFile(root, chapters, type);
        this.backupFile = Book.getBackupFile(root, chapters, type);

        synchronized (JSONHelper.class) {

            boolean configFileValid = configFile.exists() && configFile.canRead()
                    && configFile.length() > 0;
            boolean backupFileValid = backupFile.exists() && backupFile.canRead()
                    && backupFile.length() > 0;

            JSONObject tempInformation = null;
            if (configFileValid) {
                tempInformation = fileToJSONObject(configFile);
            }
            if (tempInformation == null && backupFileValid) {
                tempInformation = fileToJSONObject(backupFile);
            }
            if (tempInformation == null) {
                tempInformation = new JSONObject();
            }
            playingInformation = tempInformation;

        }

        try {
            // bookmarks
            if (!(playingInformation.has(JSON_BOOKMARKS)) || (playingInformation.has(JSON_BOOKMARKS)
                    && !(playingInformation.get(JSON_BOOKMARKS) instanceof JSONArray))) {
                playingInformation.put(JSON_BOOKMARKS, new JSONArray());
            }

            // time
            if (!(playingInformation.has(JSON_TIME)) || (playingInformation.has(JSON_TIME)
                    && !(playingInformation.get(JSON_TIME) instanceof Number))) {
                playingInformation.put(JSON_TIME, 0);
            }

            // rel path
            if (!(playingInformation.has(JSON_REL_PATH)) || (playingInformation.has(JSON_REL_PATH)
                    && !(playingInformation.get(JSON_REL_PATH) instanceof String))) {
                playingInformation.put(JSON_REL_PATH, "1.0");
            }

            // speed
            if (!(playingInformation.has(JSON_SPEED)) || (playingInformation.has(JSON_SPEED)
                    && !(playingInformation.get(JSON_SPEED) instanceof String))) {
                playingInformation.put(JSON_SPEED, "1.0");
            }

            // book name
            if (!(playingInformation.has(JSON_NAME)) || (playingInformation.has(JSON_NAME)
                    && !(playingInformation.get(JSON_NAME) instanceof String))) {
                playingInformation.put(JSON_NAME, "");
            }

            // cover replacement
            if (!(playingInformation.has(JSON_USE_COVER_REPLACEMENT))
                    || (playingInformation.has(JSON_USE_COVER_REPLACEMENT)
                    && !(playingInformation.get(JSON_USE_COVER_REPLACEMENT) instanceof Boolean))) {
                playingInformation.put(JSON_USE_COVER_REPLACEMENT, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    private JSONObject fileToJSONObject(@NonNull File file) {
        try {
            String returnString = FileUtils.readFileToString(file);
            if (returnString.length() > 0) return new JSONObject(returnString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public float getSpeed() {
        try {
            return Float.valueOf(playingInformation.getString(JSON_SPEED));
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
            return 1f;
        }
    }

    public void setSpeed(float speed) {
        try {
            playingInformation.put(JSON_SPEED, String.valueOf(speed));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setUseCoverReplacement(boolean useCoverReplacement) {
        try {
            playingInformation.put(JSON_USE_COVER_REPLACEMENT, useCoverReplacement);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean useCoverReplacement() {
        try {
            return playingInformation.getBoolean(JSON_USE_COVER_REPLACEMENT);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    @NonNull
    public ArrayList<Bookmark> getBookmarks() {
        ArrayList<Bookmark> bookmarks = new ArrayList<>();
        try {
            JSONArray bookmarksJ = playingInformation.getJSONArray(JSON_BOOKMARKS);
            for (int i = 0; i < bookmarksJ.length(); i++) {
                JSONObject bookmarkJ = (JSONObject) bookmarksJ.get(i);
                int time = bookmarkJ.getInt(JSON_BOOKMARK_TIME);
                String title = bookmarkJ.getString(JSON_BOOKMARK_TITLE);
                String relPath = bookmarkJ.getString(JSON_BOOKMARK_REL_PATH);
                bookmarks.add(new Bookmark(relPath, title, time));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bookmarks;
    }

    public void setBookmarks(@NonNull ArrayList<Bookmark> bookmarks) {
        try {
            JSONArray bookmarksJ = new JSONArray();
            for (Bookmark b : bookmarks) {
                JSONObject bookmarkJ = new JSONObject();
                bookmarkJ.put(JSON_BOOKMARK_TIME, b.getTime());
                bookmarkJ.put(JSON_BOOKMARK_TITLE, b.getTitle());
                bookmarkJ.put(JSON_REL_PATH, b.getPath());
                bookmarksJ.put(bookmarkJ);
            }
            playingInformation.put(JSON_BOOKMARKS, bookmarksJ);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Stores the book information. Creates a backup of the old config file, so if there is an error
     * on writing (ie the devices goes off) the information is not lost.
     */
    public void writeJSON() {
        synchronized (JSONHelper.class) {
            try {
                // make backup
                if (configFile.exists()) {
                    FileUtils.copyFile(configFile, backupFile);
                }

                // write new file
                FileUtils.write(configFile, playingInformation.toString());

                // delete backup
                if (backupFile.exists()) {
                    FileUtils.deleteQuietly(backupFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    public String getRelPath() {
        try {
            return playingInformation.getString(JSON_REL_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setRelPath(@NonNull String relPath) {
        try {
            playingInformation.put(JSON_REL_PATH, relPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public String getName() {
        try {
            return playingInformation.getString(JSON_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setName(@NonNull String name) {
        try {
            playingInformation.put(JSON_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getTime() {
        try {
            return playingInformation.getInt(JSON_TIME);
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setTime(int time) {
        try {
            playingInformation.put(JSON_TIME, time);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
