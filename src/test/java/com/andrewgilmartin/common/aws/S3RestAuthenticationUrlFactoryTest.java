package com.andrewgilmartin.common.aws;

import static com.andrewgilmartin.common.util.StringUtils.isEmpty;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class S3RestAuthenticationUrlFactoryTest {

    private static String awsAccessKey;
    private static String awsSecretKey;
    private static String awsBucket;
    private static String[] awsItems; // eg "1-100.txt ☎.txt"

    @BeforeClass
    public static void beforeClass() {
        awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        awsBucket = System.getenv("AWS_BUCKET_NAME");
        awsItems = isEmpty(System.getenv("AWS_BUCKET_ITEMS")) ? null : System.getenv("AWS_BUCKET_ITEMS").split(" ");
        Assume.assumeNotNull(awsAccessKey);
        Assume.assumeNotNull(awsSecretKey);
        Assume.assumeNotNull(awsBucket);
        Assume.assumeNotNull((Object) awsItems);
    }

    @Test
    public void testTest() {
        S3RestAuthenticationUrlFactory factory = new S3RestAuthenticationUrlFactory(awsAccessKey, awsSecretKey);
        for (String awsItem : awsItems) {
            String url = factory.create("GET", awsBucket, awsItem, 60 /* seconds */);
            Assert.assertNotNull(url);
            Assert.assertTrue(url.contains("AWSAccessKeyId="+awsAccessKey));
            Assert.assertTrue(url.contains("Expires="));
            Assert.assertTrue(url.contains("Signature="));
        }
    }

}
