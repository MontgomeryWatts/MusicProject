<!DOCTYPE html>
<html>
    <head>
        <title>Displays a searched artist's albums</title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "/html/navigation.html">
        <form method="get" action="/search">
            <table>
                <tr>
                    <td class="label"> Artist Name </td>
                    <td>
                        <input type="text" name="artist_name">
                    </td>
                </tr>
            </table>
            <input type="submit" value="Search artist" >
        </form>

    </body>

</html>