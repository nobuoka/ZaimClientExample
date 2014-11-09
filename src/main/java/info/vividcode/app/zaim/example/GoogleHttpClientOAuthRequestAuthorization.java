package info.vividcode.app.zaim.example;

import info.vividcode.util.oauth.OAuthRequestHelper;
import info.vividcode.util.oauth.OAuthRequestHelper.ParamList;

import java.util.Arrays;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;

public class GoogleHttpClientOAuthRequestAuthorization extends OAuthRequestAuthorization<HttpRequest> {

    @Override
    protected String buildOAuthParamUrl(HttpRequest target) {
        GenericUrl url = target.getUrl();
        StringBuilder sb = new StringBuilder();
        sb.append(url.getScheme());
        sb.append("://");
        sb.append(url.getHost());
        int port = url.getPort();
        boolean isDefaultPort = (port == -1 || ("https".equals(url.getScheme()) ? port == 443 : port == 80));
        if (!isDefaultPort) {
            sb.append(':');
            sb.append(url.getPort());
        }
        sb.append(url.getRawPath());
        return sb.toString();
    }

    @Override
    protected String getRequestMethod(HttpRequest target) {
        return target.getRequestMethod().toUpperCase();
    }

    @Override
    protected ParamList getQueryParamList(HttpRequest target) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ParamList getRequestBodyParamList(HttpRequest target) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void putAuthorization(HttpRequest target, OAuthRequestHelper helper) {
        target.getHeaders().setAuthorization(Arrays.asList(helper.getAuthorizationHeaderString("")));
    }

}
