package com.freshbooks;


import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scribe.exceptions.OAuthConnectionException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.freshbooks.model.Request;
import com.freshbooks.model.Response;
import com.thoughtworks.xstream.XStream;

public class OAuthApiConnection extends AbstractApiConnection
{

    static final Log logger = LogFactory.getLog(OAuthApiConnection.class);
    private OAuthService oauth;

    private Token token;
    private String url;
    private boolean debug;
    private int MAX_RETRIES = 3;
    private long lastRequestTime = 0;

    /**
     * The time zone in which the FreshBooks servers are.
     */
    private TimeZone freshBooksTimeZone = TimeZone.getTimeZone("EST5EDT");

    public OAuthApiConnection(OAuthService oauthService, Token token, String url, String userAgent)
    {
        super(url, userAgent, "https", "/api/2.1/xml-in", "EST5EDT");
        oauth = oauthService;
        this.token = token;
        this.url = url;
        this.lastRequestTime = 0;
    }

    @Override
    protected Response performRequest(Request request)
    {

        XStream xs = new CustomXStream(this.freshBooksTimeZone);
        checkRequestAndOmitFields(request, xs);
        String paramString = xs.toXML(request);

        OAuthRequest req = new OAuthRequest(Verb.POST, url);
        req.addPayload(paramString);
        req.addHeader("Content-Type", "application/xml");
        req.setCharset("UTF-8");


        oauth.signRequest(token, req);
        int retries = 3;
        boolean ok = false;
        org.scribe.model.Response resp = null;

        while (!ok && retries-- > 0)
        {
            try
            {
                spaceoutRequests();
                resp = req.send();
                ok = resp.isSuccessful();
            }
            catch (OAuthConnectionException e)
            {
                if (retries != 0)
                {
                    logger.warn(String.format("Request failed will retry %d more times", retries));
                }
                else
                {
                    logger.error(e);
                    throw e;
                }
            }
        }

        Response response = (Response) xs.fromXML(resp.getBody());

        if (!resp.isSuccessful())
        {
            throw new ApiException(
                    String.format("Code: %s, Message: %s, Body: %s", resp.getCode(), resp.getMessage(), resp.getBody()));
        }

        return response;
    }

    private void spaceoutRequests()
    {
        long sleep = 1000 - System.currentTimeMillis();

        try
        {
            if (sleep > 0)
            {
                Thread.sleep(sleep);
            }
        }
        catch (InterruptedException ie)
        {
            logger.error("Interrupted", ie);
        }

    }
}
