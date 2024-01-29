package sr.wat.edu.pl.core.sys;

public class Node {
    public enum NodeState {
        NOT_READY,
        READY,
        IDLE,
        WAITING,
        WORKING
    }


    private int id;
    private NodeState state;

    public Node(int id) {
        this.id = id;
        state = NodeState.NOT_READY;
    }

    public Node(int id, NodeState state) {
        this.id = id;
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("Node[%d]", id);
    }


    public int getId() {
        return id;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }
}
