<!DOCTYPE html>
<html>
    <head>
        <title>Displays random artists</title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#if artists?has_content>
            <#list artists as artist>
                <#if artist["_id"]["image"]??>
                    <div>
                        <a href="/artists/${artist["_id"]["uri"]}" class="image-link">
                            <img src="${artist["_id"]["image"]}" class="artist-preview">
                        </a>
                        <span>&nbsp;</span>
                        <a href="spotify:artist:${artist["_id"]["uri"]}">
                            ${artist["_id"]["name"]}
                        </a>
                    </div>
                </#if>
            </#list>
        <#else>
            <h1>
                NO ARTISTS FOUND
            </h1>
        </#if>


    </body>

</html>