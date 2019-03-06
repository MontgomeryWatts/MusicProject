<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <a class="navbar-brand" href="/">Home Page</a>
        <ul class="nav navbar-nav">
            <li><a href="/playlist">Create Playlist</a></li>
            <li class="dropdown">
                <a class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                    <span class=" glyphicon glyphicon-globe"></span> Discover<span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li><a href="/artists">Get 20 random artists</a></li>
                    <li><a href="/artists/random">Go to random artist</a></li>
                    <li role="separator" class="divider"></li>
                    <li><a href="/genres">Genres</a></li>
                </ul>
            </li>
        </ul>
        <form class="navbar-form navbar-right" method="get" action="/search">
            <div class="form-group">
                <input type="text" class="form-control" placeholder="Artist Name" name="artist_name" id="artist_input">
            </div>
            <button type="submit" class="btn btn-default" onclick="return notEmpty('artist_input')">
                <span class="glyphicon glyphicon-search"></span> Search
            </button>
        </form>
    </div>
</nav>