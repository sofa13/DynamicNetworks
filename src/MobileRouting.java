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
 * A MobileRouting for Jbotsim
 *
 * @author slepage
 */
public class MobileRouting extends Node {

    boolean firstclock = true;
    boolean receivemsg = false;
    boolean done = false;
    boolean firstmode2 = true;
    boolean firstprint = true;
    boolean firstcfbroadcast = true;
    Message m = null;
    int k = 0;
    int n = 0;
    int destID = 0;
    int modemsg = 0;
    int mode = 0;
    int msgcontent = 0;

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
        		System.out.println("My ID: "+this.getID()+" Clock");
        		
	            List<Integer> list = new ArrayList<Integer>();
	            list.add(this.getID()); //msg
	            list.add(10); //destination
	            list.add(0); //mode
	            
	            mode = 1;
	            
        		Message m = new Message(list);
	            
	            become("INITIATOR");	           
	            this.sendAll(m);	            
        	} else {
         		become("PASSIVE");
         	}
        	firstclock = false;
        }
    }

    @SuppressWarnings("unchecked")
	public void receive(Message message) {
		become("RECEIVED");
    	m = message;
    	msgcontent = ((ArrayList<Integer>)message.getContent()).get(0);
    	destID = ((ArrayList<Integer>)message.getContent()).get(1);
    	modemsg = ((ArrayList<Integer>)message.getContent()).get(2);
    	receivemsg = true;
    }
    
    public List<Integer> generateMsg(int newmode) {
    	List<Integer> list = new ArrayList<Integer>();
        list.add(msgcontent);
        list.add(destID);
        list.add(newmode);
		return list;
    }
    
    public void broadcast(boolean linkChange, int bcmode) {
    	Node bcSender = linkChange? null : m.getSender();

		Message bcMessage = new Message(generateMsg(bcmode));
		
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
    	} else if (s == "DONE") {
	    	done = true;
	    	this.setState("DONE");
	    	this.setColor(Color.white);
    	}
    }
    
    public void doneMsg() {
		try {
			PrintWriter out;
			out = new PrintWriter(new FileWriter("./src/MobileRoutingCorrectness.txt"));
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
		if (mode == 0) {
			receive(message);
			n = destID + 1;
			if ((modemsg == 1 && this.getID() == destID)||(modemsg == 2)) {
				if (this.getID() == destID && firstprint) {
					doneMsg();
					firstprint = false;
				}
				mode = 2;
			} else {
				broadcast(linkChange, 1);
				mode = 1;
			}
		} 
		if (mode == 1) {
			//on link change
			if (linkChange) {
					n += 1;
					broadcast(linkChange, 1);
					return;
			}
			//on msg received
			receive(message);
			if (modemsg == 1 && destID > n) {
				n = destID;
				broadcast(linkChange, 1);
			} else if (modemsg == 2) {
				n = n > destID ? n : destID;
				mode = 2;
			}
		} 
		if (mode == 2) {
			CounterFloodingMethod(linkChange, n, 2);
			if (!linkChange)
				receive(message);
			if (n > destID)
				n = destID;
			CounterFloodingMethod(linkChange, n, 2);
		}
    }
    public void CounterFloodingMethod(boolean linkChange, int nprime, int cfmode) {
    	if (firstcfbroadcast) {
			broadcast(linkChange, cfmode);
			k = 0;
			firstcfbroadcast = false;
		} else {
	    	if (k < nprime*2 && linkChange) {
		    	broadcast(linkChange, cfmode);
		    	k += 1;
	    	} else if (k >= nprime*2 && linkChange){
	    		become("DONE");
	    	}
		}
    }
    public void onLinkChange(Link link) {
    	if (receivemsg) {
    		onMessageOrLinkChange(null, true);
    	}
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
    		System.out.println("Usage: IDFlooding <number of nodes> <thin or dense topology> <slow or fast speed>");
    		return;
    	}
    	
    	int nodes = Integer.parseInt(args[0]);
    	String toptype = args[1];
    	String speed = args[2];
    	
    	if (nodes > 50 || nodes < 1) {
        	System.out.println("Node size should be greater than 1 and less than 50");
        	return;
        } else if (!(toptype.equals("thin") || toptype.equals("dense"))) {
        	System.out.println("Invalid topology. Topology: thin, dense");
        	return;
        } else if (!(speed.equals("slow") || speed.equals("fast"))) {
        	System.out.println("Invalid speed. Speeds: slow, fast");
        	return;
        }
    	
    	Random rnd = new Random();
    	
        Topology tpg = new Topology();
        tpg.setDefaultNodeModel(MobileRouting.class);
               
        int size = nodes; 
        
        tpg.setMessageEngine(new MessageEngine());

        if (toptype.equals("thin")) {
	        TopologyGenerator.generateRingLine(tpg, size);
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.ADVERSARY);
        } else if (toptype.equals("dense")){
        	TopologyGenerator.generateCompleteGraph(tpg, size, .2, .2);	        
	        tpg.setDynamicEngine(new DynamicEngine(), DynamicEngine.Type.RANDOM);
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
        
        if (speed.equals("fast")) {
        	tpg.setClockSpeed(1000,0);
            tpg.setClockSpeed(1010,1);
        } else if (speed.equals("slow")) {
        	tpg.setClockSpeed(6000,0);
            tpg.setClockSpeed(6010,1);
        } 
        
        new JViewer(tpg);
        tpg.start();
    }

}