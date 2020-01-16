package com.andrewgilmartin.common.aws;

import com.andrewgilmartin.common.aws.S3RestAuthenticationUrlFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class RESTAuthenticationUrlFactoryTest {

    private static String awsAccessKey;
    private static String awsSecretKey;

    @BeforeClass
    public static void beforeClass() {
        awsAccessKey = System.getenv("AWS_ACCESS_KEY_ID");
        awsSecretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        Assume.assumeNotNull(awsAccessKey);
        Assume.assumeNotNull(awsSecretKey);
    }

    @Test
    public void testTest() {
        S3RestAuthenticationUrlFactory factory = new S3RestAuthenticationUrlFactory(awsAccessKey, awsSecretKey);

        //String url = factory.create("GET", "com.andrewgilmartin.scratch", "1-100.txt", 60 /* seconds */);
        String url = factory.create("GET", "com.andrewgilmartin.scratch", "☎.txt", 60 /* seconds */);
        assertEquals("", url);
    }

}
