/*******************************************************************************
 * Copyright (c) 2019-01-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.zookeeper.core.service;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.iff.infra.util.Exceptions;
import org.iff.infra.util.Logger;
import org.iff.infra.util.NumberHelper;
import org.iff.infra.util.PreRequiredHelper;
import org.iff.zookeeper.util.ByteHelper;
import org.iff.zookeeper.util.Tuple;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ZookeeperServerService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-28
 * auto generate by qdp.
 */
public class ZookeeperClientService {

    public static final Map<String, ZooKeeper> map = new HashMap<>();

    public static final Map<String, List<Tuple.Two>> history = new HashMap<>();

    public static boolean connect(String hostPort) {
        String id = PreRequiredHelper.requireNotBlank(hostPort, "ZookeeperClientService zookeeper server host:port is required!");
        try {
            ZooKeeper zk = null;
            if (map.containsKey(id)) {
                zk = map.get(id);
                if (!zk.getState().isAlive()) {
                    close(id);
                    zk = null;
                }
            }
            if (zk != null) {
                return zk == null ? false : true;
            }
            /** 信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 */
            CountDownLatch connectedSemaphore = new CountDownLatch(1);
            zk = new ZooKeeper(hostPort/** zookeeper地址 */, 2000/** session超时时间 */, new Watcher() {
                public void process(WatchedEvent event) {
                    //获取事件的状态
                    Event.KeeperState keeperState = event.getState();
                    Event.EventType eventType = event.getType();
                    //如果是建立连接
                    if (Event.KeeperState.SyncConnected != keeperState) {
                        return;
                    }
                    if (Event.EventType.None == eventType) {
                        //如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
                        connectedSemaphore.countDown();
                        System.out.println("connected.");
                    }
                }
            });
            //进行阻塞
            connectedSemaphore.await();
            map.put(id, zk);
            return zk == null ? false : true;
        } catch (Exception e) {
            Exceptions.runtime("ZookeeperClientService connect zookeeper fail!", e);
        }
        return false;
    }

    public static void close(String id) {
        if (map.containsKey(id)) {
            try {
                map.remove(id).close();
                history.remove(id);
            } catch (Exception e) {
            }
        }
    }

    public static ZooKeeper get(String id) {
        ZooKeeper zk = null;
        if (map.containsKey(id)) {
            zk = map.get(id);
            if (!(zk.getState().isConnected() && zk.getState().isAlive())) {
                map.remove(id);
                zk = null;
            }
        }
        return zk;
    }

    public static Tuple.Two<String/*result*/, String/*cmd*/, String[]/*args*/> processZKCmdLine(String id, String commandLine) {
        Tuple.Two<String/*result*/, String/*cmd*/, String[]/*args*/> command = new Tuple.Two<>();
        try {
            String[] split = StringUtils.split(commandLine, ' ');
            command.setValueStartsFromOne(1, split[0]).setValueStartsFromOne(2, ArrayUtils.subarray(split, 1, split.length));
            processZKCmd(id, command);
            return command;
        } catch (Exception e) {
            command.result(e);
        }
        return command;
    }

