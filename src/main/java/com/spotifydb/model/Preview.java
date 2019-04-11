package com.spotifydb.model;

public class Preview {

    private String id;
    private String imageUrl;
    private String text;

    public Preview(String id, String imageUrl, String text){
        this.id = id;
        this.imageUrl = imageUrl;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }
}
