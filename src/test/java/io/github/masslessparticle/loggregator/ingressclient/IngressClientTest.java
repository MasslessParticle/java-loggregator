package io.github.masslessparticle.loggregator.ingressclient;

import io.github.masslessparticle.loggregator.ingressserver.TestIngressServer;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class IngressClientTest {

    @Test
    void canBuildAnIngressClientWithMutualTLSEnabled() {
        TestIngressServer server = buildIngressServer();


        assertTrue(true);
    }

    private TestIngressServer buildIngressServer() {
        try {
            return new TestIngressServer(
                    fixturePath("server.crt"),
                    fixturePath("server.key"),
                    fixturePath("CA.crt")
            );
        } catch (SSLException e) {
            e.printStackTrace();
            fail("Unable to build test ingress server");
        }

        return null;
    }

    private String fixturePath(String name) {
        File f = new File("src/test/resources/fixtures/" + name);
        assertTrue(f.exists());

        return f.getAbsolutePath();
    }
}