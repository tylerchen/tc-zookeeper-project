/*******************************************************************************
 * Copyright (c) 2019-01-26 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.zookeeper.core;

import org.apache.zookeeper.server.DatadirCleanupManager;
import org.apache.zookeeper.server.quorum.QuorumPeer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerMain;
import org.iff.infra.util.Logger;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * CustomQuorumPeerMain
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-26
 * auto generate by qdp.
 */
public class CustomQuorumPeerMain extends QuorumPeerMain implements Closeable {

    protected DatadirCleanupManager purgeMgr;
    protected Exception startupError = null;

    public static CustomQuorumPeerMain start(QuorumPeerConfig config) {
        CustomQuorumPeerMain main = new CustomQuorumPeerMain();
        new Thread(new Runnable() {
            public void run() {
                if (config.getServers().size() > 0) {
                    try {
                        // Start and schedule the the purge task
                        DatadirCleanupManager purgeMgr = new DatadirCleanupManager(config.getDataDir(), config.getDataLogDir(), config.getSnapRetainCount(), config.getPurgeInterval());
                        purgeMgr.start();
                        main.setPurgeMgr(purgeMgr);
                        main.runFromConfig(config);
                    } catch (Exception e) {
                        main.startupError = e;
                        Logger.error("CustomQuorumPeerMain Unexpected exception, exiting abnormally", e);
                    }
                }
            }
        });
        for (int i = 0; i < 50; i++) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                if (main.isRunning()) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        return main;
    }


    public QuorumPeer getCurrentQuorumPeer() {
        return quorumPeer;
    }

    public DatadirCleanupManager getPurgeMgr() {
        return purgeMgr;
    }

    public void setPurgeMgr(DatadirCleanupManager purgeMgr) {
        this.purgeMgr = purgeMgr;
    }

    public boolean isRunning() {
        return getCurrentQuorumPeer() != null && getCurrentQuorumPeer().isRunning();
    }

    public Exception getStartupError() {
        return startupError;
    }

    public void close() {
        try {
            getPurgeMgr().shutdown();
        } catch (Exception e) {
        }
        try {
            getCurrentQuorumPeer().shutdown();
        } catch (Exception e) {
        }
    }
}
