package db.updates.old;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * OBSOLETE! Use AddArtistAndSongs
 * After creating the authors
 */

public class UpdateArtistGenresSelenium {
    private WebDriver browser;

    UpdateArtistGenresSelenium(){
        browser = new FirefoxDriver();
        browser.get("http://everynoise.com/engenremap.html");

        WebElement iframe = browser.findElement(By.xpath("/html/body/iframe"));
        browser.switchTo().frame(iframe);

    }

    public static void main(String[] args) {

        //Setup MongoDB connection and start up Selenium
        MongoClient client = new MongoClient();
        MongoDatabase db = client.getDatabase("music");
        MongoCollection<Document> col = db.getCollection("artists");
        UpdateArtistGenresSelenium artistGenres = new UpdateArtistGenresSelenium();

        //Iterates through every artist without a genres array, searches their genres, and updates their document
        for (Document doc: col.find(new Document( "genres", new Document("$exists", false)))){
            List<String> genres = artistGenres.genreSearch( doc.getString("_id"));
            Document replacement = new Document("$set", new Document("genres", genres));
            col.updateOne(doc, replacement);
        }


        artistGenres.close();
    }

    /**
     * Looks up an artist's name and fills an array with the genres retrieved from a webpage
     * @param artist The artist whose genres array is being updated
     * @return An array representing the genres of the passed artist
     */
    public List<String> genreSearch(String artist){
        List<String> genres = new ArrayList<String>();

        //Search bar gets refreshed between searches, needs to be reinitialized if multiple searches are performed
        WebElement searchbar = browser.findElement(By.xpath("/html/body/form/input[1]"));

        searchbar.click();
        searchbar.clear();
        searchbar.sendKeys(artist);
        searchbar.submit();

        //A sleep is used instead of a WebDriverWait because the div element is already present, but will be refreshed
        //sometime after submit is called on the search bar. Otherwise the genres list will always be empty.
        try{
            Thread.sleep(5000);
        } catch (Exception e){
            System.out.println("test");
        }

        WebElement genreDiv = browser.findElement(By.xpath("/html/body/div"));

        for( WebElement genre : genreDiv.findElements(By.tagName("a")) )
            genres.add(genre.getText());

        return genres;
    }

    /**
     * Closes Selenium's WebDriver
     */
    public void close(){
        browser.close();
    }
}
