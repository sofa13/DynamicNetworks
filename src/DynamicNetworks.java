import jbotsim.DynamicEngine;
import jbotsim.MessageEngine;
import jbotsim.Topology;
import jbotsim.ui.JViewer;
import jbotsimx.topology.TopologyGenerator;

public class DynamicNetworks {
    public static void main(String args[]) {
    	if(args.length != 3)
    		System.out.println("Usage: DynamicNetworks <Algorithm> <number of nodes> <thin or dense topology>");
    	
    	String algorithm = args[0];
    	int nodes = Integer.parseInt(args[1]);
    	String toptype = args[2];
    	
        Topology tpg = new Topology();
        if (algorithm.equals("CounterFlooding"))
        	tpg.setDefaultNodeModel(CounterFlooding.class);
        else if (algorithm.equals("IDFlooding"))
        	tpg.setDefaultNodeModel(IDFlooding.class);
        else if (algorithm.equals("MobileRouting"))
        	tpg.setDefaultNodeModel(MobileRouting.class);
        else {
        	System.out.println("Invalid algorithm name. Algorithms: CounterFlooding, IDFlooding, MobileRouting");
        	return;
        }
        
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
        tpg.setClockSpeed(2050,1);
        new JViewer(tpg);
        tpg.start();
    }
}
