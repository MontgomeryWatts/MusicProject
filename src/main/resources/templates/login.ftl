<!DOCTYPE html>
<html lang="en">
<head>
    <title><#if artist??>${artist["name"]}</#if></title>
        <#include "head.ftl">
</head>

<body>
    <#include "navbar.ftl">
    <div class="container">
        <form>
            <div class="form-group">
                <input type="text" placeholder="Username">
            </div>

            <div class="form-group">
                <input type="password" placeholder="Password">
            </div>

            <input class="btn btn-default" type="submit">
        </form>
    </div>

    <#include "javascript.ftl">
</body>

</html>