<!DOCTYPE html>
<html lang="en">
    <head>
        <title><#if artist??>${artist["name"]}</#if></title>
        <#include "head.ftl">
    </head>

    <body>
        <#include "navbar.ftl">
        <div class="container">
            <#if artist??>
                <div class="row text-center">
                    <a href="spotify:artist:${artist["_id"]}">
                        <h1>${artist["name"]}</h1>
                        <img src=<#if artist["images"]?has_content>${artist["images"][1]}<#else>"/images/blank_profile_pic.png"</#if> class="preview">
                    </a>
                </div>


                <#if artist["genres"]??>
                    <br>
                    <div class="row text-center">
                        <h2>Genres</h2>
                        <#list artist["genres"] as genre>
                            <a href="/search?type=artist&genre=${genre?replace('&', '%26')?replace('+', '%2B')}&page=1" class="tag">${genre}</a>
                        </#list>
                    </div>
                </#if>

                <#if artist["albums"]??>
                    <br>
                    <div class="row text-center">
                        <h2>Albums</h2>
                        <#list artist["albums"] as album>
                           <div class="media col-md-3">
                               <div>
                                   <a class="media-object" href="/albums/${album["albumId"]}">
                                       <img src=<#if album["image"]??>${album["image"]}<#else>"/images/no_album_art.png"</#if> class="album-image">
                                   </a>
                               </div>

                               <div class="no-text-overflow">
                                   <br>
                                   <a href="spotify:album:${album["albumId"]}">
                                       ${album["title"]}
                                   </a>
                                   <br>

                               </div>
                           </div>
                        </#list>
                    </div>
                    <br>
                </#if>


            </#if>
        </div>

        <#include "javascript.ftl">
    </body>

</html>