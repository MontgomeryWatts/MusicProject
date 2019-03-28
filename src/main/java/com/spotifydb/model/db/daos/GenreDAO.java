package com.spotifydb.model.db.daos;

import java.util.List;

public interface GenreDAO {
    List<String> getGenres();
    List<String> getGenresByLetter(char c);
    String getNthPopularGenre(int n);
}
