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

    public Preview(Type type, String internalID, String externalID, String imageUrl, String text){

        this.imageUrl = imageUrl;
        this.text = text;

        switch (type){
            case ARTIST:
                this.internalLink = "/artists/" + internalID;
                this.externalLink = "spotify:artist:" + externalID;
                break;
                // Both albums and songs have their external links stored, as there is currently no
                // plans to have an internal route such as /albums or /songs
            case ALBUM:
                this.internalLink = "/albums/" + internalID;
                this.externalLink = "spotify:album:" + externalID;
                break;
            case SONG:
                this.internalLink = "/albums/" + internalID;
                this.externalLink = "spotify:track:" + externalID;
                break;
            default:
                this.internalLink = "/albums/0YrdQQiUYjNmLPs0SI53qy";
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
