import java.util.ArrayList;
import java.util.Set;

public interface Node {

    // NOTE: Node is an interface and does not have a constructor.
    // However, your CompliantNode.java class requires a 4 argument
    // constructor as defined in Simulation.java

    /** {@code followees[i]} is true if and only if this node follows node {@code i} 
     * 设置每个node的所随从的节点*/
    void setFollowees(boolean[] followees);

    /** initialize proposal list of transactions 
     * 设置提议交易集合*/
    void setPendingTransaction(Set<Transaction> pendingTransactions);

    /**
     * @return proposals to send to my followers. REMEMBER: After final round, behavior of
     *         {@code getProposals} changes and it should return the transactions upon which
     *         consensus has been reached.
     *         得到发送给从节点的交易集合，注意：
     */
    Set<Transaction> sendToFollowers();

    /** receive candidates from other nodes. 
     * 参与者就是参与者id+其推送的交易，接收参与者信息*/
    void receiveFromFollowees(Set<Candidate> candidates);
}