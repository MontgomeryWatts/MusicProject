package com.spotifydb.ui.legacy;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.spotifydb.model.db.spotify.SpotifyQueries;
import freemarker.template.Configuration;
import freemarker.template.Template;
import spark.*;

import javax.servlet.http.Cookie;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class GetAccessToken {
    /*
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(GetAccessToken.class, "/");

        final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost/access");


        final SpotifyApi spotifyApi = SpotifyQueries.createSpotifyAPI(redirectUri);

        final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("playlist-modify-public")
                .show_dialog(true)
                .build();

        final URI uri = authorizationCodeUriRequest.execute();

        Spark.setPort(80);

        get(new Route("/access"){
            @Override
            public Object handle(Request request, Response response) {
                StringWriter writer = new StringWriter();
                String code = request.queryParams("code");

                try{
                    Template t = config.getTemplate("getaccesstoken.ftl");
                    Map<String, Object> map = new HashMap<>();
                    map.put("link", uri);

                    if (code != null) {
                        map.put("code", code);
                        response.raw().addCookie(new Cookie("auth_code", code));
                    }


                    t.process(map, writer);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }

                return writer;
            }
        });

    }

    private String getCookie(final Request request) {
        if (request.raw().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.raw().getCookies()) {
            if (cookie.getName().equals("auth_code")) {
                return cookie.getValue();
            }
        }
        return null;
    }
    */
}
