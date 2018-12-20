<!DOCTYPE html>
<html>
    <head>
        <title><#if artist??>${artist["_id"]["name"]}</#if></title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "/html/navigation.html">
        <#if artist??>
            <h1>${artist["_id"]["name"]}</h1>
            <#if artist["_id"]["image"]??>
                <img src="${artist["_id"]["image"]}" class="artist-preview">
            </#if>
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
            <table>
                <#list artist["albums"] as album>
                    <tr>
                        <td rowspan="3">
                            <a href="/artists/${artist["_id"]["uri"]}/${album["uri"]}">
                                <#if album["image"]??>
                                    <img src="${album["image"]}" class="album-image">
                                </#if>
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