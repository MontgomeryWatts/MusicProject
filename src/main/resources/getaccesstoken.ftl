<!DOCTYPE html>
<html>
<head>
    <title>Get Access Tokens</title>
</head>

<body>
    <a href="${link}"> Login to Spotify here! </a>
    <br>

<#if code??>
    <p>The access token is: ${code}</p>
</#if>
</body>

</html>