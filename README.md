SpotifyDB is a side project I started to have some hands-on learning with a full stack application. I go back and incorporate approaches and designs that I learn in classwork, so it's subject to change. 

It started as an exercise in MongoDB, and as such the document schema focuses heavily on embedding around artists. However I have plans for future features that are expensive to do with the current schema, and I may pivot to a more relational database. Ideally I would be able to plug-and-play any DBMS behind the scenes and the site would work irregardless. 

SpotifyDB is currently hosted on Heroku at spotifydb.com. I use MongoDB 3.6.8 for the database. The calls made to the Spotify API require credentials that can be obtained by signing up for a Spotify developer account.
