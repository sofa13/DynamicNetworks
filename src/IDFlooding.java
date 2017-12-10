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
    boolean done = false;
    Message m = null;
    Node sender = new Node();
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
        		firstclock = false;
	           
        		System.out.println("My ID: "+this.getID()+" Clock");
	            List<Integer> list = new ArrayList<Integer>();
	            list.add(this.getID());
	            list.add(this.getID());
        		Message m = new Message(list);
	            
        		System.out.println(this.getNeighbors());
	            
	            become("INITIATOR");	           
	            this.sendAll(m);	            
        	}
        }
    }

    public void receive(Message message, boolean linkChange) {    	
    	m = message;
    	sender = linkChange? null : message.getSender();
    	//if (!receivemsg) {
	    	become("RECEIVED");
	    	receivemsg = true;
    	//}
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
    
    @SuppressWarnings("unchecked")
    public void onMessageOrLinkChange(Message message, boolean linkChange) {
		List<Integer> list = (ArrayList<Integer>)message.getContent();
		
		int maxID = list.get(1);
		
		if (!receivemsg) {
			maxID = this.getID() > maxID ? this.getID(): maxID; 
		}
		
		if (maxID > n) {
			List<Integer> list2 = new ArrayList<Integer>();
			list2.add(list.get(0));
			list2.add(maxID);
			receive(new Message(list2), linkChange);
			n = maxID;			
		}
		if (k < n*2) {
	    	broadcast();
	    	k += 1;
    	} else {
    		become("DONE");
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
        Topology tpg = new Topology();
        tpg.setDefaultNodeModel(IDFlooding.class);
        int size = 25;
        tpg.setMessageEngine(new MessageEngine());

        boolean flag = true;
        if (flag) {
	        TopologyGenerator.generateRingLine(tpg, size);
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.ADVERSARY);
        } else {
			TopologyGenerator.generateCompleteGraph(tpg, size);	        
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.RANDOM);
        }
        
        tpg.setClockSpeed(2000,0);
        tpg.setClockSpeed(2050,1);
        new JViewer(tpg);
        tpg.start();
    }

}