package com.spotifydb.model.db.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.detailed.ServiceUnavailableException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetSeveralAlbumsRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpotifyConnection {
    private static final int MAX_ATTEMPTS = 10;
    private static final int WAIT_TIME = 1000;

    private SpotifyApi spotifyApi;

    public SpotifyConnection(URI uri){
        spotifyApi = createSpotifyAPI(uri);
    }

    public SpotifyConnection(){
        this(null);
    }

    public static SpotifyApi createSpotifyAPI(URI uri){

        SpotifyApi spotifyApi;
        String clientID = System.getenv("SPOTIFY_CLIENT_ID");
        String clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET");

        SpotifyApi.Builder builder = SpotifyApi.builder()
                .setClientId(clientID)
                .setClientSecret(clientSecret);
        if(uri != null)
            builder.setRedirectUri(uri);

        spotifyApi = builder.build();


        ClientCredentialsRequest request;
        if(spotifyApi != null) {
            request = spotifyApi.clientCredentials().build();
            try {
                ClientCredentials clientCredentials = request.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return spotifyApi;
    }

    public Artist getArtistById(String artistID){
        if(artistID == null){
            return null;
        }

        Artist artist = null;
        GetArtistRequest request = spotifyApi.getArtist(artistID).build();
        int failedAttempts = 0;

        while( (artist == null) && (failedAttempts != MAX_ATTEMPTS)){
            try{
                artist = request.execute();
            } catch (Exception e){
                failedAttempts++;
                try{
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie){
                    System.err.println("What in tarnation");
                }
            }
        }
        return artist;
    }

    /**
     * Returns a Paging<Artist> for the artist name provided. Since artist names are not necessarily unique, this returns
     * any artists that may potentially be the one of interest.
     * @param artistName The name of an artist
     * @return Collection of all Artists that have the provided name
     */

    public Paging<Artist> getArtistsByName(String artistName){
        if(artistName == null){
            return new Paging.Builder<Artist>().setItems(new Artist[0]).build();
        }

        SearchArtistsRequest artReq = spotifyApi.searchArtists(artistName)
                .market(CountryCode.US)
                .build();

        try{
            return artReq.execute();
        } catch (TooManyRequestsException tmre){ //Too many requests made, wait until we can make more

            int wait = tmre.getRetryAfter() * 1000;
            System.out.println("TooManyRequestsException in getArtistsByName, waiting for " + wait + " milliseconds");
            try{
                Thread.sleep(wait);
            } catch (InterruptedException ie){
                ie.printStackTrace();
            }
            return getArtistsByName(artistName);
        } catch (ServiceUnavailableException sue){ //Unlike TooManyRequestsException we don't know how long to sleep
            try{
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return getArtistsByName(artistName);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return new Paging.Builder<Artist>().setItems(new Artist[0]).build();
    }

    /**
     * There are a couple requests provided by the Spotify Web API wrapper that pertain to albums.
     * The {@link GetArtistsAlbumsRequest} used by this method returns a {@link Paging}<{@link AlbumSimplified}>.
     * This wouldn't be a problem, except for the fact that {@link AlbumSimplified} objects do not their Tracks as an
     * attribute. As such rather than getting a {@link Paging}<{@link AlbumSimplified}>, and making a request for each album
     * to get its tracks, I simply get a List of the albums' ids, and make additional requests for a {@link Paging}<{@link Album}>.
     * This results in n/20 additional requests to Spotify, where n is the number of albums the artist has. This is actually
     * better than if I had done a {@link com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest} for each album,
     * which would result in n additional requests to Spotify.
     * @param artistId The id of the {@link Artist} to retrieve the album ids of.
     * @return A List<String> of album ids
     */

    private  List<String> getAlbumIds(String artistId){

        ArrayList<String> albumIds = new ArrayList<>();
        int offset = 0;
        int itemsAdded = 0;
        int idsToAdd = Integer.MAX_VALUE;
        int failedAttempts = 0;
        Paging<AlbumSimplified> albumsPaging;

        //While all album Ids have not been added and there have not been 10 failed attempts
        while( (idsToAdd - itemsAdded != 0) && (failedAttempts != MAX_ATTEMPTS) ){
            final GetArtistsAlbumsRequest albumsRequest = spotifyApi.getArtistsAlbums(artistId)
                    .market(CountryCode.US)
                    .album_type("album,single")
                    .limit(50) //50 is the max that may be returned
                    .offset(offset)
                    .build();
            try{
                albumsPaging = albumsRequest.execute();


                if(albumsPaging != null){
                    offset += 50;
                    idsToAdd = albumsPaging.getTotal();
                    for(AlbumSimplified a: albumsPaging.getItems())
                        albumIds.add(a.getId());
                    itemsAdded = albumIds.size();
                }
            } catch (Exception e){
                failedAttempts++;
                try{
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie){
                    System.err.println("Unlikely to happen.");
                }
            }
        }
        return albumIds;
    }

    private Album[] getAlbumsById(List<String> ids){
        int albumsToGet = ids.size();
        if(albumsToGet == 0)
            return new Album[0];
        int failedAttempts = 0;
        int startIndex = 0;
        int endIndex = (albumsToGet - startIndex > 20) ? startIndex + 20 : startIndex + (albumsToGet - startIndex);
        List<Album> albums = new ArrayList<>();

        while( (albumsToGet != 0) && (failedAttempts != MAX_ATTEMPTS) ){
            try{
                String[] idArray = ids.subList(startIndex, endIndex).toArray(new String[0]); //make sure inbounds
                GetSeveralAlbumsRequest request = spotifyApi.getSeveralAlbums(idArray)
                        .market(CountryCode.US)
                        .build();
                Album[] albumArray = request.execute();
                if(albumArray != null){
                    startIndex += 20;
                    endIndex = (ids.size() - startIndex > 20) ? startIndex + 20 : startIndex + (ids.size() - startIndex);
                    albums.addAll( Arrays.asList(albumArray) );
                    albumsToGet -= albumArray.length;
                }

            } catch(Exception e) {
                failedAttempts++;
                try{
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException ie){
                    System.err.println("Unlikely to happen.");
                }
            }
        }
        return albums.toArray(new Album[0]);
    }

    public Album[] getAlbumsByArtist(Artist artist){
        if (spotifyApi == null){
            return null; //Indicate there was an error, not that an artist has no albums
        }

        String artistId = artist.getId();
        List<String> albumIds = getAlbumIds(artistId);
        return getAlbumsById(albumIds);
    }
}
