package app.audio.Files;

import lombok.Getter;

@Getter
public final class Episode extends AudioFile {
    private final String description;
    @Override
    public String getGenre() {
        return null;
    }
    @Override
    public String getArtist() {
        return null;
    }

    @Override
    public String getAlbum() {
        return null;
    }
    @Override
    public boolean isEpisode() {
        return true;
    }

    public Episode(final String name, final Integer duration, final String description) {
        super(name, duration);
        this.description = description;
    }
}
