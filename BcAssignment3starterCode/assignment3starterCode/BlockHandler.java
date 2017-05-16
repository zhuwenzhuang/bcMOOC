
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import sun.security.rsa.RSAPublicKeyImpl;

public class BlockHandler {
	private BlockChain blockChain;

	/** assume blockChain has the genesis block */
	public BlockHandler(BlockChain blockChain) {
		this.blockChain = blockChain;
	}

	public static final String KEY_ALGORITHM = "RSA";

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		kpg.initialize(1024);// 1024?
		KeyPair kp = kpg.generateKeyPair();

		RSAPublicKey pk = (RSAPublicKey) kp.getPublic();
		RSAPrivateKey sk = (RSAPrivateKey) kp.getPrivate();
		System.out.println(pk + "\n" + sk);
		{
			Cipher cipher = null;
			try {
				cipher = Cipher.getInstance(KEY_ALGORITHM);
				cipher.init(cipher.ENCRYPT_MODE, pk);// 公钥、私钥，加密、解密都可以
				byte[] out = cipher.doFinal(new byte[10]);
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
		}

		Block b = new Block(null, pk);
		BlockChain bc = new BlockChain(b);
		BlockHandler bh = new BlockHandler(bc);
		Transaction t = new Transaction();
		t.addInput(b.getCoinbase().getHash(), 0);
		t.addOutput(25, pk);
		t.addSignature(Crypto.signature(sk, t.getRawDataToSign(0)), 0);
		t.finalize();
		bh.processTx(t);
		b = bh.createBlock(pk);
		bh.processBlock(b);
	}

	/**
	 * add {@code block} to the block chain if it is valid.
	 * 
	 * @return true if the block is valid and has been added, false otherwise
	 */
	public boolean processBlock(Block block) {
		if (block == null)
			return false;
		return blockChain.addBlock(block);
	}

	/** create a new {@code block} over the max height {@code block} */
	public Block createBlock(PublicKey myAddress) {
		Block parent = blockChain.getMaxHeightBlock();
		byte[] parentHash = parent.getHash();
		Block current = new Block(parentHash, myAddress);
		UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
		TransactionPool txPool = blockChain.getTransactionPool();
		TxHandler handler = new TxHandler(uPool);
		Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
		Transaction[] rTxs = handler.handleTxs(txs);
		for (int i = 0; i < rTxs.length; i++)
			current.addTransaction(rTxs[i]);

		current.finalize();
		if (blockChain.addBlock(current))
			return current;
		else
			return null;
	}

	/** process a {@code Transaction} */
	public void processTx(Transaction tx) {
		blockChain.addTransaction(tx);
	}
}
