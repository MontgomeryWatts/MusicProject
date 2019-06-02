<!DOCTYPE html>
<html lang="en">
<head>
    <title><#if artist??>${artist["album"]["title"]}</#if></title>
        <#include "head.ftl">
</head>

<body>
        <#include "navbar.ftl">
            <div class="container">
                <#if artist??>
                    <div class="row">
                        <#assign album=artist["album"]>

                        <div class="media">
                            <img src="${album["image"]}" class="album-image pull-left">

                            <a href="spotify:album:${album["albumId"]}">
                                <h1>${album["title"]}</h1>
                            </a>
                            <p>By
                                <#list artist["credits"] as credit>
                                    <a href="/artists/${credit["artistId"]}">
                                        ${credit["name"]}<#if credit_has_next>, </#if>
                                    </a>
                                </#list>
                            </p>
                            <p>Released ${album["release_date"]?date}</p>
                        </div>

                        <br>


                        <#list album["songs"] as song>
                           <a class="list-group-item text-center" href="spotify:track:${song["trackId"]}">
                               <small class="pull-left">
                                   ${song?index + 1}
                               </small>

                               ${song["title"]}

                               <small class="pull-right">
                                   ${(song["duration"]/60)?int}:<#if (song["duration"]%60)?int < 10>0</#if>${song["duration"]%60}
                               </small>
                           </a>
                        </#list>
                    </div>
                </#if>
            </div>

        <#include "javascript.ftl">
</body>

</html>