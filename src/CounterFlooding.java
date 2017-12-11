import java.awt.Color;

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
    boolean firstreceive = true;
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
        		firstclock = false;
	           
        		System.out.println("My ID: "+this.getID()+" Clock");
	            
        		Message m = new Message(new Integer(this.getID()));
	            
        		System.out.println(this.getNeighbors());
	            
	            become("INITIATOR");	           
	            this.sendAll(m);	            
        	}
        }
    }

    public void receive(Message message, boolean linkChange) {
    	m = message;
    	sender = linkChange? null : message.getSender();
    	if (!receivemsg) {
	    	become("RECEIVED");
	    	receivemsg = true;
    	}
    }
    
    public void broadcast() {
    	for (Node node : this.getNeighbors()) {
            if (node != sender) {
                this.send(node, m);
            }
        }
    }
    
    public void become(String s) {
    	if (s == "DONE") {
	    	done = true;
	    	this.setColor(Color.white);
    	} else if (s == "INITIATOR") {
    		done = false;
    		this.setColor(Color.red);
    	} else if (s == "RECEIVED") {
    		done = false;
    		this.setColor(Color.black);
    	}
    }
    
    public void doneMsg() {
    	System.out.println("*** DONE, ID " + this.getID() + " ***");
		System.out.println("*** TOTAL NUMBER OF MESSAGES ***");
		System.out.println(this.getTotalMessages());
		System.out.println();
		System.out.println("*** TOTAL TIME ***");
		System.out.println(this.getTotalTime());
		System.out.println();
    }
    
    @Override
    public void onMessage(Message message) {
    	super.onMessage(message);
    	System.out.println("My ID: "+this.getID()+" RCVD: "+message.toString());
    	onMessageOrLinkChange(message, false);
    }
    public void onMessageOrLinkChange(Message message, boolean linkChange) {
		receive(message, linkChange);
		if (firstreceive) {
			broadcast();
			firstreceive = false;
		} else {
	    	if (k < n*2) {
		    	broadcast();
		    	k += 1;
	    	} else {
	    		become("DONE");
	    	}
		}

    	if (done && !linkChange) {
    		doneMsg();
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
			TopologyGenerator.generateCompleteGraph(tpg, size);	        
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.RANDOM);
        } else {
        	System.out.println("Invalid topology type. Topology types: Thin, Dense");
        	return;
        }
        
        tpg.setClockSpeed(2000,0);
        tpg.setClockSpeed(2001,1);
        new JViewer(tpg);
        tpg.start();
    }
}