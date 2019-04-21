<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${title}</title>
        <#include "head.ftl">
    </head>

    <body>
        <#include "navbar.ftl">
        <div class="container">
            <form action="/search" method="GET">
                <div class="form-group row" >
                    <label for="typeSelector" class="col-xs-offset-1 col-xs-4 col-md-offset-2 col-md-2">Search Type</label>
                    <select name="type" id="typeSelector" class="col-xs-6 col-md-6">
                        <option selected="selected" value="artist">Artist</option>
                        <option value="album">Album</option>
                        <option value="song">Song</option>
                    </select>
                </div>

                <div class="form-group row">
                    <label for="name" class="col-xs-offset-1 col-xs-4 col-md-offset-2 col-md-2">Name</label>
                    <input type="text" class="col-xs-6 col-md-6" placeholder="Name/Title" name="name" id="name">
                </div>

                <div class="form-group row">
                    <label for="genre" class="col-xs-offset-1 col-xs-4 col-md-offset-2 col-md-2">Artist Genre</label>
                    <input type="text" class="col-xs-6 col-md-6" placeholder="Genre" name="genre" id="genre">
                </div>

                <input class="col-xs-offset-5 col-xs-3 col-md-offset-6 col-md-2" type="submit" value="Search">
            </form>

        </div>

        <#include "javascript.ftl">
    </body>

</html>