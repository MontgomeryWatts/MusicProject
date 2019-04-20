<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${title}</title>
        <#include "head.ftl">
    </head>

    <body>
        <#include "navbar.ftl">
        <div class="container">
            <form action="/search" method="post">
                <div class="form-group" >
                    <label for="typeSelector" class="col-md-2">Search Type</label>
                    <select id="typeSelector" class="col-md-6">
                        <option selected="selected" value="artist">Artist</option>
                        <option value="album">Album</option>
                        <option value="song">Song</option>
                    </select>
                </div>
            </form>

        </div>

        <#include "javascript.ftl">
    </body>

</html>