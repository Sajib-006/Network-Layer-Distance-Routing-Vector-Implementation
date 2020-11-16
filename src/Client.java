import java.util.ArrayList;
import java.util.Random;

//Work needed
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        /**
         * Tasks
         */
        
        /*
        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
        ArrayList<EndDevice> endDevices = new ArrayList<>();
        endDevices = (ArrayList<EndDevice>) networkUtility.read();
        Random random = new Random();
        int dropCount = 0;
        int total_drop_count = 0;
        int total_hop_count = 0,actual_hops=0,actual_success=0;
        int success_cnt=0, fail_cnt=0;
        for(int i=0; i<10; i++){
            System.out.println("client loop: "+i);
            String message = "hello peep";

            IPAddress sourceIP = endDevices.get(endDevices.size()-1).getIpAddress();

            int random_idx = random.nextInt(endDevices.size());
            IPAddress destIP = endDevices.get(random_idx).getIpAddress();
            Packet packet = new Packet(message,"nothing",sourceIP,destIP);
            System.out.println("SOurce IP:"+packet.getSourceIP());
            System.out.println("Dest IP:"+packet.getDestinationIP());
            //int clientID = random.nextInt(NetworkLayerServer.clientCount);
            if(i<10){
                packet.setSpecialMessage("SHOW_ROUTE");
                networkUtility.write(packet);
                String routingPath = (String) networkUtility.read();
                System.out.println(routingPath);
                String msg = (String) networkUtility.read();
                System.out.println(msg);
                if(msg.equalsIgnoreCase("FAILURE")) {
                    dropCount++;
                    fail_cnt++;
                    //total_drop_count+= dropCount;
                }
                else if(msg.equalsIgnoreCase("Packet not received for Connection problem")) {
                    //do nothing
                    break;
                }
                else{
                    String parts[] = msg.split(":");
                    int hopCount = Integer.parseInt(parts[1]);
                    total_hop_count+= hopCount;
                    success_cnt++;
                    if(hopCount>1){
                        actual_hops+=hopCount;
                        actual_success++;
                    }
                }
            }
            else{
                networkUtility.write(packet);
                String msg = (String) networkUtility.read();
                System.out.println(msg);
                if(msg.equalsIgnoreCase("FAILURE")) {
                    dropCount++;
                    //total_drop_count+= dropCount;
                }
                else if(msg.equalsIgnoreCase("Packet not received for Connection problem")) {
                    //do nothing
                    break;
                }
                else{
                    String parts[] = msg.split(":");
                    int hopCount = Integer.parseInt(parts[1]);
                    total_hop_count+= hopCount;
                    success_cnt++;
                    if(hopCount>1){
                        actual_hops+=hopCount;
                        actual_success++;
                    }
                }


            }
        }
        System.out.println("Drop Count: "+ dropCount);
        System.out.print("Total hop Count: "+ total_hop_count);
        if(success_cnt!=0) System.out.println("  Average hop Count: "+(total_hop_count*1.0)/success_cnt);
        System.out.println();
        System.out.print("Actual hop Count: "+ actual_hops);
        if(actual_success!=0) System.out.println("  Average Actual hop Count: "+(actual_hops*1.0)/actual_success);
    }
}
