package ameba.websocket.sockjs.transport;

import com.google.common.base.Charsets;

/**
 * @author icode
 */
public class IFrameUtils {

    public static byte[] generateIFrame(String origin) {
        return ("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "  <script>\n" +
                "    document.domain = document.domain;\n" +
                "    _sockjs_onload = function(){SockJS.bootstrap_iframe();};\n" +
                "  </script>\n" +
                "  <script src=\"" + origin + "\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <h2>Don't panic!</h2>\n" +
                "  <p>This is a SockJS hidden iframe. It's used for cross domain magic.</p>\n" +
                "</body>\n" +
                "</html>").getBytes(Charsets.UTF_8);
    }

    public static byte[] generateHtmlFile(String callback) {
        return ("<!doctype html>\n" +
                "<html><head>\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
                "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                "</head><body><h2>Don't panic!</h2>\n" +
                "  <script>\n" +
                "    document.domain = document.domain;\n" +
                "    var c = parent." + callback + ";\n" +
                "    c.start();\n" +
                "    function p(d) {c.message(d);};\n" +
                "    window.onload = function() {c.stop();};\n" +
                "  </script>").getBytes(Charsets.UTF_8);
    }
}
