<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Displays generated playlist</title>
        <#include "head.ftl">
    </head>

    <body>
            <#include "navbar.ftl">
            <div class="container">
                <#if songs?has_content>
                    <a class="btn center-block" data-toggle="collapse" data-target="#playlistCarousel">
                        Toggle Carousel
                    </a>
                    <div id="playlistCarousel" class="carousel slide collapse in" data-ride="carousel" data-interval="false">
                        <div class="carousel-inner">
                        <#list songs as song>
                            <div class="item <#if song["_id"]=songs?first["_id"]>active</#if>">
                                <a href="spotify:track:${song["_id"]}">
                                    <img class="img-responsive center-block" src="${song["image"]}" alt="Album Art">
                                </a>
                                <div class="carousel-caption playlist-caption transparent-background">
                                    <h2>${song["title"]}</h2>
                                    <p>${song["artist"]}</p>
                                </div>
                            </div>
                        </#list>
                        </div>
                        <a class="carousel-control left" href="#playlistCarousel" data-slide="prev">
                            <span class="glyphicon glyphicon-chevron-left"></span>
                        </a>

                        <a class="carousel-control right" href="#playlistCarousel" data-slide="next">
                            <span class="glyphicon glyphicon-chevron-right"></span>
                        </a>
                    </div> <#-- End of carousel -->

                    <a class="btn center-block collapsed" data-toggle="collapse" data-target="#playlistAsList">
                        Toggle List
                    </a>
                    <ol class="list-group collapse" id="playlistAsList">
                    <#list songs as song>
                                    <a class="list-group-item" href="spotify:track:${song["_id"]}">
                                        ${song["title"]} - ${song["artist"]} &emsp;&emsp; ${(song["duration"]/60)?int}:<#if (song["duration"]%60)?int < 10>0</#if>${song["duration"]%60}
                                    </a>
                    </#list>
                    </ol>
                <#else>
                <h1>
                    NO SONGS FOUND
                </h1>
                </#if>
            </div>

        <#include "javascript.ftl">
    </body>

</html>