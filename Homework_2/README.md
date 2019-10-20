# BlockChain
1901212691 胡逸凡
## 1.member summarize
1. member variable
    0.  public class BlockNode
        a self designed datastructure as a node of the block tree
        contain it's block, parent, utxoPool of in this stage(when it comes to this block).  
        ```java
        private class BlockNode {
        	public Block block;
        	public int h;
        	public UTXOPool utxoPool;
        	public BlockNode parent;
        	public ArrayList <BlockNode> children;
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
    1. public static final int CUT_OFF_AGE = 10;
    3. private HashMap<ByteArrayWrapper, BlockNode> blockChain;
        the dictionary memorize all the useful blocks
    4. private BlockNode maxHeightNode;
        remember the last block on the longest brench
    5. private TransactionPool txPool; 
        a global TransactionPool
    6. private int oldestBlockHeight;
        maintain a oldestBlockHeight to delete blocks that doesn't need to be memorize at the end of creating a new block
        ```java
        if (maxHeightNode.h - oldestBlockHeight >= 9) {
            for all aliveNode
            if(aliveNode.h <= maxHeightNode.h - 9){
                delete blocks 
            }
            oldestBlockHeight = maxHeightNode.h - 8;
        }
        ```
2. member methods
    1. constructer 
        
        init the members including utxoPool, txPool, blockChain(the dictoinary)
        add coinbase utxos into utxoPool
        create a blockNode with the given block(genesisBlock)
        ``` java
        BlockNode genesisNode = new BlockNode(genesisBlock, null, utxoPool);
        ```
        register into the blockChain dictionary
        ```java 
        ByteArrayWrapper wrappedGenesisHash = new ByteArrayWrapper(genesisBlock.getHash());
        blockChain.put(wrappedGenesisHash, genesisNode);
        ```
        maintain the maxHeightNode and oldestBlockHeight
    
    2. addBlock 
        
        get the parent node with the PrevBlockHash
        create a txHandler with the parents utxoPool
        handle TXs
        ```java
        Transaction[] validTxs = handler.handleTxs(blockTxs);
        ```
        check the length of current branch, cut off if too short
        put in coinbase into UTXOPool
        remove valid transactions from global txPool
        register in the new block 
        ```java
        BlockNode thisNewBlock = new BlockNode(block, parent, handler.getUTXOPool());
    	blockChain.put(new ByteArrayWrapper(block.getHash()), thisNewBlock);
    	```
    	maintain maxHNode
    	only keep the recent blocks, blocks more(equal) than 10 is a waste of memory
    3. public Block getMaxHeightBlock() 
        just return (trivial)
    4. public UTXOPool getMaxHeightUTXOPool() 
        just return (trivial)
    5. public int getOldestBlockHeight()
        just return (trivial)
    6. public TransactionPool getTransactionPool
        just return (trivial)
## 2. adjustment of the source code BlockHandler 
handleTxs before calling the API of BlockChain (addBlock) is redundent and pain in the ass (dealing with call with value and call with reference).
I deleted these lines and just put all txs into the new block no matter it's valid or not, the BlockChain class will handle it
## 3. Test case
1. testEmptyBlock()
    test constructer
    test easy making a empty block (no txs) with blockHandler.processBlock
2. testValidTx()
    test easy making a block with a tx with blockHandler.processBlock
3. testInalidTx()
test easy making a block with invalid txs(over spending and double spending) with blockHandler.processBlock
should return false

4. testPrevBlockHash()
test creating with a valid parent hash and a wrong hash

5. testCreateMultiBlocks()
    test creating contineuosly with the blockHandler.createBlock
    check if the class will handle the transactionPool correctly, the second block should contain zero txs because we created two blocks with out adding any new txs
    ```java 
    assertTrue("Failed: Second block after single tx",createdBlock.getTransactions().size() == 1 );
    assertTrue("Failed: Second block after single tx",createdBlock2.getTransactions().size() == 0 );
    ```
6. testCreateAfterProcess()
test if the new created block will behind the longest branch processed before
    ```java
    assertTrue(createdBlock.getPrevBlockHash().equals(block.getHash()) );
    ```
7. testMemoryMaintain()
    test if the block chain will delete useless blocks that is two old by checking the getOldestBlockHeight.
