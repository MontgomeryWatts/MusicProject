<!DOCTYPE html>
<html>
<head>
    <title>Displays generated playlist</title>
    <link rel="stylesheet" href="/css/style.css">
</head>

<body>
        <#include "/html/navigation.html">
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


</body>

</html>