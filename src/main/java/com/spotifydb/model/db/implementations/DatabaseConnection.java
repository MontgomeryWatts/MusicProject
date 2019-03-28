package com.spotifydb.model.db.implementations;

import com.spotifydb.model.db.daos.ArtistDAO;
import com.spotifydb.model.db.daos.GenreDAO;
import com.spotifydb.model.db.daos.SongDAO;

public abstract class DatabaseConnection implements ArtistDAO, GenreDAO, SongDAO {
}
