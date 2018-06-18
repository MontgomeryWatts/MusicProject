package db.updates.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;

/**
 * This is where I messed around with the Spotify API wrapper before writing the methods I needed in SpotifyHelpers.
 * As such, the code is very messy.
 */

public class SpotifyTests {
    public static void main(String[] args) {

        SpotifyApi spotifyApi = SpotifyHelpers.createSpotifyAPI();

        System.out.println(spotifyApi.getAccessToken());
        SearchArtistsRequest artReq = spotifyApi.searchArtists("Isaiah Rashad")
                .market(CountryCode.US)
                .limit(1)
                .build();

        String mad = "";

        try{
            Paging<Artist> artistPaging = artReq.execute();
            for( Artist a: artistPaging.getItems()) {
                System.out.println(a.getName());
                mad = a.getId();
                System.out.println(mad);
                for(String genre: a.getGenres())
                    System.out.println(genre);
            }
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }

        GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(mad)
                .market(CountryCode.US)
                .album_type("album")
                .build();

        String gz = "";

        try{
            Paging<AlbumSimplified> albumPaging = albumsRequest.execute();
            for( AlbumSimplified a: albumPaging.getItems()) {
                System.out.println(a.getName());
                if(a.getName().equals("Cilvia Demo"))
                    gz = a.getId();
            }
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }


        GetAlbumsTracksRequest txRequest = spotifyApi.getAlbumsTracks(gz).build();

        try{
            Paging<TrackSimplified> tracks = txRequest.execute();
            for( TrackSimplified a: tracks.getItems()) {
                System.out.println(a.getName() + " " + a.getDurationMs() / 1000 + " " + a.getUri());
            }
        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }



        GetTrackRequest trackRequest = spotifyApi.getTrack("6eYxmK9fdL4hwLnXG2zY3s").build();

        try {
            final Track track = trackRequest.execute();

            System.out.println("Name: " + track.getName());
            System.out.println("Artists");
            for(ArtistSimplified a : track.getArtists())
                System.out.println(a.getName());
            System.out.println("Duration: " + track.getDurationMs() / 1000);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
