import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;



public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool pool;
    public TxHandler(UTXOPool utxoPool) {
    	pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	ArrayList<Transaction.Input> inputs = tx.getInputs();
    	HashSet<UTXO> findDuplicate = new HashSet<>();
    	int index = -1;
    	double inputValue = 0;
    	double outputValue = 0;
    	for(Transaction.Input in : inputs){
    		index++;
    		UTXO unusedTxnOutput = new UTXO(in.prevTxHash, in.outputIndex);
    		if(!pool.contains(unusedTxnOutput)){
    			return false;
    		}
    		if(findDuplicate.contains(unusedTxnOutput)){
    			return false;
    		}
    		findDuplicate.add(unusedTxnOutput);
    		Transaction.Output lastOutput = pool.getTxOutput(unusedTxnOutput);
    		if(!Crypto.verifySignature(lastOutput.address, tx.getRawDataToSign(index), in.signature)){
    			return false;
    		}
    		inputValue+=lastOutput.value;
    	}
    	for(Transaction.Output output : tx.getOutputs()){
    		if(output.value < 0){
    			return false;
    		}
    		outputValue+=output.value;
    	}
    	if(outputValue>inputValue){
    		return false;
    	}
    	return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	ArrayList<Transaction> validTxn = new ArrayList<>();
    	for(Transaction tx : possibleTxs){
    		if(isValidTx(tx)){
    			validTxn.add(tx);
    			for(Transaction.Input in : tx.getInputs()){
    				UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
    				pool.removeUTXO(utxo);
    			}
    			for(int i=0;i<tx.getOutputs().size();i++){
    				Transaction.Output output = tx.getOutputs().get(i);
    				byte[] hash = tx.getHash();
    				pool.addUTXO(new UTXO(hash, i), output);
    			}
    		}
    	}
    	return validTxn.toArray(new Transaction[validTxn.size()]);
    }

}
