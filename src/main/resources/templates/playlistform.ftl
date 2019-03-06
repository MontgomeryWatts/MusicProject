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
            <table>
                <tr>
                    <td class="label"> Artist Name </td>
                    <td>
                        <input type="text" placeholder="artist name" name="artist_input">
                    </td>
                </tr>
                <tr>
                    <td class="label"> Genre </td>
                    <td>
                        <input type="text" placeholder="genre" name="genre_input">
                    </td>
                </tr>
                <tr>
                    <td class="label"> Length </td>
                    <td>
                        <input type="text" placeholder="hours" name="hour_input" id="hour_input">
                    </td>
                    <td>
                        <input type="text" placeholder="minutes" name="minute_input" id="minute_input">
                    </td>
                </tr>
                <tr>
                    <td class="label"> Allow Explicit? </td>
                    <td>
                        <input type="checkbox" name="explicit_input">
                    </td>
                </tr>
            </table>
            <input type="submit" value="Create Playlist" onclick="return isInteger('hour_input') && isInteger('minute_input')">
        </form>
    </div>

    <#include "javascript.ftl">
</body>
</html>