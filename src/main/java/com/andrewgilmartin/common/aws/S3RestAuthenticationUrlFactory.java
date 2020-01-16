package com.andrewgilmartin.common.aws;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Factory to create URL that use Amazon S3 REST Authentication for
 * non-anonymous access.
 *
 * http://s3.amazonaws.com/doc/s3-developer-guide/RESTAuthentication.html
 */
public class S3RestAuthenticationUrlFactory {

    private final String awsAccessKeyId;
    private final String awsSecretKey;

    public S3RestAuthenticationUrlFactory(String awsAccessKeyId, String awsSecretKey) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretKey = awsSecretKey;
    }

    /**
     * Returns a URL to HTTP GET the given item. The returned URL is valid only
     * the for the given item until now + seconds-to-live.
     */
    public String create(String bucket, String item, long secondsToLive) {
        return create("GET", bucket, item, secondsToLive);
    }

    /**
     * Returns a URL to access the given item. The returned URL is valid only
     * the for the given item until now + seconds-to-live.
     */
    public String create(String httpMethod, String bucket, String item, long secondsToLive) {
        String path = "/" + uriEncode(bucket) + "/" + uriEncode(item);
        String expires = Long.toString(System.currentTimeMillis() / 1000 + secondsToLive);
        String url = "https://s3.amazonaws.com" + path
                + "?AWSAccessKeyId=" + awsAccessKeyId
                + "&Expires=" + expires
                + "&Signature=" + base64Encode(
                        sha1Encrypt(
                                awsSecretKey,
                                httpMethod + "\n"
                                /* empty content MD5  */ + "\n"
                                /* empty content type */ + "\n"
                                + expires + "\n"
                                + path
                        )
                );
        return url;
    }

    /**
     * Convenience method to URI encode the value.
     */
    private static String uriEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Convenience method to base64 encode the value.
     */
    private static String base64Encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    /**
     * Convenience method to SHA1 cryptographic hash the value with the (shared)
     * secret.
     */
    private static byte[] sha1Encrypt(String secret, String data) {
        try {
            String algorithm = "HmacSHA1";
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}

// END
