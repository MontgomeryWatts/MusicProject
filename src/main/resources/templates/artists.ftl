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
                    <#list artists?chunk(2) as row>
                        <div class="row">
                            <#list row as artist>
                                <#if artist??>
                                    <#if artist=row?first>
                                    <div class="col-xs-10 col-xs-offset-1 col-md-offset-1 col-md-4 no-text-overflow">
                                    <#else>
                                    <div class="col-xs-10 col-xs-offset-1 col-md-offset-2 col-md-4 no-text-overflow">
                                    </#if>
                                        <a href="/artists/${artist.id}">
                                            <img src="${artist.imageUrl}" class="artist-preview">
                                        </a>
                                        <span>&nbsp;</span>
                                        <a href="spotify:artist:${artist.id}">
                                            ${artist.text}
                                        </a>
                                </div>
                                </#if>
                            </#list>
                        </div>

                    </#list>
                 <nav>

                     <div class="text-center">
                         <ul class="pagination">
                         <#if hasPrev??>
                            <li><a style="color:black" href="${prevLink}"><span>&laquo;</span></a></li>
                            <li><a style="color:black" href="${prevLink}">${page - 1}</a></li>
                         </#if>

                         <#if hasPrev?? || hasNext??>
                            <li class="active"><a style="color:black">${page}</a></li>
                         </#if>

                         <#if hasNext??>
                            <li><a style="color:black" href="${nextLink}">${page + 1}</a></li>
                            <li><a style="color:black" href="${nextLink}"><span>&raquo;</span></a></li>
                         </#if>
                         </ul>
                     </div>

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