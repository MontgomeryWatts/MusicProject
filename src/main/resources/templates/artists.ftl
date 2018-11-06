<!DOCTYPE html>
<html>
    <head>
        <title>Displays random artists</title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#list artists as artist>
            <#assign local_link = "/artists/" + artist["_id"]["uri"]>
            <#assign spotify_link = "spotify:artist:" + artist["_id"]["uri"]>
            <#if artist["_id"]["image"]??>
                <a href=${local_link}>
                    <img src="${artist["_id"]["image"]}" class="artist-preview">
                </a>
                <a href=${spotify_link}>
                    <p>${artist["_id"]["name"]}</p>
                </a>
            </#if>
        </#list>

    </body>

</html>