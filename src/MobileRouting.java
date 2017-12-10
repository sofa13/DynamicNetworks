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
 * A MobileRouting for Jbotsim
 *
 * @author slepage
 */
public class MobileRouting extends Node {

    boolean firstclock = true;
    boolean receivemsg = false;
    boolean done = false;
    Message m = null;
    int k = 0;
    int n = 0;
    int destID = 0;
    int modemsg = 0;
    int mode = 0;
    int msgcontent = 0;
    Node sender = null;

    public MobileRouting() {

        this.disableWireless();
    }

    public MobileRouting(int id) {
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
	            list.add(this.getID()); //msg
	            list.add(23); //destination
	            list.add(1); //mode
        		Message m = new Message(list);
	            
        		System.out.println(this.getNeighbors());
	            
	            become("INITIATOR");	           
	            this.sendAll(m);	            
        	}
        }
    }

    @SuppressWarnings("unchecked")
	public void receive(Message message) {
		become("RECEIVED");
    	m = message;
    	sender = message.getSender();
    	msgcontent = ((ArrayList<Integer>)message.getContent()).get(0);
    	destID = ((ArrayList<Integer>)message.getContent()).get(1);
    	modemsg = ((ArrayList<Integer>)message.getContent()).get(2);
    	receivemsg = true;
    }
    
    public void generateMsg(int newmode) {
    	List<Integer> list = new ArrayList<Integer>();
        list.add(msgcontent);
        list.add(destID);
        list.add(newmode);
		m = new Message(list);
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
    public void CounterFlooding() {
    	if (k < n*2) {
	    	broadcast();
	    	k += 1;
    	} else {
    		become("DONE");
    	}
    }
    
	@Override
    public void onMessage(Message message) {
    	super.onMessage(message);
    	System.out.println("My ID: "+this.getID()+" RCVD: "+message.toString());
    	onMessageOrLinkChange(message);
    }
    public void onMessageOrLinkChange(Message message) {		
		if (mode == 0) {
			receive(message);
			n = destID + 1;
			if ((modemsg == 1 && this.getID() == destID)||(modemsg == 2))
				mode = 2;
			else
				generateMsg(1);
				broadcast();
				mode = 1;
		} else if (mode == 1) {
			//on link change
			if (message == null) {
				n += 1;
				generateMsg(1);
				broadcast();
			}
			receive(message);
			if (modemsg == 1 && destID > n) {
				n = destID;
				generateMsg(1);
				broadcast();
			} else if (modemsg == 2) {
				n = n > destID ? n : destID;
				mode = 2;
			}
		} else if (mode == 2) {
			//process/delete message locally
			generateMsg(2);
			CounterFlooding();
			receive(message);
			if (n > destID) {
				n = destID;
				CounterFlooding();
			}
		}
		if (done) {
    		System.out.println("*** DONE, ID " + this.getID() + " ***");
    		System.out.println("*** TOTAL NUMBER OF MESSAGES ***");
    		System.out.println(this.getTotalMessages());
    		System.out.println();
    		System.out.println("*** TOTAL TIME ***");
    		System.out.println(this.getTotalTime());
    		System.out.println();
		}
    }
    public void onLinkChange(Link link) {
    	if (receivemsg)
    		sender = null;
    		onMessageOrLinkChange(null);
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
        tpg.setDefaultNodeModel(MobileRouting.class);
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