<!DOCTYPE html>
<html>
    <head>
        <title>Displays a searched artist's albums</title>
        <style type="text/css">
            .label { text-align: right}
        </style>
    </head>

    <body>

        <form method="post">
            <table>
                <tr>
                    <td class="label">
                        Artist
                    </td>

                    <td>
                        <input type="text" name="input">
                    </td>
                </tr>
            </table>
            <input type="submit">
        </form>

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