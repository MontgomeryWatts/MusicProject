<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${title}</title>
        <#include "head.ftl">
    </head>

    <body>
        <#include "navbar.ftl">
        <div class="container">
             <#if artists?has_content>
                    <#list artists.iterator() as artist>
                        <div class="col-xs-10 col-xs-offset-2 col-md-offset-0 col-md-6 no-text-overflow">
                            <div class="container-fluid">
                                <a href="/artists/${artist.id}">
                                    <img src="${artist.imageUrl}" class="artist-preview">
                                </a>
                                <span>&nbsp;</span>
                                <a href="spotify:artist:${artist.id}">
                                    ${artist.text}
                                </a>
                            </div>
                        </div>
                    </#list>
                 <nav>
                     <ul class="pager">
                         <#if RequestParameters.p??>
                             <#if RequestParameters.p?number gte 2>
                            <li class="previous"><a style="color:black" href="${prevLink}"><span>←  </span>Previous</a></li>
                             </#if>
                         </#if>

                         <#if hasNext??>
                            <li class="next"><a style="color:black" href="${nextLink}">Next<span>  →</span></a></li>
                         </#if>
                     </ul>
                 </nav>
             <#else>
            <h1>
                NO ARTISTS FOUND
            </h1>
             </#if>
        </div>

        <#include "javascript.ftl">
    </body>

</html>