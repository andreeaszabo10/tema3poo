package app.pages;

import app.user.Artist;
import app.user.Host;
import app.user.User;

public class PageFactory {
    /**
     * new artist page
     */
    public static Page createArtistPage(Artist artist) {
        return new ArtistPage(artist);
    }

    /**
     * new home page
     */
    public static Page createHomePage(final User user) {
        return new HomePage(user);
    }
    /**
     * new host page
     */
    public static Page createHostPage(final Host host) {
        return new HostPage(host);
    }
    /**
     * new liked page
     */
    public static Page createLikePage(final User user) {
        return new LikedContentPage(user);
    }
}
