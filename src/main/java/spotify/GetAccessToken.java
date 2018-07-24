package spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
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
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(DisplayAlbums.class, "/");

        final String clientId = "9e3cd263a287408b9346b2a06a56cbcb";
        final String clientSecret = "15fbb67cda8c4faf8e75061c26d2ef4a";
        final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost");
        final String code = "";

        final SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();

        final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                //.state("x4xkmn9pu3j6ukrs8n")
                .scope("playlist-modify-public")
                .show_dialog(true)
                .build();

        final URI uri = authorizationCodeUriRequest.execute();

        Spark.setPort(80);

        get(new Route("/"){
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
}
