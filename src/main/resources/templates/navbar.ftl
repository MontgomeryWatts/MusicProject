<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <a class="navbar-brand brand-color" href="/">SpotifyDB</a>
        <button class="navbar-toggle" type="button" data-toggle="collapse" data-target="#navbar-collapsible">
            <span class="glyphicon glyphicon-align-justify"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbar-collapsible">
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
            <ul class="nav navbar-nav navbar-right">
                <li>
                    <form class="navbar-form" method="GET" action="/search">
                        <div class="form-group" style="display: flex">
                            <input type="hidden" value="artist" name="type">
                            <input type="text" class="form-control" placeholder="Artist Name" name="name" id="artist_input">
                            <button type="submit" class="btn btn-default" onclick="return notEmpty('artist_input')">
                                <span class="glyphicon glyphicon-search"></span> Search
                            </button>
                        </div>
                    </form>
                </li>
                <li class="navbar-link"><a href="/search">Advanced Search</a></li>
            </ul>

        </div>
    </div>
</nav>