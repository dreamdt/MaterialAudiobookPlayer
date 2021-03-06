package de.ph1b.audiobook.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

import de.ph1b.audiobook.utils.Validate;


public class Book implements Comparable<Book> {

    public static final int ID_UNKNOWN = -1;
    private long id = ID_UNKNOWN;
    public static final String TAG = Book.class.getSimpleName();
    private static final String IMAGE_EXTENSION = ".jpg";
    private static final String FILE_EXTENSION = "-map.json";
    private static final String BACKUP_SUFFIX = ".backup";
    @NonNull
    private final String root;
    @NonNull
    private final ArrayList<Chapter> chapters;
    @NonNull
    private final ArrayList<Bookmark> bookmarks;
    private final Type type;
    @NonNull
    private String name;
    private int time = 0;
    private float playbackSpeed = 1;
    @NonNull
    private String relativeMediaPath;
    private boolean useCoverReplacement;

    public Book(@NonNull String root,
                @NonNull String name,
                @NonNull ArrayList<Chapter> chapters,
                @NonNull ArrayList<Bookmark> bookmarks,
                float playbackSpeed,
                long id,
                int time,
                @NonNull String relativeMediaPath,
                boolean useCoverReplacement,
                @NonNull Type type) {
        new Validate().notNull(root, name, chapters, bookmarks, relativeMediaPath, type)
                .notEmpty(root, name, relativeMediaPath)
                .notEmpty(chapters);

        //check if bookmark exists
        for (Bookmark b : bookmarks) {
            boolean bookmarkExists = false;
            for (Chapter c : chapters) {
                if (b.getPath().equals(c.getPath())) {
                    bookmarkExists = true;
                    break;
                }
            }
            if (!bookmarkExists) {
                throw new IllegalArgumentException("Cannot add bookmark=" + b + " because it is not " +
                        "in chapters=" + chapters);
            }
        }

        this.bookmarks = bookmarks;
        this.playbackSpeed = playbackSpeed;
        this.root = root;
        this.name = name;
        this.chapters = chapters;
        this.id = id;
        this.useCoverReplacement = useCoverReplacement;
        this.type = type;
        setPosition(time, relativeMediaPath);
    }

    @NonNull
    public static File getCoverFile(@NonNull String root, @NonNull ArrayList<Chapter> chapters) {
        if (chapters.size() == 1) {
            String fileName = "." + chapters.get(0).getName() + IMAGE_EXTENSION;
            return new File(root, fileName);
        } else {
            String fileName = "." + (new File(root).getName()) + IMAGE_EXTENSION;
            return new File(root, fileName);
        }
    }

    @NonNull
    public static File getConfigFile(@NonNull String root, @NonNull ArrayList<Chapter> chapters,
                                     @NonNull Type type) {
        switch (type) {
            case COLLECTION_FILE:
            case SINGLE_FILE:
                return new File(root, "." + chapters.get(0).getName() + FILE_EXTENSION);
            case COLLECTION_FOLDER:
            case SINGLE_FOLDER:
                return new File(root, "." + (new File(root).getName()) + FILE_EXTENSION);
        }
        throw new IllegalArgumentException("Wrong type=" + type);
    }

    @NonNull
    public static File getBackupFile(@NonNull String root, @NonNull ArrayList<Chapter> chapters,
                                     @NonNull Type type) {
        return new File(getConfigFile(root, chapters, type).getAbsolutePath() + BACKUP_SUFFIX);
    }

    public Type getType() {
        return type;
    }

    public void setPosition(int time, @NonNull String relativeMediaPath) {
        new Validate().notNull(relativeMediaPath)
                .notEmpty(relativeMediaPath);

        boolean relativeMediaPathExists = false;
        for (Chapter c : chapters) {
            if (c.getPath().equals(relativeMediaPath)) {
                relativeMediaPathExists = true;
            }
        }
        if (!relativeMediaPathExists) {
            throw new IllegalArgumentException("Creating book with name=" + name +
                    " failed because relativeMediaPath=" + relativeMediaPath +
                    " does not exist in chapters");
        }

        this.time = time;
        this.relativeMediaPath = relativeMediaPath;
    }

    public boolean isUseCoverReplacement() {
        return useCoverReplacement;
    }

    public void setUseCoverReplacement(boolean useCoverReplacement) {
        this.useCoverReplacement = useCoverReplacement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Book) {
            Book that = (Book) o;

            if (!(this.root.equals(that.root))) return false;

            boolean chaptersE = true;
            if (this.chapters.size() != that.chapters.size()) {
                chaptersE = false;
            } else {
                for (int i = 0; i < this.chapters.size(); i++) {
                    if (!this.chapters.get(i).equals(that.chapters.get(i))) {
                        chaptersE = false;
                        break;
                    }
                }
            }

            return this.type == that.type && chaptersE;

        }
        return false;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = PRIME + root.hashCode();
        for (Chapter c : chapters) {
            result = PRIME * result + c.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return TAG + "[" +
                "root=" + root +
                ",chapters=" + chapters +
                ",bookmarks=" + bookmarks +
                ",id=" + id + ", " +
                ",name=" + name +
                ",time=" + time +
                ",playbackSpeed=" + playbackSpeed +
                ",relativeMediaPath=" + relativeMediaPath +
                ",useCoverReplacement=" + useCoverReplacement +
                "]";
    }

    @NonNull
    public String getRelativeMediaPath() {
        return relativeMediaPath;
    }

    @Nullable
    public Chapter getNextChapter() {
        int currentIndex = chapters.indexOf(getCurrentChapter());
        if (currentIndex < chapters.size() - 1) {
            return chapters.get(currentIndex + 1);
        }
        return null;
    }

    @NonNull
    public Chapter getCurrentChapter() {
        for (Chapter c : chapters) {
            if (c.getPath().equals(relativeMediaPath)) {
                return c;
            }
        }
        throw new IllegalArgumentException("getCurrentChapter has no valid path with" +
                " relativeMediaPath=" + relativeMediaPath);
    }

    @Nullable
    public Chapter getPreviousChapter() {
        int currentIndex = chapters.indexOf(getCurrentChapter());
        if (currentIndex > 0) {
            return chapters.get(currentIndex - 1);
        }
        return null;
    }

    public int getTime() {
        return time;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        new Validate().notNull(name)
                .notEmpty(name);
        this.name = name;
    }

    @NonNull
    public File getCoverFile() {
        return getCoverFile(root, chapters);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    @NonNull
    public ArrayList<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
    }

    @NonNull
    public String getRoot() {
        return root;
    }

    @Override
    public int compareTo(@NonNull Book that) {
        return new NaturalStringComparator().compare(this.name, that.name);
    }

    public enum Type {
        COLLECTION_FOLDER,
        COLLECTION_FILE,
        SINGLE_FOLDER,
        SINGLE_FILE,
    }
}

