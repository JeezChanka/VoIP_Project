package pl.poznan.put.voip.server.services;

import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.server.Server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CallService {
    private List<Call> activeCalls = new LinkedList<Call>();

    public CallService() {

    }

    public Session startCall(String username, int port) {
        Map<String, Session> uos = Server.getServer().getUserService().getOnlineUsers();

        Session s1 = Server.getServer().currentSession();
        Session s2 = uos.get(username);
        if (s2 == null)  return null;

        for(Call call:activeCalls) {
            if(call.isAssociated(s1) || call.isAssociated(s2))
                return null;

        }

        Call newCall = new Call(s1, port, s2, -1);
        activeCalls.add(newCall);

        return s2;
    }

    public Session acceptCall(int port) {
        Session s = Server.getServer().currentSession();

        for (Call call: activeCalls) {
            if (!call.isAssociated(s)) continue;

            TargetData t1 = call.getSource(s);
            TargetData t2 = call.getTarget(s);

            t1.setPort(port);

            return t2.getS();
        }

        return null;
    }

    public Session declineCall() {
        Session s1 = Server.getServer().currentSession();

        Iterator<Call> it = activeCalls.iterator();
        while (it.hasNext()) {
            Call call = it.next();

            TargetData t1 = call.getTarget(s1);
            if (t1 == null) continue;

            it.remove();
            Session s2 = t1.getS();

            return s2;
        }
        return null;
    }

    public void forwardPacket(DatagramPacket packet) {
        InetAddress sourceIP = packet.getAddress();
        int sourcePort = packet.getPort();
        byte[] sourceData = packet.getData();
        int dataLen = sourceData.length;

        for(Call call:activeCalls) {
            TargetData target = call.getTarget(sourceIP, sourcePort);
            if(target == null) continue;

            if(target.getPort()==-1) return;

            DatagramPacket forpacket = new DatagramPacket(sourceData, dataLen, target.getIP(), target.getPort());

            Server.getServer().getCallWrapper().sendUdpData(forpacket);
            return;
        }
    }

    private class Call {
        private TargetData t1;
        private TargetData t2;

        public Call(Session s1,  int port1, Session s2,  int port2) {
            this.t1 = new TargetData(s1, port1);
            this.t2 = new TargetData(s2, port2);
        }

        public boolean isAssociated(Session session) {
            return session == t1.getS() || session == t2.getS();
        }

        public TargetData getTarget(InetAddress ip,  int port1) {
            if (t1.represents(ip, port1)) return t2;
            if (t2.represents(ip, port1)) return t1;
            return null;
        }

        public TargetData getTarget(Session session) {
            if (t1.getS() == session) return t2;
            if (t2.getS() == session) return t1;
            return null;
        }

        public TargetData getSource(Session session) {
            if (t1.getS() == session) return t1;
            if (t2.getS() == session) return t2;
            return null;
        }
    }

    private class TargetData {
        private Session s;
        private int port;

        private TargetData(Session s, int port) {
            this.s = s;
            this.port = port;
        }

        public Session getS() {
            return s;
        }

        public void setS(Session s) {
            this.s = s;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public InetAddress getIP() {
            return s.getSocket().getSocket().getInetAddress();
        }

        public boolean represents(InetAddress ip,  int port1) {
            return s.getSocket().getSocket().getInetAddress().equals(ip) && port == port1;
        }
    }
}
