package jbotsim;

import jbotsim.Link.Type;
import jbotsim.event.ClockListener;
import jbotsim.event.StartListener;

import java.util.ArrayList;
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
    	if (type == Type.ADVERSARY) {
    		processDynamicNetwork();
    		//adversarial();
    	} else if (type == Type.RANDOM) {
        	updateLinks();
    	}
    }
    
    protected void updateLinks(){
    	List<Node> nodes = topology.getNodes();
        List<Link> links = topology.getLinks();
        
        if (nodes != null && links != null) {
        	int numnodes = nodes.size();
        	if (numnodes == topology.topSize) {
				Random r = new Random();
				Topology tp = topology;
				double birthRate = topology.birthRate; 
				double deathRate = topology.deathRate;
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
        List<Node> nodes = topology.getNodes();
        List<Link> links = topology.getLinks();
        int size = topology.topSize;
        Random rnd = new Random();
        
        if (nodes != null && links != null) {
        	int numnodes = nodes.size();
        	if (numnodes == size) {
        		// first remove all links
        		for (Link link : links){
        			topology.removeLink(link);
        		}
        		
        		// second replace with random links
		        List<Integer> nodeIds =new ArrayList<Integer>();
		        for (Node node : nodes) {
		        	nodeIds.add(node.getID());
		        }
		        int randomIndex = rnd.nextInt(nodeIds.size());
		        int start = nodeIds.get(randomIndex);
		        nodeIds.remove(randomIndex);
		        while (nodeIds.size() > 0) {
		        	randomIndex = rnd.nextInt(nodeIds.size());
		        	int end = nodeIds.get(randomIndex);
		        	
		        	Node n1 = nodes.get(start);
			    	Node n2 = nodes.get(end);
			    	
		        	Link newlink = new Link(n1, n2);
		        	topology.addLink(newlink);
		        	
		        	start = end;
		        	nodeIds.remove(randomIndex);
		        }
        	}
        }
    }
    
    protected void adversarial(){
    	
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
			    	Link newlink = new Link(n1, n2);
			    	
			    	int id = n2.getID();
			    	Node n3 = nodes.get(id-1);
			    	Link oldlink = new Link(n3, n2);
			    	
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
