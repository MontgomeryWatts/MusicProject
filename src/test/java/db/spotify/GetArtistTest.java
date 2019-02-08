package db.spotify;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

public class GetArtistTest {

    @Test
    public void testValidId(){
        String artistId = "0xOeVMOz2fVg5BJY3N6akT";
        Assert.assertTrue(SpotifyQueries.getArtistDocById(artistId) instanceof Document);
    }

    @Test
    public void testNullId(){
        String artistId = null;
        Assert.assertNull(SpotifyQueries.getArtistDocById(artistId));
    }

    @Test
    public void testInvalidId(){
        String artistId = "Not an id!";
        Assert.assertNull(SpotifyQueries.getArtistDocById(artistId));
    }

    @Test
    public void testNullName(){
        String artistName = null;
        Assert.assertEquals(0, SpotifyQueries.getArtistDocsByName(artistName).size());
    }

    @Test
    public void testInvalidName(){
        String artistName = "there better not be an artist with this name";
        Assert.assertEquals(0, SpotifyQueries.getArtistDocsByName(artistName).size());
    }

    @Test
    public void testValidName(){
        String artistName = "JPEGMAFIA";
        Assert.assertTrue(SpotifyQueries.getArtistDocsByName(artistName).size() >= 0);
    }
}
