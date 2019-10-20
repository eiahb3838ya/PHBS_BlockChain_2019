package test;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import block_chain.Block;
import block_chain.BlockChain;
import block_chain.BlockHandler;
import block_chain.Transaction;
import block_chain.TxHandler;

class BlockChainHandlerTest {
	static KeyPair kpA,kpB, kpC;
	static KeyPairGenerator kpg;

	@BeforeAll
	static void setUpBeforeClass() {
			
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		    kpA = kpg.generateKeyPair();
		    kpB = kpg.generateKeyPair();
		    kpC = kpg.generateKeyPair();
	    }
		catch(GeneralSecurityException e) {
	    	   throw new AssertionError(e);
	    }
	}

	@Test
	void testEmptyBlock() {
		Block genesisBlock = new Block(null, kpA.getPublic());
        genesisBlock.finalize();

        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block = new Block(genesisBlock.getHash(), kpB.getPublic());
        block.finalize();

        assertTrue("Failed to process genesis block",blockHandler.processBlock(block));
//		fail("Not yet implemented");
	}
	@Test
	void testValidTx() {
		Block genesisBlock = new Block(null, kpA.getPublic());
        genesisBlock.finalize();

        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        
        Block block = new Block(genesisBlock.getHash(), kpB.getPublic());
        Transaction Tx1 = new Transaction();
        Tx1.addInput(genesisBlock.getCoinbase().getHash(), 0);
        
        Tx1.addOutput(10, kpA.getPublic());
        Tx1.addOutput(15, kpB.getPublic());
        
        
//        start sign a sig
        byte[] signed = null;
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.update(Tx1.getRawDataToSign(0));
            signed = sig.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
//      signed done;
        
        Tx1.addSignature(signed, 0);
        Tx1.finalize();
        block.addTransaction(Tx1);
        block.finalize();
        
        assertTrue("Could not process block with 1 tx.",blockHandler.processBlock(block));
		
	}
	
	@Test
	void testInalidTx() {
		Block genesisBlock = new Block(null, kpA.getPublic());
        genesisBlock.finalize();

        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        
        Block block1 = new Block(genesisBlock.getHash(), kpB.getPublic());
        
        Transaction Tx1 = new Transaction();
        Tx1.addInput(genesisBlock.getCoinbase().getHash(), 0);
        
        Tx1.addOutput(10, kpA.getPublic());
        Tx1.addOutput(25, kpB.getPublic());
        
        
//        start sign a sig
        byte[] signed = null;
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.update(Tx1.getRawDataToSign(0));
            signed = sig.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
//      signed done;
        
        Tx1.addSignature(signed, 0);
        Tx1.finalize();
        block1.addTransaction(Tx1);
        block1.finalize();
        
//      output>input invalid
        assertFalse("the tx was excepted", blockHandler.processBlock(block1));
        
        
        Block block2 = new Block(genesisBlock.getHash(), kpB.getPublic());
        
        //add a tx spending the coinbase
        Transaction Tx21 = new Transaction();
        Tx21.addInput(genesisBlock.getCoinbase().getHash(), 0);
        
        Tx21.addOutput(10, kpA.getPublic());
        Tx21.addOutput(10, kpB.getPublic());
        
	//      start sign a sig
		byte[] signed2 = null;
		Signature sig2 = null;
		try {
			sig2 = Signature.getInstance("SHA256withRSA");
		    sig2.initSign(kpA.getPrivate());
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
		    e.printStackTrace();
		}
		try {
		    sig2.update(Tx21.getRawDataToSign(0));
		    signed2 = sig2.sign();
		} catch (SignatureException e) {
		    e.printStackTrace();
		}
//    signed done;
  
	    Tx21.addSignature(signed2, 0);
	    Tx21.finalize();
	    block2.addTransaction(Tx21);
	      
	    //add another tx also spending the coinbase
	    Transaction Tx22 = new Transaction();
	    Tx22.addInput(genesisBlock.getCoinbase().getHash(), 0);
		
	    Tx22.addOutput(10, kpA.getPublic());
	    Tx22.addOutput(10, kpB.getPublic());
		
		//      start sign a sig
		  byte[] signed3 = null;
		  Signature sig3 = null;
		  try {
			  sig3 = Signature.getInstance("SHA256withRSA");
		      sig3.initSign(kpA.getPrivate());
		  } catch (InvalidKeyException | NoSuchAlgorithmException e) {
		      e.printStackTrace();
		  }
		  try {
		      sig3.update(Tx22.getRawDataToSign(0));
		      signed3 = sig3.sign();
		  } catch (SignatureException e) {
		      e.printStackTrace();
		  }
		//    signed done;
		  
		  Tx22.addSignature(signed3, 0);
		  Tx22.finalize();
		  block2.addTransaction(Tx22);
		  block2.finalize();
		  assertFalse("Invalid prevBlockHash not detected.",blockHandler.processBlock(block2));
		
	}
	
	@Test
	public void testPrevBlockHash() {
	        
	
		Block genesisBlock = new Block(null, kpA.getPublic());
		genesisBlock.finalize();
		
		BlockChain blockChain = new BlockChain(genesisBlock);
		BlockHandler blockHandler = new BlockHandler(blockChain);
		
		byte[] hash = genesisBlock.getHash();
		byte[] hashCopy = Arrays.copyOf(hash, hash.length);
		hashCopy[0]++;
		
		//process with a wrong hash
		Block block1 = new Block(hashCopy, kpB.getPublic());
		block1.finalize();
		assertFalse("Invalid prevBlockHash not detected.",blockHandler.processBlock(block1));
		
		//process with a correct hash
		Block block2 = new Block(hash, kpB.getPublic());
		block2.finalize();
		assertTrue("Invalid prevBlockHash detected.",blockHandler.processBlock(block2));
	}
	
	@Test
	public void testCreateMultiBlocks() {


		
//		create a genesisBlock and get the handler
		Block genesisBlock = new Block(null, kpA.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        
//        create a valid Tx
        Transaction Tx1 = new Transaction();
        Tx1.addInput(genesisBlock.getCoinbase().getHash(), 0);
        Tx1.addOutput(10, kpA.getPublic());
        Tx1.addOutput(15, kpB.getPublic());
        //start sign a sig
        byte[] signed = null;
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.update(Tx1.getRawDataToSign(0));
            signed = sig.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        //signed done;
        
//        everything prepared, add it into TxPool
        Tx1.addSignature(signed, 0);
        Tx1.finalize();
        blockHandler.processTx(Tx1);
       
        /////////////////////////////////////////////////////
        //debug use 
        //prove Tx1 valid
        //TxHandler testUse = new TxHandler(blockChain.getMaxHeightUTXOPool());
        //System.out.println(testUse.isValidTx(blockChain.getTransactionPool().getTransaction(Tx1.getHash())));
        /////////////////////////////////////////////////////
       
//        create block using Tx added above 
        Block createdBlock = blockHandler.createBlock(kpB.getPublic());
        
//        there should be no more Txs in TxPool, this new block should be empty
        Block createdBlock2 = blockHandler.createBlock(kpC.getPublic());
      
        assertTrue("Failed: Second block after single tx",createdBlock != null );
        assertTrue("Failed: Second block after single tx",createdBlock.getPrevBlockHash().equals(genesisBlock.getHash()) );
        assertTrue("Failed: Second block after single tx",createdBlock.getTransactions().size() == 1 );
        assertTrue("Failed: Second block after single tx",createdBlock.getTransaction(0).equals(Tx1) );
        assertTrue("Failed: Second block after single tx",createdBlock2.getPrevBlockHash().equals(createdBlock.getHash()));
        assertTrue("Failed: Second block after single tx",createdBlock2.getTransactions().size() == 0 );
        
        
        
	}
	
	@Test
	public void testCreateAfterProcess() {
//		create a genesisBlock and get the handler
		Block genesisBlock = new Block(null, kpA.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        
        Block block = new Block(genesisBlock.getHash(), kpB.getPublic());
        Transaction Tx1 = new Transaction();
        Tx1.addInput(genesisBlock.getCoinbase().getHash(), 0);
        
        Tx1.addOutput(10, kpA.getPublic());
        Tx1.addOutput(15, kpB.getPublic());
        
//        start sign a sig
        byte[] signed = null;
        Signature sig = null;
        try {
        	sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.update(Tx1.getRawDataToSign(0));
            signed = sig.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
//      signed done;
        
        Tx1.addSignature(signed, 0);
        Tx1.finalize();
        block.addTransaction(Tx1);
        block.finalize();
        
        assertTrue("Could not process block with 1 tx.",blockHandler.processBlock(block));


        Block createdBlock = blockHandler.createBlock(kpA.getPublic());

        assertTrue("Null created block",createdBlock != null);
        assertTrue("Created block parent is not 1st block",
                createdBlock.getPrevBlockHash().equals(block.getHash()) );
        assertTrue("Created block should not have transactions",createdBlock.getTransactions().size() == 0);
    }
	
	@Test
	void testMemoryMaintain() {
		
		int APPEND_BLOCK_COUNT = 30;
		
		Block genesisBlock = new Block(null, kpA.getPublic());
        genesisBlock.finalize();

        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        
        for (int i=0;i<APPEND_BLOCK_COUNT;i++) {  	
        	Block createdBlock = blockHandler.createBlock(kpB.getPublic());
        }
     

        assertTrue("Failed to process genesis block",blockChain.getOldestBlockHeight() == APPEND_BLOCK_COUNT+1-8);
//		fail("Not yet implemented");
	}
	

}
