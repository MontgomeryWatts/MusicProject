<!DOCTYPE html>
<html>
<head>
    <title>Displays random artists</title>
    <link rel="stylesheet" href="/css/style.css">
</head>

<body>
<#if artist??>
    <#list artist["albums"] as album>
        <a href=${album["uri"]}>
            <img src="${album["image"]}" class="album">
        </a>
        <p>${album["title"]}</p>
        <br>
    </#list>
</#if>


</body>

</html>