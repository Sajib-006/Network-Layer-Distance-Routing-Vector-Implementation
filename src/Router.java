//Work needed
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;
    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();



        /**
         * 80% Probability that the router is up
         */
//        Random random = new Random();
//        double p = random.nextDouble();
//        if(p < 0.80) state = true;
//        else state = false;
        state = true;
        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }



    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        routingTable = new ArrayList<>();
        for(int i=0; i<NetworkLayerServer.routers.size(); i++){
            RoutingTableEntry entry = new RoutingTableEntry(i+1,Constants.INFINITY,-1);
            Router gateway = NetworkLayerServer.routerMap.get(entry.getRouterId());
            if(entry.getRouterId() == routerId) {
                entry.setDistance(0);
                entry.setGatewayRouterId(routerId);
            }
            else if(neighborRouterIDs.contains(entry.getRouterId()) && gateway.getState()== true ){
                entry.setDistance(1);
                entry.setGatewayRouterId(entry.getRouterId());
            }
            else entry.setDistance(Constants.INFINITY);
            routingTable.add(entry);
        }
        //System.out.println(routerId+"::"+routingTable.size());
        
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {
        double dist_xz = 0, dist_zy = 0, dist_xy = 0;
        int next_hop_zy;
        for(RoutingTableEntry entry: routingTable){
            if(entry.getRouterId() == neighbor.getRouterId()) dist_xz = entry.getDistance();
        }
        boolean updated = false;
        //System.out.println(routerId+":table size:"+routingTable.size());
        if(getState() && routingTable.size()!=0 && neighbor.getRoutingTable().size()!=0)
        for(int i=0; i<routingTable.size(); i++) {
            dist_xy = routingTable.get(i).getDistance();
            //System.out.println("-----in update: i="+i);
            RoutingTableEntry y = neighbor.getRoutingTable().get(i);
            dist_zy = neighbor.getRoutingTable().get(i).getDistance();
            if(dist_xz + dist_zy < dist_xy ){
                routingTable.get(i).setDistance(dist_xz + dist_zy);
                routingTable.get(i).setGatewayRouterId(neighbor.getRouterId());
                updated = true;
            }

        }
        return updated;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        double dist_xz = 0, dist_zy = 0, dist_xy = 0;
        int next_hop_zy,next_hop_xy;
        for(RoutingTableEntry entry: routingTable){
            if(entry.getRouterId() == neighbor.getRouterId()) dist_xz = entry.getDistance();
        }
        boolean updated = false;
        //System.out.println(routerId+":table size:"+routingTable.size()+"..."+neighbor.getRoutingTable().size());
        if(getState() && routingTable.size()!=0 && neighbor.getRoutingTable().size()!=0)
        for(int i=0; i<routingTable.size(); i++) {
            //System.out.println("-----in update: i="+i);
            dist_xy = routingTable.get(i).getDistance();
            dist_zy = neighbor.getRoutingTable().get(i).getDistance();
            next_hop_xy = routingTable.get(i).getGatewayRouterId();
            next_hop_zy = neighbor.getRoutingTable().get(i).getGatewayRouterId();
            if((next_hop_xy == neighbor.getRouterId()) || ( (dist_xz + dist_zy) < dist_xy && next_hop_zy != routerId)){
                routingTable.get(i).setDistance(dist_xz + dist_zy);
                routingTable.get(i).setGatewayRouterId(neighbor.getRouterId());
                updated = true;
            }

        }
        return updated;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }
    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
