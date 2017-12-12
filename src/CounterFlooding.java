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
    boolean done = false;
    Message m = null;
    Node sender = new Node();
    int k = 0;
    int n = 25;

    public CounterFlooding() {

        this.disableWireless();
    }

    public CounterFlooding(int id) {
        this.setID(id);
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
	            
        		System.out.println(this.getNeighbors());
	            
	            become("INITIATOR");	           
	            this.sendAll(m);	            
        	} else {
         		become("PASSIVE");
         	}
        	firstclock = false;
        }
    }

    public void receive(Message message, boolean linkChange) {
    	m = message;
    	sender = message.getSender();
    	become("RECEIVED");
    	receivemsg = true;
    }
    
    public void broadcast() {
    	for (Node node : this.getNeighbors()) {
            if (node != sender) {
                this.send(node, m);
            }
        }
    }
    
    public void become(String s) {
    	if (s == "INITIATOR") {
    		done = false;
    		this.setState("INITIATOR");
    		this.setColor(Color.red);
    	} else if (s == "PASSIVE"){
    		done = false;
    		this.setState("PASSIVE");
    	} else if (s == "RECEIVED") {
    		done = false;
    		this.setState("RECEIVED");
    		this.setColor(Color.black);
    		checkAllReceived();
    	} else if (s == "DONE") {
	    	done = true;
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
			out = new PrintWriter(new FileWriter("./src/CounterFloodingCorrectness.txt"));
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
    	System.out.println("My ID: "+this.getID()+" RCVD: "+message.toString());
    	onMessageOrLinkChange(message, false);
    }
    public void onMessageOrLinkChange(Message message, boolean linkChange) {		
    	CounterFloodingMethod(message, linkChange);
    }
    public void CounterFloodingMethod(Message message, boolean linkChange) {
    	if (!receivemsg) {
			receive(message, linkChange);
			broadcast();
			k = 0;
		} else {
	    	if (k < n*2 && linkChange) {			
		    	broadcast();
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
    	if(args.length != 2) {
    		System.out.println("Usage: CounterFlooding <number of nodes> <thin or dense topology>");
    		return;
    	}
    	
    	int nodes = Integer.parseInt(args[0]);
    	String toptype = args[1];
    	
    	Random rnd = new Random();
    	
        Topology tpg = new Topology();
        tpg.setDefaultNodeModel(CounterFlooding.class);
        
        if (nodes > 50 || nodes < 1) {
        	System.out.println("Node size should be greater than 1 and less than 50");
        	return;
        }
        
        int size = nodes;
        
        tpg.setMessageEngine(new MessageEngine());

        if (toptype.equals("Thin")) {
	        TopologyGenerator.generateRingLine(tpg, size);
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.ADVERSARY);
        } else if (toptype.equals("Dense")){
			TopologyGenerator.generateCompleteGraph(tpg, size, .5, .4);	        
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.RANDOM);
        } else {
        	System.out.println("Invalid topology type. Topology types: Thin, Dense");
        	return;
        }
        
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
        
        tpg.setClockSpeed(2000,0);
        tpg.setClockSpeed(2010,1);
        new JViewer(tpg);
        tpg.start();
    }
}