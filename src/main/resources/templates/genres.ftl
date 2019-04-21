<!DOCTYPE html>
<html lang="en">
    <head>
        <title>${title}</title>
        <#include "head.ftl">
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
                            <div class="col-xs-6 col-md-3">
                                <a href="/search?type=artist&genre=${genre}&page=1">
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

        <#include "javascript.ftl">
    </body>

</html>