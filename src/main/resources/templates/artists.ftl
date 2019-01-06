<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Displays random artists</title>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link href="/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="/css/style.css">
    </head>

    <body>
        <#include "/html/navigation.html">
        <div class="container">
             <#if artists?has_content>
            <#list artists?chunk(2) as row>
                <div class="row">
                <#list row as artist>
                    <#if artist["_id"]["image"]?has_content>
                    <div class="col-md-6 no-text-overflow">
                        <a href="/artists/${artist["_id"]["uri"]}">
                            <img src="${artist["_id"]["image"][1]}" class="artist-preview">
                        </a>
                        <span>&nbsp;</span>
                        <a href="spotify:artist:${artist["_id"]["uri"]}">
                            ${artist["_id"]["name"]}
                        </a>
                    </div>
                    </#if>
                </#list>
                </div>
            </#list>
             <#else>
            <h1>
                NO ARTISTS FOUND
            </h1>
             </#if>
        </div>


        <script src="/js/formValidation.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="/js/bootstrap.min.js"></script>
    </body>

</html>