    public static boolean processZKCmd(String id, Tuple.Two<String/*result*/, String/*cmd*/, String[]/*args*/> command) throws Exception {
        ZooKeeper zk = get(id);
        if (zk == null) {
            command.result("Can't not connect server: " + id);
            return false;
        }
        List<Tuple.Two> histCmds = history.get(id);
        {
            if (histCmds == null) {
                histCmds = new ArrayList<>();
                history.put(id, histCmds);
            }
            histCmds.add(command);
        }
        Stat stat = new Stat();
        String cmd = command.first();
        String[] args = command.second();
        String path = getArg(args, 0);
        boolean watch = getArg(args, 1) != null;
        List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        List<String> result = new ArrayList<>();
        Logger.debug("Processing " + cmd);
        if (cmd.equals("quit")) {
            close(id);
            result.add("OK");
        } else if (cmd.equals("redo") && args.length >= 1) {
            Integer i = Integer.decode(args[0]);
            List<Tuple.Two> twos = histCmds;
            int commandCount = twos == null ? -1 : twos.size();
            if (commandCount <= i || i < 0) { // don't allow redoing this redo
                result.add("Command index out of range");
            } else {
                return processZKCmd(id, (Tuple.Two<String, String, String[]>) twos.get(i).clone());
            }
        } else if (cmd.equals("history")) {
            List<Tuple.Two> twos = histCmds;
            if (twos != null && !twos.isEmpty()) {
                int len = twos.size();
                for (int i = len - 15; i < len; i++) {
                    if (i < 0) {
                        continue;
                    }
                    result.add(i + " - " + twos.get(i).first() + " " + StringUtils.join(twos.get(i).second(), " "));
                }
            } else {
                result.add("no history.");
            }
        } else if (cmd.equals("printwatches")) {
            //printWatches = args[1].equals("on");
            result.add("printwatches always off.");
        } else if (cmd.equals("connect")) {
            if (args.length >= 1) {
                connect(args[0]);
            } else {
                get(id);
            }
        }
        // Below commands all need a live connection
        if (zk == null || !zk.getState().isAlive()) {
            result.add("Not connected");
            return false;
        } else {
            result.add("OK");
        }

        if (cmd.equals("create") && args.length >= 2) {
            //create [-s] [-e] path data acl
            String seq = null, eph = null, data = null, aclStr = null;
            for (int i = 0, j = 1; i < args.length; i++) {
                if ("-s".equals(args[i])) {
                    seq = args[i];
                } else if ("-e".equals(args[i])) {
                    eph = args[i];
                } else {
                    j = j + 1;
                }
                path = j == 2 ? args[i] : path;
                data = j == 3 ? args[i] : data;
                aclStr = j == 4 ? args[i] : aclStr;
            }
            CreateMode flags = CreateMode.PERSISTENT;
            flags = seq != null ? CreateMode.EPHEMERAL_SEQUENTIAL : (eph != null ? CreateMode.EPHEMERAL : flags);
            if (aclStr != null) {
                acl = parseACLs(aclStr);
            }
            String newPath = zk.create(path, ByteHelper.utf8(data), acl, flags);
            result.add("Created " + newPath);
        } else if (cmd.equals("delete") && args.length >= 1) {
            //delete path [version]
            zk.delete(path, watch ? NumberHelper.getInt(getArg(args, 1), -1) : -1);
            result.add("OK");
        } else if (cmd.equals("rmr") && args.length >= 1) {
            //rmr path
            ZKUtil.deleteRecursive(zk, path);
            result.add("OK");
        } else if (cmd.equals("set") && args.length >= 2) {
            //set path data [version]
            String data = getArg(args, 1), version = getArg(args, 2);
            stat = zk.setData(path, ByteHelper.utf8(data), NumberHelper.getInt(version, -1));
            result.add(printStat(stat));
        } else if (cmd.equals("aget") && args.length >= 1) {
            //aget path
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zk.getData(path, watch, new AsyncCallback.DataCallback() {
                public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                    result.add("rc = " + rc + " path = " + path + " data = " + (data == null ? "null" : new String(data)) + " stat = \n");
                    result.add(printStat(stat));
                    countDownLatch.countDown();
                }
            }, path);
            countDownLatch.await(30, TimeUnit.SECONDS);
            if (result.size() < 1) {
                result.add("executing aget...");
            }
        } else if (cmd.equals("get") && args.length >= 1) {
            //get path [watch]
            byte data[] = zk.getData(path, watch, stat);
            result.add(ByteHelper.utf8(data == null ? "null".getBytes() : data) + "\n");
            result.add(printStat(stat));
        } else if (cmd.equals("ls") && args.length >= 1) {
            //ls path [watch]
            List<String> children = zk.getChildren(path, watch);
            result.add(StringUtils.join(children, " "));
        } else if (cmd.equals("ls2") && args.length >= 1) {
            //ls2 path [watch]
            List<String> children = zk.getChildren(path, watch, stat);
            result.add(StringUtils.join(children, " "));
            result.add(printStat(stat));
        } else if (cmd.equals("getAcl") && args.length >= 1) {
            //getAcl path
            acl = zk.getACL(path, stat);
            for (ACL a : acl) {
                result.add(a.getId() + ": " + getPermString(a.getPerms()));
            }
        } else if (cmd.equals("setAcl") && args.length >= 2) {
            //setAcl path acl [aclVersion]
            String aclStr = getArg(args, 1), aclVersion = getArg(args, 2);
            stat = zk.setACL(path, parseACLs(aclStr), NumberHelper.getInt(aclVersion, -1));
            result.add(printStat(stat));
        } else if (cmd.equals("stat") && args.length >= 1) {
            //stat path [watch]
            stat = zk.exists(path, watch);
            if (stat == null) {
                result.add("KeeperException.NoNodeException: " + path);
            } else {
                result.add(printStat(stat));
            }
        } else if (cmd.equals("listquota") && args.length >= 1) {
            //listquota path
            String absolutePath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode;
            byte[] data = null;
            try {
                result.add("absolute path is " + absolutePath);
                data = zk.getData(absolutePath, false, stat);
                StatsTrack st = new StatsTrack(new String(data));
                result.add("Output quota for " + path + " " + st.toString());
                data = zk.getData(Quotas.quotaZookeeper + path + "/" + Quotas.statNode, false, stat);
                result.add("Output stat for " + path + " " + new StatsTrack(new String(data)).toString());
            } catch (KeeperException.NoNodeException ne) {
                result.add("quota for " + path + " does not exist.");
            }
        } else if (cmd.equals("setquota") && args.length >= 3) {
            //setquota -n|-b val path
            String option = getArg(args, 0), val = getArg(args, 1);
            path = getArg(args, 2);
            result.add("Comment: the parts are option " + option + " val " + val + " path " + path);
            if ("-b".equals(option)) {
                // we are setting the bytes quota
                createQuota(zk, path, Long.parseLong(val), -1);
                result.add("OK");
            } else if ("-n".equals(option)) {
                // we are setting the num quota
                createQuota(zk, path, -1L, Integer.parseInt(val));
                result.add("OK");
            } else {
                result.add("Usage: setquota -n|-b val path.");
            }
        } else if (cmd.equals("delquota") && args.length >= 2) {
            //delquota [-n|-b] path
            //if neither option -n or -b is specified, we delete the quota node for thsi node.
            if (args.length == 2) {
                //this time we have an option
                String option = getArg(args, 0);
                path = getArg(args, 1);
                if ("-b".equals(option)) {
                    delQuota(zk, path, true, false);
                } else if ("-n".equals(option)) {
                    delQuota(zk, path, false, true);
                }
                result.add("OK");
            } else if (args.length == 1) {
                // we dont have an option specified.
                // just delete whole quota node
                delQuota(zk, path, true, true);
                result.add("OK");
            } else if (cmd.equals("help")) {
                result.add("Usage: delquota [-n|-b] path.");
                result.add("OK");
            }
        } else if (cmd.equals("close")) {
            close(id);
            result.add("OK");
        } else if (cmd.equals("sync") && args.length >= 2) {
            //sync path
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zk.sync(path, new AsyncCallback.VoidCallback() {
                public void processResult(int rc, String path, Object ctx) {
                    result.add("Sync returned " + rc);
                    countDownLatch.countDown();
                }
            }, null);
            countDownLatch.await(30, TimeUnit.SECONDS);
            if (result.size() < 1) {
                command.result("Sync...");
            }
        } else if (cmd.equals("addauth") && args.length >= 2) {
            //addauth scheme auth
            String scheme = getArg(args, 0), auth = getArg(args, 1);
            byte[] b = auth == null ? null : auth.getBytes();
            zk.addAuthInfo(scheme, b);
            result.add("OK");
        } else {
            result.add("connect host:port\n" +
                    "close\n" +
                    "create [-s] [-e] path data acl\n" +
                    "delete path [version]\n" +
                    "rmr path\n" +
                    "set path data [version]\n" +
                    "get path [watch]\n" +
                    "ls path [watch]\n" +
                    "ls2 path [watch]\n" +
                    "getAcl path\n" +
                    "setAcl path acl\n" +
                    "stat path [watch]\n" +
                    "sync path\n" +
                    "setquota -n|-b val path\n" +
                    "listquota path\n" +
                    "delquota [-n|-b] path\n" +
                    "history\n" +
                    "redo cmdno\n" +
                    "printwatches on|off\n" +
                    "quit\n" +
                    "addauth scheme auth");
        }
        result.add("");
        command.result(StringUtils.join(result, '\n'));
        return watch;
    }

    /**
     * create [-s] [-e] path data acl
     * 其中”-s”表示创建一个顺序自动编号的节点,”-e”表示创建一个临时节点.默认为持久性节点
     */
    public static void create(String id) {
        ZooKeeper zk = null;
    }

    public static List<String> servers() {
        return new ArrayList<>(map.keySet());
    }

    private static String getArg(String[] args, int i) {
        return args == null || args.length <= i ? null : args[i];
    }

    private static List<ACL> parseACLs(String aclString) {
        List<ACL> acl;
        String acls[] = aclString.split(",");
        acl = new ArrayList<ACL>();
        for (String a : acls) {
            int firstColon = a.indexOf(':');
            int lastColon = a.lastIndexOf(':');
            if (firstColon == -1 || lastColon == -1 || firstColon == lastColon) {
                System.err.println(a + " does not have the form scheme:id:perm");
                continue;
            }
            ACL newAcl = new ACL();
            newAcl.setId(new Id(a.substring(0, firstColon), a.substring(firstColon + 1, lastColon)));
            newAcl.setPerms(getPermFromString(a.substring(lastColon + 1)));
            acl.add(newAcl);
        }
        return acl;
    }

    private static int getPermFromString(String permString) {
        int perm = 0;
        for (int i = 0; i < permString.length(); i++) {
            switch (permString.charAt(i)) {
                case 'r':
                    perm |= ZooDefs.Perms.READ;
                    break;
                case 'w':
                    perm |= ZooDefs.Perms.WRITE;
                    break;
                case 'c':
                    perm |= ZooDefs.Perms.CREATE;
                    break;
                case 'd':
                    perm |= ZooDefs.Perms.DELETE;
                    break;
                case 'a':
                    perm |= ZooDefs.Perms.ADMIN;
                    break;
                default:
                    ;
            }
        }
        return perm;
    }

    private static String getPermString(int perms) {
        StringBuilder p = new StringBuilder();
        if ((perms & ZooDefs.Perms.CREATE) != 0) {
            p.append('c');
        }
        if ((perms & ZooDefs.Perms.DELETE) != 0) {
            p.append('d');
        }
        if ((perms & ZooDefs.Perms.READ) != 0) {
            p.append('r');
        }
        if ((perms & ZooDefs.Perms.WRITE) != 0) {
            p.append('w');
        }
        if ((perms & ZooDefs.Perms.ADMIN) != 0) {
            p.append('a');
        }
        return p.toString();
    }

    private static String printStat(Stat stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("cZxid = 0x" + Long.toHexString(stat.getCzxid())).append('\n');
        sb.append("ctime = " + new Date(stat.getCtime()).toString()).append('\n');
        sb.append("mZxid = 0x" + Long.toHexString(stat.getMzxid())).append('\n');
        sb.append("mtime = " + new Date(stat.getMtime()).toString()).append('\n');
        sb.append("pZxid = 0x" + Long.toHexString(stat.getPzxid())).append('\n');
        sb.append("cversion = " + stat.getCversion()).append('\n');
        sb.append("dataVersion = " + stat.getVersion()).append('\n');
        sb.append("aclVersion = " + stat.getAversion()).append('\n');
        sb.append("ephemeralOwner = 0x" + Long.toHexString(stat.getEphemeralOwner())).append('\n');
        sb.append("dataLength = " + stat.getDataLength()).append('\n');
        sb.append("numChildren = " + stat.getNumChildren()).append('\n');
        return sb.toString();
    }

    /**
     * this method creates a quota node for the path
     *
     * @param zk       the ZooKeeper client
     * @param path     the path for which quota needs to be created
     * @param bytes    the limit of bytes on this path
     * @param numNodes the limit of number of nodes on this path
     * @return true if its successful and false if not.
     */
    public static boolean createQuota(ZooKeeper zk, String path, long bytes, int numNodes) throws Exception {
        // check if the path exists. We cannot create quota for a path that already exists in zookeeper for now.
        Stat initStat = zk.exists(path, false);
        if (initStat == null) {
            throw new IllegalArgumentException(path + " does not exist.");
        }
        // now check if their is already existing parent or child that has quota.
        String quotaPath = Quotas.quotaZookeeper;
        // check for more than 2 children --
        // if zookeeper_stats and zookeeper_qutoas are not the children then this path is an ancestor of some path that already has quota
        String realPath = Quotas.quotaZookeeper + path;
        try {
            List<String> children = zk.getChildren(realPath, false);
            for (String child : children) {
                if (!child.startsWith("zookeeper_")) {
                    throw new IllegalArgumentException(path + " has child " + child + " which has a quota");
                }
            }
        } catch (KeeperException.NoNodeException ne) {
            // this is fine
        }
        //check for any parent that has been quota
        checkIfParentQuota(zk, path);
        // this is valid node for quota start creating all the parents
        if (zk.exists(quotaPath, false) == null) {
            try {
                zk.create(Quotas.procZookeeper, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zk.create(Quotas.quotaZookeeper, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (KeeperException.NodeExistsException ne) {
                // do nothing
            }
        }
        // now create the direct children and the stat and quota nodes
        String[] splits = path.split("/");
        StringBuilder sb = new StringBuilder();
        sb.append(quotaPath);
        for (int i = 1; i < splits.length; i++) {
            sb.append("/" + splits[i]);
            quotaPath = sb.toString();
            try {
                zk.create(quotaPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (KeeperException.NodeExistsException ne) {
                //do nothing
            }
        }
        String statPath = quotaPath + "/" + Quotas.statNode;
        quotaPath = quotaPath + "/" + Quotas.limitNode;
        StatsTrack strack = new StatsTrack(null);
        strack.setBytes(bytes);
        strack.setCount(numNodes);
        try {
            zk.create(quotaPath, strack.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            StatsTrack stats = new StatsTrack(null);
            stats.setBytes(0L);
            stats.setCount(0);
            zk.create(statPath, stats.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException.NodeExistsException ne) {
            byte[] data = zk.getData(quotaPath, false, new Stat());
            StatsTrack strackC = new StatsTrack(new String(data));
            if (bytes != -1L) {
                strackC.setBytes(bytes);
            }
            if (numNodes != -1) {
                strackC.setCount(numNodes);
            }
            zk.setData(quotaPath, strackC.toString().getBytes(), -1);
        }
        return true;
    }

    private static void checkIfParentQuota(ZooKeeper zk, String path) throws Exception {
        final String[] splits = path.split("/");
        String quotaPath = Quotas.quotaZookeeper;
        for (String str : splits) {
            if (str.length() == 0) {
                // this should only be for the beginning of the path
                // i.e. "/..." - split(path)[0] is empty string before first '/'
                continue;
            }
            quotaPath += "/" + str;
            List<String> children = null;
            try {
                children = zk.getChildren(quotaPath, false);
            } catch (KeeperException.NoNodeException ne) {
                Logger.debug("child removed during quota check", ne);
                return;
            }
            if (children.size() == 0) {
                return;
            }
            for (String child : children) {
                if (Quotas.limitNode.equals(child)) {
                    throw new IllegalArgumentException(path + " has a parent " + quotaPath + " which has a quota");
                }
            }
        }
    }

    /**
     * this method deletes quota for a node.
     *
     * @param zk       the zookeeper client
     * @param path     the path to delete quota for
     * @param bytes    true if number of bytes needs to
     *                 be unset
     * @param numNodes true if number of nodes needs
     *                 to be unset
     * @return true if quota deletion is successful
     * @throws KeeperException
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean delQuota(ZooKeeper zk, String path, boolean bytes, boolean numNodes) throws Exception {
        String parentPath = Quotas.quotaZookeeper + path;
        String quotaPath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode;
        if (zk.exists(quotaPath, false) == null) {
            System.out.println("Quota does not exist for " + path);
            return true;
        }
        byte[] data = null;
        try {
            data = zk.getData(quotaPath, false, new Stat());
        } catch (KeeperException.NoNodeException ne) {
            System.err.println("quota does not exist for " + path);
            return true;
        }
        StatsTrack strack = new StatsTrack(new String(data));
        if (bytes && !numNodes) {
            strack.setBytes(-1L);
            zk.setData(quotaPath, strack.toString().getBytes(), -1);
        } else if (!bytes && numNodes) {
            strack.setCount(-1);
            zk.setData(quotaPath, strack.toString().getBytes(), -1);
        } else if (bytes && numNodes) {
            // delete till you can find a node with more than
            // one child
            List<String> children = zk.getChildren(parentPath, false);
            /// delete the direct children first
            for (String child : children) {
                zk.delete(parentPath + "/" + child, -1);
            }
            // cut the tree till their is more than one child
            trimProcQuotas(zk, parentPath);
        }
        return true;
    }

    /**
     * trim the quota tree to recover unwanted tree elements
     * in the quota's tree
     *
     * @param zk   the zookeeper client
     * @param path the path to start from and go up and see if their
     *             is any unwanted parent in the path.
     * @return true if sucessful
     * @throws KeeperException
     * @throws IOException
     * @throws InterruptedException
     */
    private static boolean trimProcQuotas(ZooKeeper zk, String path) throws Exception {
        if (Quotas.quotaZookeeper.equals(path)) {
            return true;
        }
        List<String> children = zk.getChildren(path, false);
        if (children.size() == 0) {
            zk.delete(path, -1);
            String parent = path.substring(0, path.lastIndexOf('/'));
            return trimProcQuotas(zk, parent);
        } else {
            return true;
        }
    }
}
