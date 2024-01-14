package app;

import app.audio.Collections.*;
import app.audio.Files.AudioFile;
import app.audio.Files.Episode;
import app.audio.Files.Song;
import app.pages.ArtistPage;
import app.player.PlayerStats;
import app.searchBar.Filters;
import app.user.Artist;
import app.user.Host;
import app.user.Merchandise;
import app.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.input.CommandInput;

import java.util.*;

/**
 * The type Command runner.
 */
public final class CommandRunner {
    /**
     * The Object mapper.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Admin admin;

    /**
     * Update admin.
     */
    public static void updateAdmin() {
        admin = Admin.getInstance();
    }

    private CommandRunner() {
    }

    /**
     * Search object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode search(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        Filters filters = new Filters(commandInput.getFilters());
        String type = commandInput.getType();
        ArrayList<String> results = new ArrayList<>();
        String message = "%s is offline.".formatted(user.getUsername());

        if (user.isStatus()) {
            results = user.search(filters, type);
            message = "Search returned " + results.size() + " results";
        }

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);
        objectNode.set("results", objectMapper.valueToTree(results));

        return objectNode;
    }

    /**
     * Search object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode wrapped(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        Artist art = admin.getArtist(commandInput.getUsername());
        Host host = admin.getHost(commandInput.getUsername());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();

        admin.updateTimestamp(commandInput.getTimestamp());

        if (user != null) {
            if (!user.getPlayer().getListened().isEmpty()){
                HashMap<String, Integer> topArtists = new HashMap<>();
                for (Artist artist : Admin.getInstance().getArtists()) {
                    int count = 0;
                    for (Map.Entry<AudioFile, Integer> entry
                            : user.getPlayer().getListened().entrySet()) {
                        if (entry.getKey().matchesArtist(artist.getUsername())) {
                            count = count + entry.getValue();
                            topArtists.remove(artist.getUsername());
                            topArtists.put(artist.getUsername(), count);
                        }
                    }
                }
                topArtists = get5(topArtists);
                resultNode.set("topArtists", objectMapper.valueToTree(topArtists));

                HashMap<String, Integer> topGenres = getStringIntegerHashMap(user);
                topGenres = get5(topGenres);
                resultNode.set("topGenres", objectMapper.valueToTree(topGenres));

                HashMap<String, Integer> topSongs = getMap(user);
                topSongs = get5(topSongs);
                resultNode.set("topSongs", objectMapper.valueToTree(topSongs));

                HashMap<String, Integer> topAlbums = new HashMap<>();
                for (Map.Entry<AudioFile, Integer> entry
                        : user.getPlayer().getListened().entrySet()) {
                    if (entry.getKey() instanceof Song song) {
                        if (topAlbums.containsKey(song.getAlbum())) {
                            topAlbums.put(song.getAlbum(),
                                    topAlbums.get(song.getAlbum()) + entry.getValue());
                        } else {
                            topAlbums.put(song.getAlbum(), entry.getValue());
                        }
                    }
                }
                topAlbums = get5(topAlbums);

                resultNode.set("topAlbums", objectMapper.valueToTree(topAlbums));

                HashMap<String, Integer> topEpisodes = new HashMap<>();
                for (Map.Entry<AudioFile, Integer> entry
                        : user.getPlayer().getListened().entrySet()) {
                    if (entry.getKey() instanceof Episode episode) {
                        if (topEpisodes.containsKey(episode.getName())) {
                            topEpisodes.put(episode.getName(),
                                    topAlbums.get(episode.getName()) + entry.getValue());
                        } else {
                            topEpisodes.put(episode.getName(), entry.getValue());
                        }
                    }
                }
                topEpisodes = get5(topEpisodes);
                resultNode.set("topEpisodes", objectMapper.valueToTree(topEpisodes));

                outputNode.put("command", "wrapped");
                outputNode.put("user", commandInput.getUsername());
                outputNode.put("timestamp", commandInput.getTimestamp());
                outputNode.set("result", resultNode);
            } else {
                outputNode.put("command", "wrapped");
                outputNode.put("user", commandInput.getUsername());
                outputNode.put("timestamp", commandInput.getTimestamp());
                outputNode.put("message", "No data to show for user "
                        + commandInput.getUsername() + ".");
            }
        }
        if (art != null) {
            HashMap<String, Integer> topAlbums = new HashMap<>();
            for (User user1 : admin.getUsers()) {
                for (Map.Entry<AudioFile, Integer> entry
                        : user1.getPlayer().getListened().entrySet()) {
                    if (entry.getKey() instanceof Song song && song.matchesArtist(art.getUsername())) {
                        if (topAlbums.containsKey(song.getAlbum())) {
                            topAlbums.put(song.getAlbum(),
                                    topAlbums.get(song.getAlbum()) + entry.getValue());
                        } else {
                            topAlbums.put(song.getAlbum(), entry.getValue());
                        }
                    }
                }
            }
            topAlbums = get5(topAlbums);
            HashMap<String, Integer> topSongs = new HashMap<>();
            HashMap<String, Integer> topFan = new HashMap<>();
            for (User user1 : admin.getUsers()) {
                for (Map.Entry<AudioFile, Integer> entry
                        : user1.getPlayer().getListened().entrySet()) {
                    if (entry.getKey() instanceof Song song && song.matchesArtist(art.getUsername())) {
                        if (topFan.containsKey(user1.getUsername())) {
                            topFan.put(user1.getUsername(), topFan.get(user1.getUsername()) + entry.getValue());
                        } else {
                            topFan.put(user1.getUsername(), entry.getValue());
                        }
                        if (topSongs.containsKey(song.getName())) {
                            topSongs.put(song.getName(), topSongs.get(song.getName()) + entry.getValue());
                        } else {
                            topSongs.put(song.getName(), entry.getValue());
                        }
                    }
                }
            }
            HashMap<String, Integer> result = new LinkedHashMap<>();
            int listeners = topFan.size();
            topFan.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue()
                            .reversed().thenComparing(Map.Entry.comparingByKey()))
                    .limit(5)
                    .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
            topFan = result;
            List<String> top = topFan.keySet().stream().toList();
            topSongs = get5(topSongs);
            resultNode.set("topAlbums", objectMapper.valueToTree(topAlbums));
            resultNode.set("topSongs", objectMapper.valueToTree(topSongs));
            ArrayNode topFansArray = resultNode.putArray("topFans");
            top.forEach(topFansArray::add);
            resultNode.put("listeners", listeners);

            outputNode.put("command", "wrapped");
            outputNode.put("user", commandInput.getUsername());
            outputNode.put("timestamp", commandInput.getTimestamp());
            outputNode.set("result", resultNode);
        }
        if (host != null) {
            HashMap<String, Integer> topEpisodes = new HashMap<>();
            HashMap<String, Integer> topFan = new HashMap<>();
            for (Podcast podcast : admin.getPodcasts()) {
                for (User user1 : admin.getUsers()) {
                    for (Map.Entry<AudioFile, Integer> entry
                            : user1.getPlayer().getListened().entrySet()) {
                        if (entry.getKey() instanceof Episode episode) {
                            for (Episode episode1 : podcast.getEpisodes()) {
                                if (episode1.getName().equals(episode.getName())) {
                                    if (podcast.matchesOwner(host.getUsername())) {
                                        if (topFan.containsKey(user1.getUsername())) {
                                            topFan.put(user1.getUsername(), topFan.get(user1.getUsername()) + entry.getValue());
                                        } else {
                                            topFan.put(user1.getUsername(), entry.getValue());
                                        }
                                        if (topEpisodes.containsKey(episode.getName())) {
                                            topEpisodes.put(episode.getName(), topEpisodes.get(episode.getName()) + entry.getValue());
                                        } else {
                                            topEpisodes.put(episode.getName(), entry.getValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            int listeners = topFan.size();
            topEpisodes = get5(topEpisodes);
            resultNode.set("topEpisodes", objectMapper.valueToTree(topEpisodes));
            resultNode.put("listeners", listeners);
            outputNode.put("command", "wrapped");
            outputNode.put("user", commandInput.getUsername());
            outputNode.put("timestamp", commandInput.getTimestamp());
            outputNode.set("result", resultNode);
        }

        return outputNode;
    }

    private static HashMap<String, Integer> getMap(User user) {
        HashMap<String, Integer> topSongs = new HashMap<>();
        for (Map.Entry<AudioFile, Integer> entry
                : user.getPlayer().getListened().entrySet()) {
            if (entry.getKey() instanceof Song song) {
                if (topSongs.containsKey(song.getName())) {
                    topSongs.put(song.getName(), topSongs.get(song.getName()) + entry.getValue());
                } else {
                    topSongs.put(song.getName(), entry.getValue());
                }
            }
        }
        return topSongs;
    }

    private static HashMap<String, Integer> getStringIntegerHashMap(User user) {
        HashMap<String, Integer> topGenres = new HashMap<>();
        for (Map.Entry<AudioFile, Integer> entry
                : user.getPlayer().getListened().entrySet()) {
            if (entry.getKey() instanceof Song song) {
                if (topGenres.containsKey(song.getGenre())) {
                    topGenres.put(song.getGenre(),
                            topGenres.get(song.getGenre()) + entry.getValue());
                } else {
                    topGenres.put(song.getGenre(), entry.getValue());
                }
            }
        }
        return topGenres;
    }

    private static HashMap<String, Integer> get5(HashMap<String, Integer> topAlbums) {
        HashMap<String, Integer> result = new LinkedHashMap<>();
        topAlbums.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(5)
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        topAlbums = result;
        return topAlbums;
    }

    /**
     * Select object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode select(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());

        String message = user.select(commandInput.getItemNumber());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Select object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode buyMerch(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        int ok = 1;
        for (Artist artist : admin.getArtists()) {
            for (Merchandise merchandise : artist.getMerch()) {
                if (merchandise.getName().equals(commandInput.getName())) {
                    user.getMerchandises().add(merchandise);
                    artist.setMerchRevenue(artist.getMerchRevenue() + merchandise.getPrice());
                    if (!admin.getListenedArtists().contains(artist.getUsername())) {
                        admin.getListenedArtists().add(artist.getUsername());
                    }
                    ok = 0;
                    break;
                }
            }
        }
        if (ok == 1) {
            objectNode.put("message", "The merch " + commandInput.getName() + " doesn't exist.");
        } else {
            objectNode.put("message", commandInput.getUsername()
                    + " has added new merch successfully.");
        }

        return objectNode;
    }

    /**
     * Select object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode seeMerch(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        ArrayNode resultNode = objectMapper.createArrayNode();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        for (Merchandise merchandise : user.getMerchandises()) {
            resultNode.add(merchandise.getName());
        }
        objectNode.set("result", resultNode);

        return objectNode;
    }

    /**
     * Select object node.
     *
     * @return the object node
     */
    public static Artist getArtistDetails(String artistName) {
        for (Artist artist : admin.getArtists()) {
            if (artist.getUsername().equals(artistName)) {
                return artist;
            }
        }
        return null;
    }

