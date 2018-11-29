<!DOCTYPE html>
<html>
    <head>
        <title><#if artist??>${artist["_id"]["name"]}</#if></title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body style="background: #10171E;">
        <#if artist??>
            <h1>${artist["_id"]["name"]}</h1>
            <img src="${artist["_id"]["image"]}" class="artist-preview">
            <br>
            <br>
            <h2>Genres</h2>
            <table>
                <tr>
                    <#list artist["genres"] as genre>
                        <a href="/genres/${genre}?p=1" class="tag">${genre}</a>
                    </#list>
                </tr>
            </table>


            <h2>Albums</h2>
            <table style="color: white;">
                <#list artist["albums"] as album>
                    <tr>
                        <td rowspan="3">
                            <a href="/artists/${artist["_id"]["uri"]}/${album["uri"]}">
                                <img src="${album["image"]}" class="album-image">
                            </a>
                        </td>
                        <td>
                            <a href="${album["uri"]}">
                                ${album["title"]}
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            ${album["year"]?c}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <#if album["is_explicit"]>
                                <b>Explicit</b>
                            <#else>
                                Clean
                            </#if>
                        </td>
                    </tr>
                </#list>
            </table>

        </#if>


    </body>

</html>