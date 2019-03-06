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
                <#list songs as song>
                    <a href="${song["uri"]}">
                        ${song["title"]} - ${song["artist"]} &emsp;&emsp; ${(song["duration"]/60)?int}:<#if (song["duration"]%60)?int < 10>0</#if>${song["duration"]%60}
                    </a>
                    <br>
                </#list>
                <#else>
                <h1>
                    NO SONGS FOUND
                </h1>
                </#if>
            </div>

        <#include "javascript.ftl">
    </body>

</html>