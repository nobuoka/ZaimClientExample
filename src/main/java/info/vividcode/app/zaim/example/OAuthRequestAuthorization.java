package info.vividcode.app.zaim.example;

import info.vividcode.util.oauth.OAuthRequestHelper;
import info.vividcode.util.oauth.OAuthRequestHelper.ParamList;

import java.security.GeneralSecurityException;
import java.util.Date;

interface OAuthCredentialsHolder {
    String getClientIdentifier();
    String getClientSharedSecret();
    void setClientCredential(String identifier, String secret);

    String getTokenIdentifier();
    String getTokenSharedSecret();
    void setTokenCredential(String identifier, String secret);
}

abstract class OAuthRequestAuthorization<T> implements OAuthCredentialsHolder {

    // Consumer key と secret
    private String mClientIdentifier = "";
    private String mClientSharedSecret = "";

    @Override
    public final String getClientIdentifier() {
        return mClientIdentifier;
    }

    @Override
    public final String getClientSharedSecret() {
        return mClientSharedSecret;
    }

    @Override
    public final void setClientCredential(String identifier, String secret) {
        mClientIdentifier = identifier;
        mClientSharedSecret = secret;
    }

    // 今回は request token を求める例で token secret はないので空文字列
    private String mTokenIdentifier = "";
    private String mTokenSharedSecret = "";

    @Override
    public final String getTokenIdentifier() {
        return mTokenIdentifier;
    }

    @Override
    public final String getTokenSharedSecret() {
        return mTokenSharedSecret;
    }

    @Override
    public final void setTokenCredential(String identifier, String secret) {
        mTokenIdentifier = identifier;
        mTokenSharedSecret = secret;
    }

    interface OAuthParamsGenerator {
        OAuthRequestHelper.ParamList generate(OAuthCredentialsHolder auth);
    }

    static class GeneralTemporaryCredentialOAuthParamsGenerator
    implements OAuthParamsGenerator {
        private final String mCallbackUrlStr;

        public GeneralTemporaryCredentialOAuthParamsGenerator(String callbackUrl) {
            mCallbackUrlStr = callbackUrl;
        }

        @Override
        public ParamList generate(OAuthCredentialsHolder auth) {
            return new OAuthRequestHelper.ParamList(
                new String[][]{
                        { "oauth_consumer_key", auth.getClientIdentifier() },
                        { "oauth_nonce", OAuthRequestHelper.getNonceString() },
                        { "oauth_signature_method", "HMAC-SHA1" },
                        { "oauth_timestamp", Long.toString( new Date().getTime() / 1000 ) },
                        { "oauth_version", "1.0" },
                        { "oauth_callback", this.mCallbackUrlStr },
                      } );
        }
    }

    public static OAuthParamsGenerator createGeneralTemporaryCredentialOAuthParamsGenerator(String callbackUrl) {
        return new GeneralTemporaryCredentialOAuthParamsGenerator(callbackUrl);
    }

    private OAuthParamsGenerator mOAuthParamListGenerator =
            createGeneralTemporaryCredentialOAuthParamsGenerator("http://www.vividcode.info/");

    protected abstract String getRequestMethod(T target);
    protected abstract String buildOAuthParamUrl(T target);
    protected abstract ParamList getQueryParamList(T target);
    protected abstract ParamList getRequestBodyParamList(T target);

    protected abstract void putAuthorization(T target, OAuthRequestHelper helper);

    public void authorize(T target) throws GeneralSecurityException {
        String urlStr = buildOAuthParamUrl(target);
        // リクエストメソッド
        String method = getRequestMethod(target);
        // secrets 文字列 (Consumer secret と token secret を繋いだもの)
        String secrets = mClientSharedSecret + "&" + mTokenSharedSecret;
        // OAuth 関係のパラメータ
        ParamList paramList = mOAuthParamListGenerator.generate(this);
        // OAuthRequestHelper のインスタンス化
        // 今回はクエリパラメータにもリクエストボディにも情報を載せないので, 後ろ 2 つの引数は null
        OAuthRequestHelper helper = new OAuthRequestHelper( urlStr, method, secrets, paramList, null, null );
        // インスタンス化と同時にシグニチャ生成もされるので, あとは helper から情報を取って
        // リクエストを送信するだけ
        System.out.println(urlStr);
        System.out.println(helper.getAuthorizationHeaderString(""));
        putAuthorization(target, helper);
    }

}
