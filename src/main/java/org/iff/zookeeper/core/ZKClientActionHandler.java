/*******************************************************************************
 * Copyright (c) 2019-01-25 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.zookeeper.core;

import org.iff.netty.server.ProcessContext;
import org.iff.netty.server.handlers.ActionHandler;
import org.iff.netty.server.handlers.BaseActionHandler;

/**
 * ZookeeperActionHandler
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-25
 * auto generate by qdp.
 */
public class ZKClientActionHandler extends BaseActionHandler {
    public static final String uriPrefix = "/zk/client";

    public boolean execute(ProcessContext ctx) {
        return false;
    }

    public ActionHandler create() {
        return new ZKClientActionHandler();
    }

    public boolean matchUri(String uri) {
        return uriPrefix.equals(uri) || (uri.startsWith(uriPrefix) && (uri.charAt(uriPrefix.length()) == '/' || uri.charAt(uriPrefix.length()) == '?'));
    }

}
