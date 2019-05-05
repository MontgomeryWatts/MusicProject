package com.spotifydb.model;

public class Preview {

    private String internalLink;
    private String externalLink;
    private String imageUrl;
    private String text;

    public enum Type {
        ARTIST,
        ALBUM,
        SONG
    }

    public Preview(Type type, String artistId, String resourceId, String imageUrl, String text){
        this.internalLink = "/artists/" + artistId;
        this.imageUrl = imageUrl;
        this.text = text;

        switch (type){
            case ARTIST:
                this.externalLink = "spotify:artist:" + resourceId;
                break;
                // Both albums and songs have their external links stored, as there is currently no
                // plans to have an internal route such as /albums or /songs
            case ALBUM:
                this.externalLink = resourceId;
                break;
            case SONG:
                this.externalLink = resourceId;
                break;
            default:
                this.externalLink = "spotify:track:7m5ImlszwdzMxtkF8ldGzS";
                break;
        }
    }

    public String getInternalLink(){
        return internalLink;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }
}
