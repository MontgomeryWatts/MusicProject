<!DOCTYPE html>
<html lang="en">
    <head>
        <title><#if artist??>${artist["_id"]["name"]}</#if></title>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link href="/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "/html/navigation.html">
        <div class="container">
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
                        <td rowspan="3" style="width: 200px;">
                            <a class="btn" data-toggle="collapse" data-target="#collapseExample${album?index}" style="padding: 6px 0px;">
                                <#if album["image"]??>
                                    <img src="${album["image"]}" class="album-image">
                                </#if>
                            </a>
                        </td>
                        <td>
                            <span>&nbsp;</span>
                            <a href="${album["uri"]}">
                                ${album["title"]}
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <span>&nbsp;</span>
                            ${album["year"]?c}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <span>&nbsp;</span>
                            <#if album["is_explicit"]>
                                <b>Explicit</b>
                            <#else>
                                Clean
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                        <div id="collapseExample${album?index}" aria-expanded="true" style="" class="panel panel-default collapse">

                            <ol class="list-group">
                                <#list album["songs"] as song>
                                    <a class="list-group-item" href="${song["uri"]}">
                                        ${song["title"]} - ${(song["duration"]/60)?int}:<#if (song["duration"]%60)?int < 10>0</#if>${song["duration"]%60}
                                    </a>
                                </#list>

                            </ol>
                        </div>
                        </td>
                    </tr>
                </#list>
                </table>

            </#if>
        </div>

        <script src="/js/formValidation.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="/js/bootstrap.min.js"></script>
    </body>

</html>