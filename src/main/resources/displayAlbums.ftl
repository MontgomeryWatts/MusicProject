<!DOCTYPE html>
<html>
    <head>
        <title>Displays a predetermined artist's albums</title>
    </head>

    <body>

        <#if artist??>
            <p>${artist}'s Albums:</p>
            <#list albums as album>
                <a href="${album["spotify"]}">
                    ${album["name"]}
                </a>
                <br>
            </#list>
        </#if>
    </body>

</html>