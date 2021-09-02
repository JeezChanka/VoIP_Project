package pl.poznan.put.voip.server.services;

import pl.poznan.put.voip.core.session.Session;
import pl.poznan.put.voip.server.Server;

import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CallService {
    private List<Call> activeCalls = new LinkedList<Call>();

    public CallService() {

    }

    public boolean startCall(String username1, String username2) {
        Map<String, Session> uos = Server.getServer().getUserService().getOnlineUsers();

        Session s1 = uos.get(username1);
        Session s2 = uos.get(username2);
        if (s2 == null)  return false;

        Socket soc1 = s1.getSocket().getSocket();
        Socket soc2 = s2.getSocket().getSocket();

        InetAddress senderIP = soc1.getInetAddress();
        InetAddress receiverIP = soc2.getInetAddress();

        for(Call call:activeCalls) {
            if(call.isAssociated(senderIP) || call.isAssociated(receiverIP))
                return false;

        }

        Call newCall = new Call(senderIP, receiverIP, soc1.getPort());
        activeCalls.add(newCall);

        return true;
    }


    //TODO potrzeba sesji do zwracania sesji adresata, na podstawie CallSocketWrapper zwracać CallSocketWrapper adresata
    private static class Call {
        private InetAddress ip1;
        private InetAddress ip2;

        private int port1;
        private int port2 = -1;

        public Call(InetAddress ip1, InetAddress ip2, int port1) {
            this.ip1 = ip1;
            this.ip2 = ip2;
            this.port1 = port1;
        }

        public boolean isAssociated(InetAddress ip) {
            return ip.equals(this.ip1) || ip.equals(this.ip2);
        }
    }
}
