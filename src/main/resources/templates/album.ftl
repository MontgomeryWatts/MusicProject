<!DOCTYPE html>
<html>
    <head>
        <title>Displays random artists</title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#if album??>
            <#list album["songs"] as song>
                <a href="${song["uri"]}">
                    <p>${song["title"]} - ${song["duration"]}</p>
                </a>
            </#list>
        </#if>
    </body>

</html>