    /**
     * Select object node.
     *
     * @return the object node
     */
    public static Host getHostDetails(String hostName) {
        for (Host host : admin.getHosts()) {
            if (host.getUsername().equals(hostName)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Select object node.
     *
     * @return the object node
     */
    public static ObjectNode endProgram() {
        ObjectNode resultNode = JsonNodeFactory.instance.objectNode();
        List<Artist> artists = new ArrayList<>();
        for (String artist : admin.getListenedArtists()) {
            Artist artist1 = getArtistDetails(artist);
            artists.add(artist1);
        }

        artists.sort(
                Comparator.comparingDouble(Artist::getMerchRevenue).reversed()
                        .thenComparing(Artist::getUsername)
        );
        for (Artist artist : artists) {
            ObjectNode artistNode = JsonNodeFactory.instance.objectNode();
            artistNode.put("merchRevenue", artist.getMerchRevenue());
            artistNode.put("songRevenue", 0.0);
            artistNode.put("ranking", artists.indexOf(artist) + 1);
            artistNode.put("mostProfitableSong", "N/A");
            resultNode.set(artist.getUsername(), artistNode);
        }

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "endProgram");
        outputNode.set("result", resultNode);
        return outputNode;
    }

    /**
     * Load object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode update(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", "The recommendations for user "
                + commandInput.getUsername() + " have been updated successfully.");
        if (commandInput.getRecommendationType().equals("fans_playlist")) {
            if (user.getPlayer().getCurrentAudioFile() instanceof Song song) {
                String artist = song.getArtist();
                Playlist playlist = new Playlist(artist
                        + " Fan Club recommendations", commandInput.getUsername());
                user.getPlaylistsRecommendations().add(playlist);
            }
        }
        if (commandInput.getRecommendationType().equals("random_song")) {
            if (user.getPlayer().getCurrentAudioFile() instanceof Song song) {
                PlayerStats stats = user.getPlayerStats();
                if ((song.getDuration() - stats.getRemainedTime()) >= 30) {
                    List<Song> songs = new ArrayList<>();
                    for (Song song1 : admin.getSongs()) {
                        if (song1.matchesGenre(song.getGenre())) {
                            songs.add(song1);
                        }
                    }
                    Random random = new Random(song.getDuration() - stats.getRemainedTime());
                    int randomIndex = random.nextInt(songs.size());
                    Song song1 = songs.get(randomIndex);
                    user.getSongRecommendations().add(song1);
                    user.setLastSong(song1);
                }
            }
        }
        if (commandInput.getRecommendationType().equals("random_playlist")) {
            if (user.getPlayer().getCurrentAudioFile() instanceof Song song) {
                Playlist playlist = new Playlist(commandInput.getUsername()
                        + "'s recommendations", commandInput.getUsername());
                user.getPlaylistsRecommendations().add(playlist);
            }
        }
        return objectNode;
    }

    /**
     * Load object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode prevPage(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", "The user " + commandInput.getUsername()
                + " has navigated successfully to the previous page.");
        user.setCurrentPage(user.getPages().get(user.getIndex() - 2));
        user.setIndex(user.getIndex() - 1);
        return objectNode;
    }

    /**
     * Load object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode nextPage(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        if ((user.getIndex() + 1) > (user.getPages().size() - 1)) {
            objectNode.put("message", "There are no pages left to go forward.");
        } else {
            objectNode.put("message", "The user " + commandInput.getUsername()
                    + " has navigated successfully to the next page.");
            user.setCurrentPage(user.getPages().get(user.getIndex()));
            user.setIndex(user.getIndex() + 1);
        }
        return objectNode;
    }

    /**
     * Load object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode loadRecommendations(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        if (user.getLastSong() != null) {
            user.getPlayer().setSource(user.getLastSong(), "song");
            if (user.getPlayer().getSource() != null && user.getPlayer().getListened().get(user.getPlayer().getSource().getAudioFile()) != null) {
                //System.out.println(player.getSource().getAudioFile().getName());
                user.getPlayer().getListened().put(user.getPlayer().getSource().getAudioFile(), user.getPlayer().getListened().get(user.getPlayer().getSource().getAudioFile()) + 1);
            } else if (user.getPlayer().getSource() != null) {
                //System.out.println(source.getAudioFile().getName());
                if (user.getPlayer().getSource().getAudioFile() instanceof Song song) {
                    if (!admin.getListenedArtists().contains(song.getArtist())) {
                        admin.getListenedArtists().add(song.getArtist());
                    }
                }
                user.getPlayer().getListened().put(user.getPlayer().getSource().getAudioFile(), 1);
            }

            user.getPlayer().pause();
        }

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", "Playback loaded successfully.");

        return objectNode;
    }

    /**
     * Load object node.loadRecommendations
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode subscribe(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String artist = null;
        if (user.getCurrentPage() instanceof ArtistPage artistPage) {
            artist = artistPage.getArtist().getUsername();
        }

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", commandInput.getUsername() + " subscribed to " + artist + " successfully.");

        return objectNode;
    }

    /**
     * Load object node.loadRecommendations
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode getNotifications(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", commandInput.getUsername() + " subscribed to successfully.");

        return objectNode;
    }

    /**
     * Load object node.loadRecommendations
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode load(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.load();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Play pause object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode playPause(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.playPause();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Repeat object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode repeat(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.repeat();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Shuffle object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode shuffle(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        Integer seed = commandInput.getSeed();
        String message = user.shuffle(seed);

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Forward object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode forward(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.forward();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Backward object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode backward(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.backward();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Like object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode like(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.like();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Next object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode next(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.next();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Prev object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode prev(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.prev();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Create playlist object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode createPlaylist(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.createPlaylist(commandInput.getPlaylistName(),
                                             commandInput.getTimestamp());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Add remove in playlist object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addRemoveInPlaylist(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.addRemoveInPlaylist(commandInput.getPlaylistId());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Switch visibility object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode switchVisibility(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.switchPlaylistVisibility(commandInput.getPlaylistId());

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Show playlists object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode showPlaylists(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        ArrayList<PlaylistOutput> playlists = user.showPlaylists();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(playlists));

        return objectNode;
    }

    /**
     * Follow object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode follow(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String message = user.follow();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Status object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode status(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        PlayerStats stats = user.getPlayerStats();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("stats", objectMapper.valueToTree(stats));

        return objectNode;
    }

    /**
     * Show liked songs object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode showLikedSongs(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        ArrayList<String> songs = user.showPreferredSongs();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(songs));

        return objectNode;
    }

    /**
     * Gets preferred genre.
     *
     * @param commandInput the command input
     * @return the preferred genre
     */
    public static ObjectNode getPreferredGenre(final CommandInput commandInput) {
        User user = admin.getUser(commandInput.getUsername());
        String preferredGenre = user.getPreferredGenre();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(preferredGenre));

        return objectNode;
    }

    /**
     * Switch connection status object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode switchConnectionStatus(final CommandInput commandInput) {
        String message = admin.switchStatus(commandInput.getUsername());
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Add user object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addUser(final CommandInput commandInput) {
        String message = admin.addNewUser(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Delete user object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode deleteUser(final CommandInput commandInput) {
        String message = admin.deleteUser(commandInput.getUsername());
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Add album object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addAlbum(final CommandInput commandInput) {
        String message = admin.addAlbum(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Remove album object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode removeAlbum(final CommandInput commandInput) {
        String message = admin.removeAlbum(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Show albums object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode showAlbums(final CommandInput commandInput) {
        Artist artist = admin.getArtist(commandInput.getUsername());
        ArrayList<AlbumOutput> albums = artist.showAlbums();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(albums));

        return objectNode;
    }

    /**
     * Add event object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addEvent(final CommandInput commandInput) {
        String message = admin.addEvent(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Remove event object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode removeEvent(final CommandInput commandInput) {
        String message = admin.removeEvent(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Add podcast object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addPodcast(final CommandInput commandInput) {
        String message = admin.addPodcast(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Remove podcast object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode removePodcast(final CommandInput commandInput) {
        String message = admin.removePodcast(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Show podcasts object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode showPodcasts(final CommandInput commandInput) {
        Host host = admin.getHost(commandInput.getUsername());
        List<PodcastOutput> podcasts = host.getPodcasts().stream().map(PodcastOutput::new).toList();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(podcasts));

        return objectNode;
    }

    /**
     * Add merch object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addMerch(final CommandInput commandInput) {
        String message = admin.addMerch(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Add announcement object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode addAnnouncement(final CommandInput commandInput) {
        String message = admin.addAnnouncement(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Remove announcement object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode removeAnnouncement(final CommandInput commandInput) {
        String message = admin.removeAnnouncement(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Gets online users.
     *
     * @param commandInput the command input
     * @return the online users
     */
    public static ObjectNode getOnlineUsers(final CommandInput commandInput) {
        List<String> onlineUsers = admin.getOnlineUsers();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(onlineUsers));

        return objectNode;
    }

