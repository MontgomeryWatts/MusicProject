<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${title}</title>
        <#include "head.ftl">
    </head>

    <body>
        <#include "navbar.ftl">
        <div class="container">
            <form method="get" action="/search">
                <table>
                    <tr>
                        <td class="label"> Artist Name </td>
                        <td>
                            <input type="text" name="artist_name" id="artist_input">
                        </td>
                    </tr>
                </table>
                <input type="submit" value="Search Artists" onclick="return notEmpty('artist_input')" >
            </form>
        </div>

        <#include "javascript.ftl">
    </body>

</html>