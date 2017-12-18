package jbotsimx.topology;

import jbotsim.Link;
import jbotsim.Node;
import jbotsim.Topology;
import jbotsimx.tvg.TVLink;

import java.util.List;
import java.util.Random;

public class TopologyGenerator {
    public static void generateCompleteGraph(Topology tp, int N, double br, double dr, boolean counterFlooding){
    	
    	Random r = new Random();
    	double birthRate = tp.birthRate = br;
    	double deathRate = tp.deathRate = dr;
    	double steadyProb = birthRate/(birthRate+deathRate);
		try{
			for (int i=0; i<N; i++){
				double angle=(2.0*Math.PI/N)*i;
				if (counterFlooding)
					tp.addNode(200+Math.cos(angle)*150, 200+Math.sin(angle)*150, tp.newInstanceOfModel("default", N));
				else
					tp.addNode(200+Math.cos(angle)*150, 200+Math.sin(angle)*150, tp.newInstanceOfModel("default"));
			}
		}catch (Exception e) {e.printStackTrace();}
		List<Node> nodes = tp.getNodes();
		for (int i=0; i<nodes.size(); i++){
			for (int j=i+1; j<nodes.size(); j++){
				if (r.nextDouble() < steadyProb)
					tp.addLink(new Link(nodes.get(i), nodes.get(j)));
			}
		}
		tp.topSize = N;
	}
	public static void generateLine(Topology tp, int order){
		int scale=(tp.getDimensions().width-50)/order;
		tp.setCommunicationRange(scale+1);
		for (int i=0; i<order; i++)
			tp.addNode(50+i*scale,100,tp.newInstanceOfModel("default"));
	}
	public static void generateRing(Topology topology, int nbNodes) {
		generateRing(topology, nbNodes, false);
	}
	public static void generateRingLine(Topology topology, int nbNodes, boolean counterFlooding){
		
		topology.setCommunicationRange(0);
		int scale=100;
		for (int i=0; i<nbNodes; i++) {
			double angle=(2.0*Math.PI/nbNodes)*i;
			if (counterFlooding)
				topology.addNode(200+Math.cos(angle)*150, 200+Math.sin(angle)*150,topology.newInstanceOfModel("default", nbNodes));
			else
				topology.addNode(200+Math.cos(angle)*150, 200+Math.sin(angle)*150,topology.newInstanceOfModel("default"));			
		}
		List<Node> nodes = topology.getNodes();
		for (int i=0; i<nbNodes-1; i++)
			topology.addLink(new Link(nodes.get(i), nodes.get(i+1)));
		topology.topSize = nbNodes;
	}
	public static void generateRing(Topology topology, int nbNodes, boolean directed){
		topology.setCommunicationRange(0);
		double angle=Math.PI*2.0/nbNodes;
		int scale=100;
		for (int i=0; i<nbNodes; i++)
			topology.addNode(50 + scale + Math.cos(angle*i)*scale,
					50 + scale + Math.sin(angle*i)*scale,topology.newInstanceOfModel("default"));

		List<Node> nodes = topology.getNodes();
		Link.Type type = directed?Link.Type.DIRECTED:Link.Type.UNDIRECTED;
		for (int i=0; i<nbNodes-1; i++)
			topology.addLink(new Link(nodes.get(i), nodes.get(i+1), type));
		topology.addLink(new Link(nodes.get(nbNodes - 1), nodes.get(0), type));
	}
	public static void generateGrid(Topology tp, int order){
		generateGrid(tp, order, order);
	}
	public static void generateGrid(Topology tp, int orderX, int orderY){
		int scale=(tp.getDimensions().width-50)/orderX;
		tp.setCommunicationRange(scale+1);
		for (int i=0; i<orderX; i++)
			for (int j=0; j<orderY; j++)
				tp.addNode(50+i*scale,50+j*scale,tp.newInstanceOfModel("default"));
	}
	public static void generateTorus(Topology tp, int order){
		int scale=(tp.getDimensions().width-50)/order;
		tp.setCommunicationRange(scale+1);
		Node[][] matrix = new Node[order][order];
		for (int i=0; i<order; i++)
			for (int j=0; j<order; j++){
				Node node = tp.newInstanceOfModel("default");
				tp.addNode(50+i*scale, 50+j*scale, node);
				matrix[i][j]=node;
			}
		for (int i=0; i<order; i++)
			tp.addLink(new Link(matrix[i][0], matrix[i][order-1]));

		for (int j=0; j<order; j++)
			tp.addLink(new Link(matrix[0][j], matrix[order-1][j]));
	}
	public static void generateKN(Topology topology, int nbNodes){
		double angle=Math.PI*2.0/nbNodes;
		int scale=100;
		for (int i=0; i<nbNodes; i++)
			topology.addNode(50 + scale + Math.cos(angle*i)*scale,
					50 + scale + Math.sin(angle*i)*scale,topology.newInstanceOfModel("default"));
		topology.setCommunicationRange(250);
	}
}
