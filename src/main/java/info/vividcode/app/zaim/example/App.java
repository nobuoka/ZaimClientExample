package info.vividcode.app.zaim.example;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedParser;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

public class App {

    public static class OAuthTemporaryCredentialResponse extends GenericData {
        @Key("oauth_token")
        public String identifier;
        @Key("oauth_token_secret")
        public String sharedSecret;
        @Key("oauth_callback_confirmed")
        public String callbackConfirmed;
    }

    private static final String CLIENT_IDENTIFIER = "YOUR_CLIENT_IDENTIFIER";
    private static final String CLIENT_SHARED_SECRET = "YOUR_CLIENT_SHARED_SECRET";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // 抽象化された HTTP トランスポート。 スレッドセーフ。 アプリケーション中で 1 つのインスタンスを使うのが望ましいらしい。
        // 実装は複数あるので、別の実装を使っても良い。
        // See: http://javadoc.google-http-java-client.googlecode.com/hg/1.19.0/com/google/api/client/http/HttpTransport.html
        HttpTransport httpTransport = new NetHttpTransport();
        try {
            // HTTP トランスポートの上にのったスレッドセーフで軽量な HTTP リクエストのファクトリ。
            // オプションで HTTP リクエストの初期化子を持つことができる。
            // See: http://javadoc.google-http-java-client.googlecode.com/hg/1.19.0/com/google/api/client/http/HttpRequestFactory.html
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(
                    new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest req) throws IOException {
                            // エラーレスポンスでも例外を投げないようにしたりもできる。 今回はエラーレスポンスで例外を投げるように。
                            //req.setThrowExceptionOnExecuteError(false);
                            // タイムアウト時間を指定。
                            req.setConnectTimeout(10000);
                        }
                    });

            GoogleHttpClientOAuthRequestAuthorization auth = new GoogleHttpClientOAuthRequestAuthorization();
            auth.setClientCredential(CLIENT_IDENTIFIER, CLIENT_SHARED_SECRET);

            GenericUrl url = new GenericUrl("https://api.zaim.net/v2/auth/request");
            HttpRequest req = requestFactory.buildPostRequest(url, null);
            auth.authorize(req);

            HttpResponse res = req.execute();
            try {
                // エラーレスポンスだと execute の中で例外が投げられるので、ここに来るのは成功レスポンスの場合のみ。
                System.out.println(res.getStatusCode());
                if (!HttpMediaType.equalsIgnoreParameters(res.getContentType(), UrlEncodedParser.MEDIA_TYPE)) {
                    //throw new RuntimeException("Unexpected media type: " + res.getContentType());
                    System.out.println("Unexpected media type: " + res.getContentType());
                }
                UrlEncodedParser urlEncodedParser = new UrlEncodedParser();
                req.setParser(urlEncodedParser);
                OAuthTemporaryCredentialResponse d = res.parseAs(OAuthTemporaryCredentialResponse.class);
                System.out.println("identifier: " + d.identifier);
                System.out.println("shared-secret: " + d.sharedSecret);
                System.out.println("callback confirmed: " + d.callbackConfirmed);

                // ユーザーに許可してもらう。
                GenericUrl authUrl = new GenericUrl("https://auth.zaim.net/users/auth");
                authUrl.put("oauth_token", d.identifier);
                System.out.println("この URL をブラウザで開いてください: " + authUrl.build());
            } finally {
                res.disconnect();
            }
        } finally {
            // NetHttpTransport を使う場合は shutdown メソッドの中でなにも実行されないはずだが一応呼んでおく。
            httpTransport.shutdown();
        }
    }

}
