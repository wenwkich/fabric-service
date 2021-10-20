package com.blkchainsolutions.fabric.client.impl.v1.commit;

import org.hyperledger.fabric.gateway.spi.CommitHandler;
import org.hyperledger.fabric.sdk.Channel;

/**
 * Functional interface describing a factory function for constructing {@link CommitHandler} instances.
 * <p>Default implementations can be obtained from {@link org.hyperledger.fabric.gateway.DefaultCommitHandlers}.</p>
 * @see <a href="https://github.com/hyperledger/fabric-gateway-java/blob/main/src/test/java/org/hyperledger/fabric/gateway/sample/SampleCommitHandlerFactory.java">SampleCommitHandlerFactory</a>
 * @see <a href="https://github.com/hyperledger/fabric-gateway-java/blob/main/src/test/java/org/hyperledger/fabric/gateway/sample/SampleCommitHandler.java">SampleCommitHandler</a>
 */
@FunctionalInterface
public interface CommitHandlerFactory {
    /**
     * Factory function to create a commit handler instance.
     * @param transactionId Transaction for which the instance is to handle commit events.
     * @param network Network on which the transaction is invoked.
     * @return A commit handler.
     */
    CommitHandler create(String transactionId, Channel channel);
}
