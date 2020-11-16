

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;
    String routingPath;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        this.routingPath = "";
        NetworkLayerServer.clientCount++;
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        networkUtility.write(NetworkLayerServer.endDevices);
        new Thread(this).start();
    }

    @Override
    public void run() {
        /**
         * Synchronize actions with client.
         */
        
        /*
        Tasks:
        1. Upon receiving a packet and recipient, call deliverPacket(packet)
        2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        3. Either send acknowledgement with number of hops or send failure message back to client
        */
        for(int i=0;i<10;i++){
            System.out.println("Packet No:"+ (i+1));
            Packet packet = (Packet) networkUtility.read();
            if(packet!=null){
                EndDevice source_end_device = NetworkLayerServer.endDeviceMap.get(packet.getSourceIP());
                boolean delivered = deliverPacket(packet);
                System.out.println();
                if(packet.getSpecialMessage().equalsIgnoreCase("SHOW_ROUTE"))
                    networkUtility.write(routingPath);
                if(delivered)
                    networkUtility.write("SUCCESS hop count:"+ packet.hopcount);
                else networkUtility.write("FAILURE");
            }
            else{
                System.out.println("Packet not received for Connection problem");
                networkUtility.write("Packet not received for Connection problem");
            }

        }


    }


    public Boolean deliverPacket(Packet p) {


        p.hopcount++;
        EndDevice dest_end_device = NetworkLayerServer.endDeviceMap.get(p.getDestinationIP());
        Short bytes[] = endDevice.getIpAddress().getBytes();
        IPAddress source_interface = new IPAddress(bytes[0] + "." + bytes[1] + "." + bytes[2] + "." + "1");
        //System.out.println("Deliver From Source: "+endDevice.getIpAddress());
        //System.out.println("Source interface: "+source_interface);

//       for(Map.Entry<IPAddress,Integer> entry : NetworkLayerServer.interfacetoRouterID.entrySet()){
//           System.out.println(entry.getKey()+"..."+entry.getValue()+"..."+NetworkLayerServer.interfacetoRouterID.get(entry.getKey()));
//       }

        Router s = NetworkLayerServer.routerMap.get(NetworkLayerServer.interfacetoRouterID.get(source_interface));
        System.out.println("Source Router id:"+s.getRouterId());
        //System.out.println(p.getSourceIP());
//        for(int i=0;i<s.getInterfaceAddresses().size();i++){
//            System.out.println(i+"..."+ s.getInterfaceAddresses().get(i)+"..."+NetworkLayerServer.interfacetoRouterID.get(s.getInterfaceAddresses().get(i)));
//        }


        Short bytes2[] = p.getDestinationIP().getBytes();
        IPAddress dest_interface = new IPAddress(bytes2[0] + "." + bytes2[1] + "." + bytes2[2] + "." + "1");
        Router d = NetworkLayerServer.routerMap.get(NetworkLayerServer.interfacetoRouterID.get(dest_interface));
        System.out.println("Dest Router ID:"+d.getRouterId());
        System.out.print("PATH:"+s.getRouterId()+"->");

        //routingPath+= s.strRoutingTable();
        Router x = null;
        //s.printRoutingTable();
        if(!s.getState()){
            //System.out.println(s.getRouterId()+" is down...");
            return false;
        }
        else{
            if(s.getRouterId()==d.getRouterId()) return true;
            while(true){
                //System.out.println("size:"+s.getRoutingTable().size());
                for(int i=0; i<s.getRoutingTable().size(); i++){
                    RoutingTableEntry entry = s.getRoutingTable().get(i);
                    int destRouterID = entry.getRouterId();
                    int gatewayRouterId = entry.getGatewayRouterId();
                    //System.out.println(destRouterID+"..."+d.getRouterId());
                    if(destRouterID == d.getRouterId()) {
                        //System.out.println("hello");
                        x = NetworkLayerServer.routerMap.get(gatewayRouterId);

                        if(x== null) return false;
                        else{
                            //System.out.println("Gateway of "+s.getRouterId()+": "+x.getRouterId()+" state: "+x.getState());
                            //System.out.println("dhukse........");
                            if(x.getState()==false){
                                //System.out.println("State down");
                                entry.setDistance(Constants.INFINITY);
                                s.getRoutingTable().set(i,entry);
                                RouterStateChanger.islocked = true;
                                //NetworkLayerServer.simpleDVR(s.getRouterId());
                                NetworkLayerServer.DVR(s.getRouterId());
                                RouterStateChanger.islocked = false;
                                //RouterStateChanger.msg.notify();
                                return false;
                            }
                            int j=0;
                            while(j< x.getRoutingTable().size()){
                                RoutingTableEntry y_entry = x.getRoutingTable().get(i);
                                int y_id = y_entry.getRouterId();
                                if(y_id == s.getRouterId()){
                                    Router y = NetworkLayerServer.routerMap.get(y_id);
                                    if(y_entry.getDistance() == Constants.INFINITY){
                                        y_entry.setDistance(1);
                                        x.getRoutingTable().set(j,y_entry);
                                        RouterStateChanger.islocked = true;
                                        //NetworkLayerServer.simpleDVR(x.getRouterId());
                                        //System.out.println("-----in 3b");
                                        NetworkLayerServer.DVR(x.getRouterId());
                                        RouterStateChanger.islocked = false;
                                        RouterStateChanger.msg.notify();
                                    }
                                }
                                j++;
                            }
                        }//if gateway x not null
                        break;
                    }//if destination matched

                }//searching s.routingTable
//                if(x ==null) System.out.println("x null");
                  s = x;
                System.out.print(x.getRouterId()+"->");
//                if(s ==null) System.out.println("s null");
                p.hopcount++;
                //System.out.println("hop count:"+p.hopcount);
                routingPath+= s.strRoutingTable();
                if(s.getRouterId() == d.getRouterId()) break;

            }//while-loop
        }//if s.state true

        return true;


        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
