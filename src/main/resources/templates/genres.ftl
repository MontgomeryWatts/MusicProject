<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Displays random genres</title>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link href="/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "/html/navigation.html">
        <div class="container">
            <#if genres?has_content>
            <#list genres as genre>
                <a href="/genres/${genre}?p=1" >${genre}</a>
                <br>
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