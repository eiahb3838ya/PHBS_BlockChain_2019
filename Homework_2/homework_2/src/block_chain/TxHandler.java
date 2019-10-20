package block_chain;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private  UTXOPool curUTXOPool;
	
    public TxHandler(UTXOPool utxoPool) {
    	this.curUTXOPool = utxoPool;
        // IMPLEMENT THIS
    }
    public UTXOPool getUTXOPool() {
    	return(new UTXOPool(curUTXOPool));
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
    	double totalOutValue = 0;
    	double totalInValue = 0;
    	HashSet<UTXO> uSet = new HashSet<UTXO>();
//    	int index = 0;
    	int inputSize = tx.getInputs().size();
        for(int index = 0;index<inputSize;index ++) {
        	Transaction.Input txIn = tx.getInput(index);
        	byte[] inPrevHash = txIn.prevTxHash;
        	int inOututIndex = txIn.outputIndex;
        	
//        	System.out.println("inPrevHash"+inPrevHash);
//        	System.out.println("inOututIndex"+inOututIndex);
        	
        	UTXO toCheck = new UTXO(inPrevHash, inOututIndex);
        	//(1)
        	if(!this.curUTXOPool.contains(toCheck)) {
        		System.out.println("(1)");
        		return false;
        	}
        	//(2)
        	PublicKey pubKeyToCheck = this.curUTXOPool.getTxOutput(toCheck).address;
        	if(Crypto.verifySignature(pubKeyToCheck, tx.getRawDataToSign(index), txIn.signature)==false){ 
        		
//        		System.out.println("index"+index);
//        		System.out.println(tx.getRawDataToSign(index));
//        		System.out.println(txIn.signature);
//        		System.out.println(Crypto.verifySignature(pubKeyToCheck, tx.getRawDataToSign(index), txIn.signature));
//        		
        		System.out.println("(2)");
        		return false;
			}
        	
        	if (!uSet.contains(toCheck)) {
        		uSet.add(toCheck);
        		totalInValue += this.curUTXOPool.getTxOutput(toCheck).value;
        	}
        	else {
        		System.out.println("(3)");
        		return(false);
        	}
        }
        for(Transaction.Output txout:tx.getOutputs()) {
        	if(txout.value>=0) {
        		totalOutValue +=txout.value;
        	}
        	else {
        		System.out.println("(4)");
        		return(false);
        	}
        }
        if (totalOutValue>totalInValue) {
        	System.out.println("(5)");
        	return(false);
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        boolean checkValid = true;
        ArrayList<Transaction> possibleTxsList =  new ArrayList<Transaction>(Arrays.asList(possibleTxs));
        ArrayList<Transaction> validT = new ArrayList<Transaction>();
        while(checkValid) {
        	int initSize = possibleTxsList.size();
        	List<Transaction> toRemove = new ArrayList<Transaction>();
        	for(Transaction aTran: possibleTxsList) {
        		if(isValidTx(aTran)){
        			//handle the UTXOPool
        			int index = 0;
        			byte[] aTranHash = aTran.getHash();
        			
//        			delete the inputs
        			for(Transaction.Input txIn:aTran.getInputs()) {
        	        	byte[] inPrevHash = txIn.prevTxHash;
        	        	int inOututIndex = txIn.outputIndex;
        	        	UTXO toDelUtxo = new UTXO(inPrevHash, inOututIndex);
        	        	this.curUTXOPool.removeUTXO(toDelUtxo);
        	        }
        			
//        			add the outputs
        			for (Transaction.Output txOut : aTran.getOutputs()) {
        				UTXO utxo = new UTXO(aTranHash, index);        				
        				this.curUTXOPool.addUTXO(utxo,txOut);
        				index++;
        			}
        			
        			
        			validT.add(aTran);
        			toRemove.add(aTran);
//        			possibleTxsList.remove(aTran);
        		}
        	}
        	possibleTxsList.removeAll(toRemove);
//        	check if there is a minus and if there is still TX in possibleTxsList
        	checkValid = possibleTxsList.size() < initSize && possibleTxsList.size()!=0;
        }
        return validT.toArray(new Transaction[validT.size()]);
    }


}
