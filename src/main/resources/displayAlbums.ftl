<!DOCTYPE html>
<html>
    <head>
        <title>Displays a predetermined artist's albums</title>
    </head>

    <body>

        <#if artist??>
            <p>${artist}'s Albums:</p>
            <#list albums as album>
                <a href="${album["uri"]}">
                    ${album["title"]}
                </a>
                <br>
            </#list>
        </#if>
    </body>

</html>