    /**
     * Gets all users.
     *
     * @param commandInput the command input
     * @return the all users
     */
    public static ObjectNode getAllUsers(final CommandInput commandInput) {
        List<String> users = admin.getAllUsers();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(users));

        return objectNode;
    }

    /**
     * Change page object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode changePage(final CommandInput commandInput) {
        String message = admin.changePage(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Print current page object node.
     *
     * @param commandInput the command input
     * @return the object node
     */
    public static ObjectNode printCurrentPage(final CommandInput commandInput) {
        String message = admin.printCurrentPage(commandInput);
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("user", commandInput.getUsername());
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.put("message", message);

        return objectNode;
    }

    /**
     * Gets top 5 album list.
     *
     * @param commandInput the command input
     * @return the top 5 album list
     */
    public static ObjectNode getTop5AlbumList(final CommandInput commandInput) {
        List<String> albums = admin.getTop5AlbumList();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(albums));

        return objectNode;
    }

    /**
     * Gets top 5 artist list.
     *
     * @param commandInput the command input
     * @return the top 5 artist list
     */
    public static ObjectNode getTop5ArtistList(final CommandInput commandInput) {
        List<String> artists = admin.getTop5ArtistList();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(artists));

        return objectNode;
    }

    /**
     * Gets top 5 songs.
     *
     * @param commandInput the command input
     * @return the top 5 songs
     */
    public static ObjectNode getTop5Songs(final CommandInput commandInput) {
        List<String> songs = admin.getTop5Songs();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(songs));

        return objectNode;
    }

    /**
     * Gets top 5 playlists.
     *
     * @param commandInput the command input
     * @return the top 5 playlists
     */
    public static ObjectNode getTop5Playlists(final CommandInput commandInput) {
        List<String> playlists = admin.getTop5Playlists();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
        objectNode.set("result", objectMapper.valueToTree(playlists));

        return objectNode;
    }
}
