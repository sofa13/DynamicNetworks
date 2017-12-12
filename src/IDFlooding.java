import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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
 * A IDFlooding for Jbotsim
 *
 * @author slepage
 */
public class IDFlooding extends Node {

    boolean firstclock = true;
    boolean receivemsg = false;
    boolean firstbroadcast = true;
    boolean done = false;
    Message m = null;
    int k = 0;
    int n = 0;

    public IDFlooding() {

        this.disableWireless();
    }

    public IDFlooding(int id) {
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
        		
        		List<Integer> list = new ArrayList<Integer>();
	            list.add(new Integer(this.getID()));
	            list.add(new Integer(this.getID()));
	            
	            Message m = new Message(list);
	            
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
    	become("RECEIVED");
    	receivemsg = true;
    }
    
    @SuppressWarnings("unchecked")
	public void broadcast(boolean linkChange, boolean append) {
    	Node bcSender = linkChange? null : m.getSender();
    	Message bcMessage = m;
    	if (append) {
	    	List<Integer> list = (ArrayList<Integer>)m.getContent();
			list.set(1,n);
			bcMessage = new Message(list);
    	}
    	for (Node node : this.getNeighbors()) {
            if (node != bcSender) {
                this.send(node, bcMessage);
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
    	System.out.println("*** ALL RECEIVED, ID " + this.getID() + " ***");
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
    	if (!this.getIfAllReceivedNotif()) {
    		System.out.println("My ID: "+this.getID()+" RCVD from: "+message.getSender().getID() + "  MSG: "+message.toString());
    	}
    	onMessageOrLinkChange(message, false);
    }
    
    @SuppressWarnings("unchecked")
	public void onMessageOrLinkChange(Message message, boolean linkChange) {
    	List<Integer> list = (ArrayList<Integer>)message.getContent();		
		int w = list.get(1);
		
    	if (!receivemsg) {
    		n = 0;
    		receive(message, linkChange);
    		w = this.getID() > w ? this.getID(): w;
    	}		
		
		if (w > n) {
			become("RECEIVED");
			n = w;
		}
		CounterFloodingMethod(message, linkChange, n+1, true);
    }
    
    public void CounterFloodingMethod(Message message, boolean linkChange, int nprime, boolean append) {
    	if (firstbroadcast) {
			broadcast(linkChange, append);
			k = 0;
			firstbroadcast = false;
		} else {
	    	if (k < nprime*2 && linkChange) {
		    	broadcast(linkChange, append);
		    	k += 1;
	    	} else if (k >= nprime*2 && linkChange){
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
    		System.out.println("Usage: IDFlooding <number of nodes> <thin or dense topology>");
    		return;
    	}
    	
    	int nodes = Integer.parseInt(args[0]);
    	String toptype = args[1];
    	
        Topology tpg = new Topology();
        tpg.setDefaultNodeModel(IDFlooding.class);
        
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
        
        tpg.setClockSpeed(500,0);
        tpg.setClockSpeed(501,1);
        new JViewer(tpg);
        tpg.start();
    }

}