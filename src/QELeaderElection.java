import java.awt.Color;
import java.util.Random;
import jbotsim.Message;
import jbotsim.Node;
import jbotsim.Topology;
import jbotsim.ui.JViewer;
import jbotsimx.messaging.AsyncMessageEngine;
import jbotsimx.topology.TopologyGenerator;

/**
 *
 * A QELeaderElection for Jbotsim
 *
 * @author giuseppe
 */
public class QELeaderElection extends Node {

    int maxid = 0;
    boolean alive = true;
    boolean firstclock = true;

    public QELeaderElection() {

        this.disableWireless();
    }

    public QELeaderElection(int id) {
        this.setID(id);
        this.disableWireless();
        maxid = this.getID();
    }

    @Override
    public void setID(int id) {
        super.setID(id);
        maxid = this.getID();
    }

    @Override
    public void onClock() {
       
        if (firstclock) {
             System.out.println("My ID:"+this.getID()+" Clock");
            Message m = new Message(new Integer(this.getID()));
            System.out.println(this.getNeighbors());
            this.sendAll(m);
            firstclock = false;
        }
    }

    @Override
    public void onMessage(Message message) {
        System.out.println("My ID:"+this.getID()+" RCVD:"+message.toString());
        int other_id = ((Integer) message.getContent()).intValue();
        if (maxid > other_id) {
            return;
        }
        maxid = other_id;
        if (maxid != this.getID()) {
            alive = false;
            this.setState("Follower");
            this.setColor(Color.black);
        }
        if (other_id == this.getID()) {
            this.setState("Leader");
            this.setColor(Color.red);
        }
        Node sender = message.getSender();
        for (Node n : this.getNeighbors()) {
            if (n != sender) {
                this.send(n, message);
            }
        }
    }

    public static void main(String args[]) {
        Topology tpg = new Topology();
        Random rnd = new Random();
        tpg.setDefaultNodeModel(QELeaderElection.class);
        int size = 25;
        TopologyGenerator.generateRing(tpg, size, false);
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

        tpg.setMessageEngine(new AsyncMessageEngine(size * 100, AsyncMessageEngine.Type.FIFO));
        tpg.setClockSpeed(1,0);
        new JViewer(tpg);
        tpg.start();
    }

}