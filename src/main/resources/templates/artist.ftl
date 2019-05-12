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
                        <img src=<#if artist["images"]?has_content>${artist["images"][1]}<#else>"/images/blank_profile_pic.png"</#if> class="artist-preview">
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
                                   <a class="btn media-object" data-toggle="collapse" data-target="#collapsedAlbum${album?index}">
                                       <img src=<#if album["image"]??>${album["image"]}<#else>"/images/no_album_art.png"</#if> class="album-image">
                                   </a>
                               </div>

                               <div class="no-text-overflow">
                                   <br>
                                   <a href="${album["uri"]}">
                                       ${album["title"]}
                                   </a>
                                   <br>

                                   <span>${album["year"]?c}</span>
                               </div>

                               <div id="collapsedAlbum${album?index}" aria-expanded="true" class="panel panel-default collapse">

                                   <ol class="list-group">
                                       <#list album["songs"] as song>
                                           <a class="list-group-item" href="${song["uri"]}">
                                               <small class="pull-left">
                                                   ${song?index + 1}
                                               </small>

                                               ${song["title"]}

                                               <small class="pull-right">
                                                   ${(song["duration"]/60)?int}:<#if (song["duration"]%60)?int < 10>0</#if>${song["duration"]%60}
                                               </small>
                                           </a>
                                       </#list>
                                   </ol>
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