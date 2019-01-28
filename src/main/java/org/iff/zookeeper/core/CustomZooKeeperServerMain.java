/*******************************************************************************
 * Copyright (c) 2019-01-25 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform.
 ******************************************************************************/
package org.iff.zookeeper.core;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.iff.infra.util.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class starts and runs a standalone ZooKeeperServer.
 */
public class CustomZooKeeperServerMain extends ZooKeeperServerMain implements Closeable {

    private static final String USAGE = "Usage: CustomZooKeeperServerMain configfile | port datadir [ticktime] [maxcnxns]";

    protected Exception startupError = null;

    public static CustomZooKeeperServerMain start(String[] args) {
        CustomZooKeeperServerMain main = new CustomZooKeeperServerMain();
        new Thread(new Runnable() {
            public void run() {
                try {
                    main.initializeAndRun(args);
                } catch (IllegalArgumentException e) {
                    main.startupError = e;
                    Logger.info(USAGE);
                    Logger.error("CustomZooKeeperServerMain Invalid arguments, exiting abnormally", e);
                } catch (QuorumPeerConfig.ConfigException e) {
                    main.startupError = e;
                    Logger.error("CustomZooKeeperServerMain Invalid config, exiting abnormally", e);
                } catch (Exception e) {
                    main.startupError = e;
                    Logger.error("CustomZooKeeperServerMain Unexpected exception, exiting abnormally", e);
                }
            }
        }).start();
        for (int i = 0; i < 30; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                if (main.startupError != null) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        return main;
    }

    public void initializeAndRun(String[] args) throws QuorumPeerConfig.ConfigException, IOException {
        super.initializeAndRun(args);
    }

    public void runFromConfig(ServerConfig config) throws IOException {
        super.runFromConfig(config);
    }

    public Exception getStartupError() {
        return startupError;
    }

    /**
     * Shutdown the serving instance
     */
    public void shutdown() {
        super.shutdown();
    }

    public void close() {
        shutdown();
    }
}
