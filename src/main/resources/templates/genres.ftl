<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${title}</title>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link href="/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "navbar.ftl">
        <div class="container">
            <ul class="list-inline text-center">
                <#list alphabet as letter>
                    <li><a href="/genres?letter=${letter}">${letter}</a></li>
                </#list>
                <li><a href="/genres/">ALL</a></li>
            </ul>


            <#if genres?has_content>
                <#list genres?chunk(4) as row>
                <div class="row">
                    <#list row as genre>
                            <div class="col-md-3">
                                <a href="/genres/${genre}?p=1">
                                    ${genre}
                                </a>
                            </div>
                    </#list>
                </div>
                </#list>
            <#else>
            <h1>
                NO GENRES FOUND
            </h1>
            </#if>
        </div>


        <script src="/js/formValidation.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="/js/bootstrap.min.js"></script>
    </body>

</html>