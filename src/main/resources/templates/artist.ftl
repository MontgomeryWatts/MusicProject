<!DOCTYPE html>
<html>
    <head>
        <title>Displays random artists</title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#if artist??>
            <#list artist["albums"] as album>
                <a href="/artists/${artist["_id"]["uri"]}/${album["uri"]}">
                    <img src="${album["image"]}" class="album">
                </a>
                <a href=${album["uri"]}>
                    <p>${album["title"]}</p>
                </a>
                <br>
            </#list>
        </#if>


    </body>

</html>