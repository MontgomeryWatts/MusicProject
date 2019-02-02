package db.implementations;

import db.daos.ArtistDAO;
import db.daos.GenreDAO;
import db.daos.SongDAO;

public abstract class DatabaseConnection implements ArtistDAO, GenreDAO, SongDAO {
}
