package block_chain;
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private int oldestBlockHeight;
    
    private class BlockNode {
    	public Block block;
    	public int h;
    	public UTXOPool utxoPool;
    	
    	public BlockNode parent;
    	public ArrayList <BlockNode> children;
    	
        
    	
    	//constructor
    	public BlockNode(Block block, BlockNode parent, UTXOPool utxoPool) {
    		this.block = block ;
    		this.parent = parent ;
    		this.utxoPool = utxoPool;
    		this.children = new ArrayList<>();
    		if(parent == null) {
    			this.h = 1;
    		}
    		else {
    			this.h = parent.h+1 ;
    			this.parent.children.add(this);
    		}
    	}
    }
    
    
    private HashMap<ByteArrayWrapper, BlockNode> blockChain;
    private BlockNode maxHeightNode;
    private TransactionPool txPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
    	
    	blockChain = new HashMap<>();
        UTXOPool utxoPool = new UTXOPool();
        txPool = new TransactionPool();
        
        
        Transaction coinbase = genesisBlock.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output out = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, out);
        }
        
        BlockNode genesisNode = new BlockNode(genesisBlock, null, utxoPool);
        ByteArrayWrapper wrappedGenesisHash = new ByteArrayWrapper(genesisBlock.getHash());
        
        blockChain.put(wrappedGenesisHash, genesisNode);
        maxHeightNode = genesisNode;
        this.oldestBlockHeight = 1;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
    	
    	return maxHeightNode.block;
        // IMPLEMENT THIS
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
    	return maxHeightNode.utxoPool;
        // IMPLEMENT THIS
    }
    
    public int getOldestBlockHeight() {
    	return oldestBlockHeight;
        // IMPLEMENT THIS
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
    	return(txPool);
    	
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
    	byte [] prevBlockHash = block.getPrevBlockHash();
    	if (prevBlockHash == null) {
    		System.out.println("prevBlockHash");
    		return(false);
    	}
    	ByteArrayWrapper wrappedPrevHash = new ByteArrayWrapper(prevBlockHash);
    	BlockNode parent = this.blockChain.get(wrappedPrevHash);
    	if (parent == null) {
    		System.out.println("parent is null");
    		return(false);
    	}
    	
    	
    	//handle the transactions
    	TxHandler handler = new TxHandler(parent.utxoPool);
    	
    	//debug
//    	  System.out.println("parent.utxoPool in chain");
//        System.out.println(parent.utxoPool);
//        System.out.println(parent.utxoPool.getAllUTXO());
    	
    	Transaction[] blockTxs = new Transaction[block.getTransactions().size()];
    	
//    	System.out.println("block.getTransactions()");
//    	System.out.println(block.getTransactions());
    	
        for (int i = 0; i < block.getTransactions().size(); i++) {
            blockTxs[i] = block.getTransaction(i);
        }
                   
        Transaction[] validTxs = handler.handleTxs(blockTxs);
        if (validTxs.length != blockTxs.length) {
        	System.out.println("Tx invalid");
            return (false);
        }
               
        if(parent.h + 1 <=maxHeightNode.h - CUT_OFF_AGE) {
        	System.out.println("cut_off_age");
        	return (false);
        }
    	   
        //put in coinbase into UTXOPool
    	for(int i = 0; i < block.getCoinbase().getOutputs().size();i++) {
    		UTXO coinbaseUTXO = new UTXO(block.getCoinbase().getHash(), i);
    		handler.getUTXOPool().addUTXO(coinbaseUTXO, block.getCoinbase().getOutput(i));
    	}
    	
    	//remove transactions from global txPool
    	List<Transaction> transactions = block.getTransactions();
        for (Transaction transaction: transactions) {
        	txPool.removeTransaction(transaction.getHash());
        }
        
    	//put in the new block 
    	BlockNode thisNewBlock = new BlockNode(block, parent, handler.getUTXOPool());
    	blockChain.put(new ByteArrayWrapper(block.getHash()), thisNewBlock);
    	
    	//maintain maxHNode
    	if(parent.h+1 >maxHeightNode.h) {
    		maxHeightNode = thisNewBlock;
    	}
    	
    	
    	// only keep the recent blocks
    	// blocks more(equal) than 10 is a waste of memory
        if (maxHeightNode.h - oldestBlockHeight >= 9) {
            Iterator<ByteArrayWrapper> aliveNodesIter = blockChain.keySet().iterator();
            while (aliveNodesIter.hasNext()) {
                ByteArrayWrapper key = aliveNodesIter.next();
                BlockNode aliveNode = blockChain.get(key);
                if (aliveNode.h <= maxHeightNode.h - 9) {
                	aliveNodesIter.remove();
                }
            }
            oldestBlockHeight = maxHeightNode.h - 8;
        }
    		
    	return(true);
    	
    	
    	
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
    	txPool.addTransaction(tx);
    }
}