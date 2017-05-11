import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	boolean[] followees = null;
	Set<Transaction> set = null;
	Set<Integer> badNode = new HashSet<>();

	public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
		// IMPLEMENT THIS
	}

	public void setFollowees(boolean[] followees) {
		this.followees = followees;
	}

	public void setPendingTransaction(Set<Transaction> pendingTransactions) {
		set = pendingTransactions;
	}

	public Set<Transaction> sendToFollowers() {
		return set;
	}

	public void receiveFromFollowees(Set<Candidate> candidates) {
		Set<Integer> senderSet = candidates.stream().map(c -> c.sender).collect(Collectors.toSet());
		for (int i = 0; i < followees.length; i++) {
			if(followees[i]&&!senderSet.contains(i)){
				badNode.add(i);
			}
			if(!followees[i]&&senderSet.contains(i)){
				badNode.add(i);
			}
		}
		for (Candidate c : candidates) {
			if(!badNode.contains(c.sender))
				set.add(c.tx);
		}
	}
}
