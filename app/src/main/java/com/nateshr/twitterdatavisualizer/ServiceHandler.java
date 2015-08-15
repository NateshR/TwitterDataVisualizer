package com.nateshr.twitterdatavisualizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class ServiceHandler {

    static String consumerKeyStr = "uoXBq376lOht3LM5e1JZrdiAe";
    static String consumerSecretStr = "UB1HEL08KMqgMo68C5pCiLKKIK4K8LYCIKvs6OmJZAbW7ujEpJ";
    static String accessTokenStr = "2197461980-8Oghsaf6MF2O8uJnytl5vUtqJg71RUyCwKUWVyG";
    static String accessTokenSecretStr = "YKe7AXzlDmJY5cpHfBxBJ6P8DghSFTBJuCXOLuVAV23C4";


    public ServiceHandler() {

    }

    public String makeServiceCall(String url) {


        OAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKeyStr,
                consumerSecretStr);
        oAuthConsumer.setTokenWithSecret(accessTokenStr, accessTokenSecretStr);
        HttpGet httpGet = new HttpGet(url);

        try {
            oAuthConsumer.sign(httpGet);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(httpGet);

            HttpEntity httpEntity = httpResponse.getEntity();
            String jsonResponse = EntityUtils.toString(httpEntity);
            return jsonResponse;

        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
            return null;
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
            return null;
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
            return null;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
