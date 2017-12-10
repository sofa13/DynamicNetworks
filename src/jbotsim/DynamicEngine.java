package jbotsim;

import jbotsim.Link.Type;
import jbotsim.event.ClockListener;

import java.util.List;
import java.util.Random;

/**
 * Created by slepage on 24/11/17.
 */
public class DynamicEngine implements ClockListener {
    protected Topology topology;
    protected List<Link> saveLinks;
    protected boolean debug=false;
    boolean addRemoveFlag=false;
    Link prevlink = null;
    Link prevaddlink = null;
    int round = 0;
    int step = 0;
    public static enum Type{ADVERSARY, RANDOM};
    public Type type;
    boolean first = true;

    public void setTopology(Topology topology, Type type){
        this.topology = topology;
        this.saveLinks = topology.getLinks();
        this.type = type;
    }
    public void setSpeed(int speed){
        topology.removeClockListener(this);
        topology.addClockListener(this, speed);
    }
    public void onClock(){
    	if (type == Type.ADVERSARY)
    		processDynamicNetwork();
    	else if (type == Type.RANDOM)
        	updateLinks();
    }
    
    protected void updateLinks(){
    	List<Node> nodes = topology.getNodes();
        List<Link> links = topology.getLinks();
        
        if (nodes != null && links != null) {
        	int numnodes = nodes.size();
        	if (numnodes == 25) {
				Random r = new Random();
				Topology tp = topology;
				double birthRate = .4; 
				double deathRate = .4;
				for (Link l : saveLinks){
					if (l != null) {
						if (tp.getLinks().contains(l) && r.nextDouble() < deathRate) {
							tp.removeLink(l);
						}
						else if (!tp.getLinks().contains(l) && r.nextDouble() < birthRate) {
							tp.addLink(l);
						}
					}
				}
        	}
        }
	}
    protected void processDynamicNetwork(){
    	
    	boolean directed = false;
        Link.Type type = directed?Link.Type.DIRECTED:Link.Type.UNDIRECTED;
        List<Node> nodes = topology.getNodes();
        List<Link> links = topology.getLinks();
        
        if (nodes != null && links != null) {
        	int numnodes = nodes.size();
        	if (numnodes == 25) {
        		if (first) {
        			first = false;
        		} else {
	        		if (step % numnodes == 0) {
	        			step = 0;
	        			step += 1;
	        		}
		        	
			        if (round % numnodes == 0 && round != 0) {
			        	round = 0;
			        }
		    		if (step % numnodes == 0 && step != 0) {
		    			step = 0;
		    			round += 1;
		    		}
		    		
			    	Node n1 = nodes.get(round);
			    	Node n2 = nodes.get(step);
			    	Link newlink = new Link(n1, n2, type);
			    	
			    	int id = n2.getID();
			    	Node n3 = nodes.get(id-1);
			    	Link oldlink = new Link(n3, n2, type);
			    	
			    	if (prevlink != null && prevaddlink !=null && oldlink != null && newlink != null) {
			    		if (prevlink != null && prevaddlink !=null) {
			    			topology.addLink(newlink);
				    		topology.addLink(prevlink);
				    		topology.removeLink(prevaddlink);
				    		topology.removeLink(oldlink);
			    		} else {
			    			topology.addLink(newlink);
				    		topology.removeLink(oldlink);
				    	}
			    	} 
				    	
			    	// increase step and save prev link
			    	prevlink = oldlink;
			    	prevaddlink = newlink;
			    	step += 1;
        		}
        	}
        }
    }
}
