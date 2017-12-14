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
    protected List<Link> saveExistLinks = new ArrayList<Link>();
    protected boolean debug=false;
    boolean addRemoveFlag=false;
    Link prevlink = null;
    Link prevaddlink = null;
    int round = 0;
    int step = 0;
    public static enum Type{THIN, DENSE, ADVERSARY};
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
    	if (type == Type.THIN) {
    		thinTopology();
    	} else if (type == Type.DENSE) {
        	denseTopology();
    	} else if (type == Type.ADVERSARY) {
    		adversaryTopology();
    	}
    }
    
    protected void denseTopology(){
        if (topology.topSize !=0 ) {
        	List<Node> nodes = topology.getNodes();
            Random rnd = new Random();
        		
			Topology tp = topology;
			double birthRate = topology.birthRate; 
			double deathRate = topology.deathRate;
			
			// first remove all the exist links saved
    		for (Link link : saveExistLinks){
    			tp.removeLink(link);
    		}
    		
    		// next create random dense topology, by removing links, and adding more links
			for (Link l : saveLinks){
				if (l != null) {
					if (tp.getLinks().contains(l) && rnd.nextDouble() < deathRate) {
						tp.removeLink(l);
					}
					else if (!tp.getLinks().contains(l) && rnd.nextDouble() < birthRate) {
						tp.addLink(l);
					}
				}
			}
			
			// add links to ensure completeness (similar to thin topology
			List<Link> existLinks = tp.getLinks();
	        List<Integer> nodeIds =new ArrayList<Integer>();
	        for (Node node : nodes) {
	        	nodeIds.add(node.getID());
	        }
	        int randomIndex = rnd.nextInt(nodeIds.size());
	        int start = nodeIds.get(randomIndex);
	        nodeIds.remove(randomIndex);
	        List<Link> newLinks = new ArrayList<Link>();
	        while (nodeIds.size() > 0) {
	        	randomIndex = rnd.nextInt(nodeIds.size());
	        	int end = nodeIds.get(randomIndex);
	        	
	        	Node n1 = nodes.get(start);
		    	Node n2 = nodes.get(end);
		    	
		    	// check if new link about to be created already exists between the two nodes
		    	boolean alreadyexists = false;
		    	for (Link link : existLinks) {
		    		List<Node> linkEndNodes = link.endpoints();
	        		if (linkEndNodes.contains(n1) && linkEndNodes.contains(n2)) {
	        			alreadyexists = true;
	        			break;
	        		}
	        	}
		    	// if not already exists, add link
		    	if (!alreadyexists) {
		        	Link newlink = new Link(n1, n2);
		        	if (!existLinks.contains(newlink)) {
		        		tp.addLink(newlink);
		        		newLinks.add(newlink);
		        	}
		    	}
	        	start = end;
	        	nodeIds.remove(randomIndex);
	        }
	        saveExistLinks = newLinks;
        }
	}
    protected void thinTopology(){    	
    	if (topology.topSize != 0) {
    		List<Node> nodes = topology.getNodes();
            List<Link> links = topology.getLinks();
            Random rnd = new Random();
    		
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
    // requires node IDs to be in order
    protected void adversaryTopology(){
        if (topology.topSize != 0) {
        	int numnodes = topology.topSize;
        	List<Node> nodes = topology.getNodes();
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
