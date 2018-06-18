package db.updates.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SpotifyHelpers {
    public static SpotifyApi createSpotifyAPI(){

        SpotifyApi spotifyApi = null;

        try {
            File file = new File("src/main/resources/clientInfo.txt");
            Scanner scanner = new Scanner(file);
            spotifyApi = SpotifyApi.builder()
                    .setClientId(scanner.nextLine())
                    .setClientSecret(scanner.nextLine())
                    .build();
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        final ClientCredentialsRequest request = spotifyApi.clientCredentials().build();

        try{
            final ClientCredentials clientCredentials = request.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return spotifyApi;
    }

    public static List<String> getArtistGenres(SpotifyApi spotifyApi, String artistName){
        List<String> genres = new ArrayList<String>();

        final SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .limit(1)
                .build();

        try{
            final Paging<Artist> artistPaging = artReq.execute();
            for( Artist a: artistPaging.getItems()) {
                genres = Arrays.asList(a.getGenres());
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return genres;
    }

    public static String getArtistID(SpotifyApi spotifyApi, String artistName){
        String id = "";

        final SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .limit(1)
                .build();

        try{
            final Paging<Artist> artistPaging = artReq.execute();
            for( Artist a: artistPaging.getItems()) {
                id = a.getId();
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return id;
    }

    public static String getArtistURI(SpotifyApi spotifyApi, String artistName){
        String uri = "";

        final SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .limit(1)
                .build();

        try{
            final Paging<Artist> artistPaging = artReq.execute();
            for( Artist a: artistPaging.getItems()) {
                uri = a.getUri();
            }
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return uri;
    }

    public static Paging<AlbumSimplified> getAlbums(SpotifyApi spotifyApi, String artistID){
        Paging<AlbumSimplified> albums = null;

        final GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(artistID)
                .market(CountryCode.US)
                .album_type("album")
                .build();
        try {
            albums = albumsRequest.execute();
        } catch (Exception e){
            System.err.println(e.getMessage());
        }

        return albums;
    }

    public static List<String> getFeatured(String artistName, TrackSimplified track){
        List<String> featured = new ArrayList<String>();
        for(ArtistSimplified a: track.getArtists()){
            if ( !a.getName().equals(artistName) )
                featured.add(a.getName());
        }
        return featured;
    }

    public static Paging<TrackSimplified> getTracks(SpotifyApi spotifyApi, String albumID){
        Paging<TrackSimplified> tracks = null;

        final GetAlbumsTracksRequest txRequest = spotifyApi.getAlbumsTracks(albumID).build();

        try{
            tracks = txRequest.execute();
        } catch (Exception e){
            System.err.println("Error: " + e.getMessage());
        }

        return tracks;
    }

}
