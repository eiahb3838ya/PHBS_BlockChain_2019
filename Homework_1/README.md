# ScroogeCoin
1901212691 胡逸凡
![GitHub Logo](https://raw.githubusercontent.com/sasikumar-sugumar/scrooge-coin/master/scroogecoin.gif)
## 1.method summarize
1. constructor
    do the make a defensive copy by setting the utxoPool a private object, and creat a new copy when call getutxoPool
    ```java
    private  UTXOPool curUTXOPool;
    public TxHandler(UTXOPool utxoPool) {
    	this.curUTXOPool = utxoPool;
    }
    public UTXOPool getUTXOPool() {
    	return(new UTXOPool(curUTXOPool));
    }
    ```
2. isValidTx
    1. for each input pull out the prevTxHash and the outputIndex to create a UTXO and check if it is in the pool
        ```java
        byte[] inPrevHash = txIn.prevTxHash;
        int inOututIndex = txIn.outputIndex;
        UTXO toCheck = new UTXO(inPrevHash, inOututIndex);
        ```
    2. verifySignature with the class Crypto
    3. obtain a seen UTXO set and check if any UTXO is claimed multiple times 
        ```java 
        if (!uSet.contains(toCheck)) {
            uSet.add(toCheck);
            totalInValue += this.curUTXOPool.getTxOutput(toCheck).value;
        }
        ```
    4. go through all the outputs, if the output greater than 0, add it into total value
    5. check the sum of output and the sum of input
3. handleTxs
    1. for each aTran in possibleTxsList check with isValid
    2. if so delete the inputs from the curUTXOPool
        ```java
    	for(Transaction.Input txIn:aTran.getInputs()) {
        	byte[] inPrevHash = txIn.prevTxHash;
        	int inOututIndex = txIn.outputIndex;
        	UTXO toDelUtxo = new UTXO(inPrevHash, inOututIndex);
        	this.curUTXOPool.removeUTXO(toDelUtxo);
        }
    3. add the outputs into the curUTXOPool
        ```java
        for (Transaction.Output txOut : aTran.getOutputs()) {
			UTXO utxo = new UTXO(aTranHash, index);        				
			this.curUTXOPool.addUTXO(utxo,txOut);
			index++;
		}
		```
	4. add the valid tx into validT and remove from the possibleTxList
	    ```java
    	validT.add(aTran);
		toRemove.add(aTran);
		```
	5. after go through all the possibleTxList check if the size of it has decreased in the process, if so do the process again.
	This means there are some change in the UTXOPool and might have different result in the method isValid.
	6. return the validT Array
# Test case
1. TxHandler
   1. testTxHandler
    test the consructor of the class
2. isValidTx
   1. testIsValidTxInCurUTXOPool
    test if the method can varify the UTXO is in the UTXOPool, do it with no UTXO in the UTXOPool at first, and add one in the end.
   2. testIsValidTxSig
    test if the method can varify the validation of the sig, do it with a wrong sig.
   3. testIsValidTxDoubleSpend
   test if the method can varify double spending, do it with spending the same input UXTO.
   assertFalse
   4. testIsValidOutputPos
   test if the method can varify double spending, do it with spending the same input UXTO.
   5. testIsValidTxSumOutput
   test if the method can varify over spending, do it with spending too much.
   
3. handleTxs
   1. testHandleTxs
   simply add a valid Tx and another that isnt valid
   2. testHandleTxsDepend
   put two Tx that one is spending another's input, and see if the method can handle Tx that depends on each other
