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
                    <#if artist["images"]?has_content>
                    <div class="col-md-6 no-text-overflow">
                        <div class="container-fluid">
                            <a href="/artists/${artist["_id"]}">
                                <img src="${artist["images"][1]}" class="artist-preview">
                            </a>
                            <span>&nbsp;</span>
                            <a href="spotify:artist:${artist["_id"]}">
                                ${artist["name"]}
                            </a>
                        </div>
                    </div>
                    </#if>
                </#list>
                </div>
            </#list>
                 <nav>
                     <ul class="pager">
                         <#if RequestParameters.p??>
                             <#if RequestParameters.p?number gte 2>
                            <li class="previous"><a style="color:black" href="${prevLink}"><span>←  </span>Previous</a></li>
                             </#if>
                         </#if>

                         <#if hasNext??>
                            <li class="next"><a style="color:black" href="${nextLink}">Next<span>  →</span></a></li>
                         </#if>
                     </ul>
                 </nav>
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