<!DOCTYPE html>
<html>
    <head>
        <title><#if album??>${album["title"]}</#if></title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "/html/navigation.html">
        <#if album??>
            <#list album["songs"] as song>
                <a href="${song["uri"]}">
                    <p>
                        ${song["title"]} - ${(song["duration"]/60)?int}:<#if (song["duration"]%60)?int < 10>0</#if>${song["duration"]%60}
                    </p>
                </a>
            </#list>
        </#if>
    </body>

</html>