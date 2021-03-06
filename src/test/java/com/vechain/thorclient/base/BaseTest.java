package com.vechain.thorclient.base;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vechain.thorclient.core.model.blockchain.NodeProvider;
import com.vechain.thorclient.core.model.clients.Address;
import com.vechain.thorclient.utils.Prefix;
import com.vechain.thorclient.utils.StringUtils;
import com.vechain.thorclient.utils.crypto.ECKeyPair;

public abstract class BaseTest implements SlatKeys {

    protected Logger            logger      = LoggerFactory.getLogger(this.getClass());

    protected String            privateKey;
    protected String            sponsorKey;
    protected String            nodeProviderUrl;
    protected String            fromAddress;
    private Map<String, String> environment = new HashMap<String, String>();

    @Before
    public void setProvider() {
        privateKey = System.getenv(PRIVATE_KEY);
        sponsorKey = System.getenv(SPONSOR_KEY);
        nodeProviderUrl = System.getenv(NODE_PROVIDER_URL);
        String nodeTimeout = System.getenv(NODE_TIMEOUT);

        Properties properties = new Properties();
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        int timeout = 5000;
        try {
            properties.load(is);
        } catch (Exception e) {
            logger.error("Can not find the file config.properties in classpath~");
        } finally {
            if (StringUtils.isBlank(nodeTimeout)) {
                nodeTimeout = properties.getProperty(NODE_TIMEOUT);
            }
            try {
                if (!StringUtils.isBlank(nodeTimeout)) {
                    timeout = Integer.parseInt(nodeTimeout);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            environment.put(NODE_TIMEOUT, String.valueOf(timeout));
            if (StringUtils.isBlank(privateKey)) {
                privateKey = properties.getProperty(PRIVATE_KEY);
            }
            if (StringUtils.isBlank( sponsorKey )){
                sponsorKey = properties.getProperty( SPONSOR_KEY );
            }
            environment.put(PRIVATE_KEY, privateKey);
            if (StringUtils.isBlank(nodeProviderUrl)) {
                nodeProviderUrl = properties.getProperty(NODE_PROVIDER_URL);
            }
            environment.put(NODE_PROVIDER_URL, nodeProviderUrl);
        }
        if (StringUtils.isBlank(this.nodeProviderUrl) || !this.nodeProviderUrl.startsWith("http")) {
            throw new RuntimeException("Can not find valid nodeProviderUrl~");
        }
        environment.put(VTHO_TOKEN_ADDRESS, Address.VTHO_Address.toHexString(Prefix.VeChainX));

        NodeProvider nodeProvider = NodeProvider.getNodeProvider();
        nodeProvider.setProvider(this.nodeProviderUrl);
        nodeProvider.setTimeout(timeout);

        this.recoverAddress();
    }

    protected Map<String, String> getEnvironment() {
        return this.environment;
    }

    protected void recoverAddress() {
        if (!StringUtils.isBlank(privateKey)) {
            ECKeyPair keyPair = ECKeyPair.create(privateKey);
            fromAddress = keyPair.getHexAddress();
            environment.put(FROM_ADDRESS, fromAddress);
        }
    }

}
