package app.audio.Files;

import app.audio.LibraryEntry;
import app.user.Artist;
import lombok.Getter;

@Getter
public abstract class AudioFile extends LibraryEntry {
    private final Integer duration;
    public abstract String getArtist();
    public abstract String getGenre();
    public abstract String getAlbum();
    public boolean isEpisode() {
        return false;
    }
    public boolean isSong() {
        return false;
    }

    public AudioFile(final String name, final Integer duration) {
        super(name);
        this.duration = duration;
    }
}
