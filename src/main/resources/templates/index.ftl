<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Welcome to SpotifyDB!</title>
        <#include "head.ftl">
    </head>
    <body>
        <#include "navbar.ftl">

        <div id="myCarousel" class="carousel slide" data-ride="carousel">

            <!-- Carousel indicators -->

            <ol class="carousel-indicators">

                <li data-target="#myCarousel" data-slide-to="0" class="active"></li>

                <li data-target="#myCarousel" data-slide-to="1"></li>

                <li data-target="#myCarousel" data-slide-to="2"></li>

            </ol>

            <!-- Wrapper for carousel items -->

            <div class="carousel-inner">

                <div class="item active">
                    <img class="img-responsive center-block" src="/images/1.png" alt="First Slide">
                    <div class="carousel-caption">
                        <h2>Create playlists</h2>
                        <p>Slide 1 info</p>
                    </div>
                </div>

                <div class="item">
                    <img class="img-responsive center-block" src="/images/2.png" alt="Second Slide">
                    <div class="carousel-caption">
                        <h2>Discover new artists</h2>
                        <p>Slide 2 info</p>
                    </div>
                </div>

                <div class="item">
                    <img class="img-responsive center-block" src="/images/3.png" alt="Third Slide">
                    <div class="carousel-caption">
                        <h2>Discover new genres</h2>
                        <p>Slide 3 info</p>
                    </div>
                </div>

            </div>

            <!-- Carousel controls -->

            <a class="carousel-control left" href="#myCarousel" data-slide="prev">

                <span class="glyphicon glyphicon-chevron-left"></span>

            </a>

            <a class="carousel-control right" href="#myCarousel" data-slide="next">

                <span class="glyphicon glyphicon-chevron-right"></span>

            </a>

        </div>

        <#include "javascript.ftl">
    </body>
</html>