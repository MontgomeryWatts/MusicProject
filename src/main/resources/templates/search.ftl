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
            <form method="get" action="/search">
                <table>
                    <tr>
                        <td class="label"> Artist Name </td>
                        <td>
                            <input type="text" name="artist_name" id="artist_input">
                        </td>
                    </tr>
                </table>
                <input type="submit" value="Search Artists" onclick="return notEmpty('artist_input')" >
            </form>
        </div>


        <script src="/js/formValidation.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <script src="/js/bootstrap.min.js"></script>
    </body>

</html>