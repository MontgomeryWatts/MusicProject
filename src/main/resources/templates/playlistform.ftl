<!DOCTYPE html>
<html lang="en">
<head>
    <title>Playlist</title>
    <#include "head.ftl">
</head>
<body>
    <#include "navbar.ftl">
    <div class="container">
        <form method="post">
            <div class="form-group">
                <input type="text" placeholder="Artist Name" name="artist_input" id="artist_input">
            </div>

            <div class="form-group">
                <input type="text" placeholder="Genre" name="genre_input" id="genre_input">
            </div>

            <div class="form-group">
                <input type="text" placeholder="Hours" name="hour_input" id="hour_input">
                <input type="text" placeholder="Minutes" name="minute_input" id="minute_input">
            </div>
            <div class="checkbox">
                <label>
                    <input type="checkbox" name="explicit_input"> Allow explicit music
                </label>
            </div>
            <input type="submit" value="Create Playlist" onclick="return isInteger('hour_input') && isInteger('minute_input')">
        </form>
    </div>

    <#include "javascript.ftl">
</body>
</html>