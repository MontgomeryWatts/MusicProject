<!DOCTYPE html>
<html>
    <head>
        <title><#if artist??>${artist["_id"]["name"]}</#if></title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#if artist??>
            <h1>${artist["_id"]["name"]}</h1>
            <img src="${artist["_id"]["image"]}" class="artist-preview">
            <br>
            <br>
            <h2>Genres</h2>
            <#list artist["genres"] as genre>
                <a href="/genres/${genre}?p=1">${genre}</a>
            </#list>

            <h2>Albums</h2>
            <#list artist["albums"] as album>
                <div>
                    <a href="/artists/${artist["_id"]["uri"]}/${album["uri"]}" class="image-link">
                        <img src="${album["image"]}" class="album-image">
                    </a>
                    <span>&nbsp;</span>
                    <a href="${album["uri"]}">
                        ${album["title"]}
                    </a>
                    <span>${album["year"]?c}</span>
                </div>
            </#list>
        </#if>


    </body>

</html>