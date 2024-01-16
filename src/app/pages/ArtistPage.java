package app.pages;

import app.audio.Collections.Album;
import app.user.Artist;
import app.user.Event;
import app.user.Merchandise;
import lombok.Getter;

import java.util.List;

/**
 * The type Artist page.
 */
public final class ArtistPage implements Page {
    @Getter
    private Artist artist;
    @Getter
    private final List<Album> albums;
    @Getter
    private final List<Merchandise> merch;
    private final List<Event> events;
    public String getArtist() {
        return this.artist.getUsername();
    }

    /**
     * Instantiates a new Artist page.
     *
     * @param artist1 the artist
     */
    public ArtistPage(final Artist artist1) {
        artist = artist1;
        albums = artist1.getAlbums();
        merch = artist1.getMerch();
        events = artist1.getEvents();
    }

    @Override
    public String printCurrentPage() {
        return "Albums:\n\t%s\n\nMerch:\n\t%s\n\nEvents:\n\t%s"
                .formatted(albums.stream().map(Album::getName).toList(),
                           merch.stream().map(merchItem -> "%s - %d:\n\t%s"
                                .formatted(merchItem.getName(),
                                           merchItem.getPrice(),
                                           merchItem.getDescription()))
                                .toList(),
                           events.stream().map(event -> "%s - %s:\n\t%s"
                                 .formatted(event.getName(),
                                            event.getDate(),
                                            event.getDescription()))
                                 .toList());
    }
}
