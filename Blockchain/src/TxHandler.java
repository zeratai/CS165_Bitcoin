import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	private UTXOPool uPool;
	
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS
		uPool = new UTXOPool (utxoPool);
	}

	public UTXOPool getUTXOPool() {
		return uPool;
	}
	
	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS
		
		double inputSum = 0;
		double outputSum = 0;
		
		ArrayList<UTXO> utxo = uPool.getAllUTXO();
		ArrayList<UTXO> existingUtxo = new ArrayList<UTXO>();
		
		for(int i = 0; i < tx.numInputs(); i++) {
			UTXO utxoTemp = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
			
			// Search if all outputs claimed by tx are in the current UTXO pool
			
			if( !uPool.contains(utxoTemp)) {
				return false;
			}
			
			inputSum += uPool.getTxOutput(utxoTemp).value;
			
			// Verify that signatures on each input of tx are valid
			RSAKey utxoKey = uPool.getTxOutput(utxoTemp).address;
			
			if( !utxoKey.verifySignature(tx.getRawDataToSign(i), tx.getInput(i).signature) ) {
				return false;
			}
			
			//System.out.println("rawDataToSign: " + tx.getRawDataToSign(i) + " tx.getInput signature: " + tx.getInput(i).signature);
			// Make sure no two UTXO are claimed multiple times by tx
			if(existingUtxo.contains(utxoTemp)) {
				return false;
			}
			
			existingUtxo.add(utxoTemp);
		}
		
		// Check to make sure all output values are non-negative
		
		for(int i = 0; i < tx.numOutputs(); i++) {
			if(tx.getOutput(i).value < 0) {
				return false;
			}
			outputSum += tx.getOutput(i).value;
		}
		
		// Make sure that the input sum is greater than the output sum otherwise return false
		if(outputSum > inputSum) {
			return false;
		}
		
		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS
		ArrayList<Transaction> checkedTransaction = new ArrayList<Transaction>();
		
		for(int i = 0; i < possibleTxs.length; i++) {
			if(isValidTx(possibleTxs[i])) {
				// Get rid of the old UTXOs from our uPool
				for(int k = 0; k < possibleTxs[i].numInputs(); k++) {
					UTXO removeUTXO = new UTXO(possibleTxs[i].getInput(k).prevTxHash, possibleTxs[i].getInput(k).outputIndex);
					uPool.removeUTXO(removeUTXO);
				}
				
				// add new UTXOs to the previous pool
				for(int j = 0; j < possibleTxs[i].numOutputs(); j++) {
					UTXO nUtxo = new UTXO(possibleTxs[i].getHash(), j);
					uPool.addUTXO(nUtxo, possibleTxs[i].getOutputs().get(j));
				}
				
				checkedTransaction.add(possibleTxs[i]);
				
			}
		}
		
		Transaction[] newTxs = new Transaction[checkedTransaction.size()];
		
		newTxs = checkedTransaction.toArray(newTxs);
		return newTxs;
		
	}

} 