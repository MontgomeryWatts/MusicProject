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
                    <div id="playlistCarousel" class="carousel slide" data-ride="carousel" data-interval="false">
                        <div class="carousel-inner">
                        <#list songs as song>
                            <div class="item <#if song["uri"]=songs?first["uri"]>active</#if>">
                                <a href="${song["uri"]}">
                                    <img class="img-responsive center-block" src="${song["image"]}" alt="Album Art">
                                </a>
                                <div class="carousel-caption transparent-background">
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
                    </div>
                <#else>
                <h1>
                    NO SONGS FOUND
                </h1>
                </#if>
            </div>

        <#include "javascript.ftl">
    </body>

</html>