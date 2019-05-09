package com.spotifydb.model;

import java.util.ArrayList;
import java.util.List;

public class PreviewPage {

    private boolean hasNext;

    List<Preview> previews;

    public PreviewPage(){
        this.hasNext = false;
        this.previews = new ArrayList<>();
    }

    public void setNext(boolean val){
        hasNext = val;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public List<Preview> getPreviews() {
        return previews;
    }

    public boolean hasItems(){
        return previews.size() > 0;
    }

    public void setPreviews(Iterable<Preview> previews){
        for (Preview preview: previews)
            this.previews.add(preview);
    }
}
