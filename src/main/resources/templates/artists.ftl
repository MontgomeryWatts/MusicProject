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
                    <#if artist["images"]?has_content>
                    <div class="col-md-6 no-text-overflow">
                        <div class="container-fluid">
                            <a href="/artists/${artist["_id"]}">
                                <img src="${artist["images"][1]}" class="artist-preview">
                            </a>
                            <span>&nbsp;</span>
                            <a href="spotify:artist:${artist["_id"]}">
                                ${artist["name"]}
                            </a>
                        </div>
                    </div>
                    </#if>
                </#list>
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