<!DOCTYPE html>
<html>
    <head>
        <title>Displays random genres</title>
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
            <#if genres?has_content>
                <#list genres as genre>
                    <a href="/genres/${genre}" >${genre}</a>
                    <br>
                </#list>
            <#else>
                <h1>
                    NO GENRES FOUND
                </h1>
            </#if>


    </body>

</html>