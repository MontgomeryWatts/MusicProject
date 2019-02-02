package db.daos;

import java.util.List;

public interface GenreDAO {
    List<String> getGenres();
    List<String> getGenresByLetter(char c);
}
