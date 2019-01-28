/*******************************************************************************
 * Copyright (c) 2019-01-25 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.zookeeper.core;

import io.netty.handler.codec.http.HttpResponseStatus;
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
public class ZookeeperActionHandler extends BaseActionHandler {
    public static final String uriPrefix = "/zk";
    public static final ZKServerActionHandler server = new ZKServerActionHandler();
    public static final ZKClientActionHandler client = new ZKClientActionHandler();

    public boolean execute(ProcessContext ctx) {
        String uri = ctx.getUri();
        if (server.matchUri(uri)) {
            return server.create().process(ctx).done();
        }
        if (client.matchUri(uri)) {
            return client.create().process(ctx).done();
        }
        ctx.getResponse().setStatus(HttpResponseStatus.NOT_FOUND);
        ctx.outputHtml();
        return true;
    }

    public ActionHandler create() {
        return new ZookeeperActionHandler();
    }

    public boolean matchUri(String uri) {
        return uriPrefix.equals(uri) || (uri.startsWith(uriPrefix) && (uri.charAt(uriPrefix.length()) == '/' || uri.charAt(uriPrefix.length()) == '?'));
    }

}
