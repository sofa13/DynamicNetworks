import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import jbotsim.Link;
import jbotsim.Message;
import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.MessageEngine;
import jbotsim.DynamicEngine;
import jbotsim.ui.JViewer;
import jbotsimx.topology.TopologyGenerator;

/**
 *
 * A CounterFlooding for Jbotsim
 *
 * @author slepage
 */
public class CounterFlooding extends Node {

    boolean firstclock = true;
    boolean receivemsg = false;
    Message m = null;
    int k = 0;
    int n = 0;

    public CounterFlooding() {

        this.disableWireless();
    }

    public CounterFlooding(String args) {
        n = Integer.parseInt(args);
        this.disableWireless();
    }

    @Override
    public void setID(int id) {
        super.setID(id);
    }

    @Override
    public void onClock() {
       
    	if (firstclock) {
        	if (this.getID() == 0) {	           
        		System.out.println("My ID: "+this.getID()+" Clock");
	            
        		Message m = new Message(new Integer(this.getID()));
	            
	            become("INITIATOR");	           
	            this.sendAll(m);	            
        	} else {
         		become("PASSIVE");
         	}
        	firstclock = false;
        }
    }

    public void receive(Message message) {
    	m = message;
    	become("RECEIVED");
    	receivemsg = true;
    }
    
    public void broadcast(boolean linkChange) {
    	Node bcSender = linkChange? null : m.getSender();
    	for (Node node : this.getNeighbors()) {
            if (node != bcSender) {
                this.send(node, m);
            }
        }
    }
    
    public void become(String s) {
    	if (s == "INITIATOR") {
    		this.setState("INITIATOR");
    		this.setColor(Color.red);
    	} else if (s == "PASSIVE"){
    		this.setState("PASSIVE");
    	} else if (s == "RECEIVED") {
    		this.setState("RECEIVED");
    		this.setColor(Color.black);
    		checkAllReceived();
    	} else if (s == "DONE") {
	    	this.setState("DONE");
	    	this.setColor(Color.white);
    	}
    }
    
    public void checkAllReceived() {
		if (this.getIfAllReceived()) {
			if (!this.getIfAllReceivedNotif()) {
				doneMsg();
			}
			this.setAllReceived(true);
		}
    }
    
    public void doneMsg() {
		try {
			PrintWriter out;
			out = new PrintWriter(new FileWriter("./CounterFloodingCorrectness.txt"));
			out.println("*** ALL RECEIVED, ID " + this.getID() + " ***");
			out.println(this.getTotalMessages());
			out.println();
			out.println("*** TOTAL TIME ***");
			out.println(this.getTotalTime());
			out.println();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    	
    }
    
    @Override
    public void onMessage(Message message) {
    	super.onMessage(message);
    	System.out.println("My ID: "+this.getID()+" RCVD: "+message.toString()+" n: " + n + " k: " + k);
    	onMessageOrLinkChange(message, false);
    }
    public void onMessageOrLinkChange(Message message, boolean linkChange) {		
    	CounterFloodingMethod(message, linkChange);
    }
    public void CounterFloodingMethod(Message message, boolean linkChange) {
    	if (!receivemsg) {
			receive(message);
			broadcast(linkChange);
			k = 0;
		} else {
	    	if (k < n*2 && linkChange) {			
		    	broadcast(linkChange);
		    	k += 1;
	    	} else if (k >= n*2 && linkChange){
	    		become("DONE");
	    	}
		}
    }
    public void onLinkChange(Link link) {
    	if (receivemsg)
    		onMessageOrLinkChange(m, true);
    }
    
    @Override
    public void onLinkAdded(Link link) {
    	onLinkChange(link);
    }
    
    @Override
    public void onLinkRemoved(Link link) {
    	onLinkChange(link);
    }
    public static void main(String args[]) {
    	if(args.length != 3) {
    		System.out.println("Usage: IDFlooding <number of nodes> <thin dense or adversary topology> <slow or fast speed>");
    		return;
    	}
    	
    	int nodes = Integer.parseInt(args[0]);
    	String toptype = args[1];
    	String speed = args[2];
    	
    	if (nodes > 200 || nodes < 1) {
        	System.out.println("Node size should be greater than 1 and less than 200");
        	return;
        } else if (!(toptype.equals("thin") || toptype.equals("dense") || toptype.equals("adversary"))) {
        	System.out.println("Invalid topology. Topology: thin, dense or adversary");
        	return;
        } else if (!(speed.equals("slow") || speed.equals("fast"))) {
        	System.out.println("Invalid speed. Speeds: slow, fast");
        	return;
        }
    	
    	DynamicEngine.Type type = DynamicEngine.Type.THIN;
    	if (toptype.equals("thin")) {
    		type = DynamicEngine.Type.THIN;
        } else if (toptype.equals("dense")){
        	type = DynamicEngine.Type.DENSE;
        } else if (toptype.equals("adversary")){
        	type = DynamicEngine.Type.ADVERSARY;
        }
   	
        Topology tpg = new Topology(type);
        tpg.setDefaultNodeModel(CounterFlooding.class);
               
        int size = nodes; 
        
        tpg.setMessageEngine(new MessageEngine());

        if (toptype.equals("thin")) {
	        TopologyGenerator.generateRingLine(tpg, size, true);
	        tpg.setDynamicEngine(new DynamicEngine(), type);
        } else if (toptype.equals("dense")){
        	TopologyGenerator.generateCompleteGraph(tpg, size, .4, .4, true);	        
	        tpg.setDynamicEngine(new DynamicEngine(), type);
        } else if (toptype.equals("adversary")){
        	TopologyGenerator.generateRingLine(tpg, size, true);
	        tpg.setDynamicEngine(new DynamicEngine(), type);
        }
        
        // generate random id configuration for thin and dense topology
        if (toptype.equals("thin") || toptype.equals("dense")) {
        	Random rnd = new Random();
	        int[] ids = new int[size];
	        for (int i = 0; i < ids.length; i++) {
	            ids[i] = i;
	        }
	        for (int i = 0; i < ids.length; i++) {
	            int j = rnd.nextInt(size);
	            int temp = ids[i];
	            ids[i] = ids[j];
	            ids[j] = temp;
	        }
	        int i = 0;
	        for (Node n : tpg.getNodes()) {
	            n.setID(ids[i]);
	            i++;
	        }
        }
        
        if (speed.equals("fast")) {
        	tpg.setClockSpeed(1000,0);
            tpg.setClockSpeed(1020,1);
        } else if (speed.equals("slow")) {
        	tpg.setClockSpeed(6000,0);
            tpg.setClockSpeed(6010,1);
        } 
        
        new JViewer(tpg);
        tpg.start();
    }
}