import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
 * A IDFlooding for Jbotsim
 *
 * @author slepage
 */
public class IDFlooding extends Node {

    boolean firstclock = true;
    boolean receivemsg = false;
    boolean firstbroadcast = true;
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
	            
	            Message msg = new Message(list);

	            become("INITIATOR");	           
	            this.sendAll(msg);	            
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

		List<Integer> list = new ArrayList<Integer>();
    	list.add(0);
        list.add(n);
		Message bcMessage = new Message(list);

    	for (Node node : this.getNeighbors()) {
            if (node != bcSender) {
                this.send(node, bcMessage);
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
			out = new PrintWriter(new FileWriter("./src/IDFloodingCorrectness.txt"));
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
    
    @SuppressWarnings("unchecked")
	public void onMessageOrLinkChange(Message message, boolean linkChange) {
		
    	List<Integer> list = (ArrayList<Integer>)message.getContent();		
		int w = list.get(1);
    	
    	if (!receivemsg) {
    		n = 0;
    		receive(message);
    		w = this.getID() > w ? this.getID(): w;
    	}		

		if (w > n) {
			become("RECEIVED");
			n = w;
		}
		CounterFloodingMethod(linkChange, n+1);
    }
    
    public void CounterFloodingMethod(boolean linkChange, int nprime) {
    	if (firstbroadcast) {
			broadcast(linkChange);
			k = 0;
			firstbroadcast = false;
		} else {
	    	if (k < nprime*2 && linkChange) {
		    	broadcast(linkChange);
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
    	if(args.length != 3) {
    		System.out.println("Usage: IDFlooding <number of nodes> <thin dense or adversary topology> <slow or fast speed>");
    		return;
    	}
    	
    	int nodes = Integer.parseInt(args[0]);
    	String toptype = args[1];
    	String speed = args[2];
    	
    	if (nodes > 50 || nodes < 1) {
        	System.out.println("Node size should be greater than 1 and less than 50");
        	return;
        } else if (!(toptype.equals("thin") || toptype.equals("dense") || toptype.equals("adversary"))) {
        	System.out.println("Invalid topology. Topology: thin, dense or adversary");
        	return;
        } else if (!(speed.equals("slow") || speed.equals("fast"))) {
        	System.out.println("Invalid speed. Speeds: slow, fast");
        	return;
        }
    	
    	Random rnd = new Random();
    	
        Topology tpg = new Topology();
        tpg.setDefaultNodeModel(IDFlooding.class);
               
        int size = nodes; 
        
        tpg.setMessageEngine(new MessageEngine());

        if (toptype.equals("thin")) {
	        TopologyGenerator.generateRingLine(tpg, size);
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.THIN);
        } else if (toptype.equals("dense")){
        	TopologyGenerator.generateCompleteGraph(tpg, size, .4, .4);	        
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.DENSE);
        } else if (toptype.equals("adversary")){
        	TopologyGenerator.generateRingLine(tpg, size);
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.ADVERSARY);
        }
        
        // generate random id configuration for thin and dense topology
        if (toptype.equals("thin") || toptype.equals("dense")) {
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