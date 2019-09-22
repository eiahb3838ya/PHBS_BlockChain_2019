package test;

import static org.junit.Assert.*;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.Before;
import org.junit.BeforeClass;

import src.*;

import org.junit.Test;


public class TxHandlerTest {
	static KeyPair kpA,kpB;
	static KeyPairGenerator kpg;
	
	@BeforeClass
	public static void beforeClass() {
		
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
		    kpA = kpg.generateKeyPair();
		    kpB = kpg.generateKeyPair();
	    }
		catch(GeneralSecurityException e) {
	    	   throw new AssertionError(e);
	    }
		
		
	}
	
	
	@Test
	public void testTxHandler() {
		UTXOPool aPool = new UTXOPool();
		TxHandler handlerToTest = new TxHandler(aPool);
		
		assertNotNull(handlerToTest);
		assertTrue(handlerToTest instanceof TxHandler);
		
	}
	
	@Test
	public void testIsValidTxInCurUTXOPool() {
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		
//		UTXO is not in the curUTXOPool!!
//		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
//		init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	
//	    first testcase:t1
		Transaction t1 = new Transaction();
		t1.addInput(MadeUpTx.getHash(), 0);
		t1.addOutput(100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
//		sign for t1
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t1.getRawDataToSign(0));
            sig1 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(0).addSignature(sig1);
        t1.finalize();
        
/////////////////////////////////////debug    
//Transaction.Input firstInput = t1.getInputs().get(0); 
//byte[] inPrevHash = firstInput.prevTxHash;
//int inOututIndex = firstInput.outputIndex;
//
//System.out.println("inPrevHash"+inPrevHash);
//System.out.println("inOututIndex"+inOututIndex);
//
//int index = 0;
//UTXO toCheck = new UTXO(inPrevHash, inOututIndex);
//PublicKey pubKeyToCheck = handlerToTest.getUTXOPool().getTxOutput(toCheck).address;
//
//System.out.println("index"+index);
//System.out.println(t1.getRawDataToSign(index));
//System.out.println(firstInput.signature);
/////////////////////////////////////debug   
        
//      the pool does not contain the UTXO
		assertFalse(handlerToTest.isValidTx(t1));
		
//		init another TxHandler with the pool added utxo		
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
	    TxHandler BhandlerToTest = new TxHandler(pool);
	    
//      the pool now contain the UTXO
	    assertTrue(BhandlerToTest.isValidTx(t1));
	}
	
	@Test
	public void testIsValidTxSig() {
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
		
//		init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	
//	    first testcase:t1
		Transaction t1 = new Transaction();
		t1.addInput(MadeUpTx.getHash(), 0);
		t1.addOutput(100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
//		sign for t1 with wrong sig
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpB.getPrivate());
            sig.update(t1.getRawDataToSign(0));
            sig1 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(0).addSignature(sig1);
        t1.finalize();

		assertFalse(handlerToTest.isValidTx(t1));
		
	}
	

	@Test
	public void testIsValidTxDoubleSpend() {
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
		
//		init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	
//	    first testcase:t1
		Transaction t1 = new Transaction();
		
		t1.addInput(MadeUpTx.getHash(), 0);
		t1.addInput(MadeUpTx.getHash(), 0);
		
		t1.addOutput(100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
//		sign for t1 
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t1.getRawDataToSign(0));
            sig1 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(0).addSignature(sig1);
        
        byte[] sig2 = null;
        try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t1.getRawDataToSign(1));
            sig2 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(1).addSignature(sig2);
        t1.finalize();
        

		assertFalse(handlerToTest.isValidTx(t1));
		

	}
	

	@Test
	public void testIsValidOutputPos() {
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
		
//		init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	
//	    first testcase:t1
		Transaction t1 = new Transaction();
		t1.addInput(MadeUpTx.getHash(), 0);
		
//		some neg output here
		t1.addOutput(-100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
//		sign for t1 with wrong sig
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t1.getRawDataToSign(0));
            sig1 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(0).addSignature(sig1);
        t1.finalize();

		assertFalse(handlerToTest.isValidTx(t1));
		
	}


	@Test
	public void testIsValidTxSumOutput() {
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
		
		
//		init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	    
//	    first testcase:t1
		Transaction t1 = new Transaction();
		t1.addInput(MadeUpTx.getHash(), 0);
		t1.addOutput(100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
//		sign for t1
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t1.getRawDataToSign(0));
            sig1 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(0).addSignature(sig1);
        t1.finalize();

//      t2
        Transaction t2 = new Transaction();
        t2.addInput(MadeUpTx.getHash(), 0);
        t2.addOutput(500, kpB.getPublic());
        t2.addOutput(500, kpB.getPublic());
        t2.addOutput(100, kpB.getPublic());
		
//		sign for t2
		byte[] sig2 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t2.getRawDataToSign(0));
            sig2 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		t2.getInput(0).addSignature(sig2);
		t2.finalize();
		
		assertTrue(handlerToTest.isValidTx(t1));
		assertFalse(handlerToTest.isValidTx(t2));
	}
	
	@Test
	public void testHandleTxs() {
		
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
		
		
//		init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	    
//	    t1
		Transaction t1 = new Transaction();
		t1.addInput(MadeUpTx.getHash(), 0);
		t1.addOutput(100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
//		sign for t1
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t1.getRawDataToSign(0));
            sig1 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
        t1.getInput(0).addSignature(sig1);
        t1.finalize();
        
//        t2
        Transaction t2 = new Transaction();
        t2.addInput(MadeUpTx.getHash(), 0);
        t2.addOutput(500, kpB.getPublic());
        t2.addOutput(500, kpB.getPublic());
        t2.addOutput(100, kpB.getPublic());
		
//		sign for t2
		byte[] sig2 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(kpA.getPrivate());
            sig.update(t2.getRawDataToSign(0));
            sig2 = sig.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		t2.getInput(0).addSignature(sig2);
		t2.finalize();
		
		Transaction [] acceptedRx = handlerToTest.handleTxs(new Transaction[] { t1,t2 });
		assertEquals(acceptedRx.length, 1);
		
	}




	@Test
	public void testHandleTxsDepend() {
		
		Transaction MadeUpTx = new Transaction();
		MadeUpTx.addOutput(1000, kpA.getPublic());
		MadeUpTx.finalize();
	
		UTXOPool pool = new UTXOPool();
		UTXO utxo = new UTXO(MadeUpTx.getHash(), 0);
		pool.addUTXO(utxo, MadeUpTx.getOutput(0));
		
		
	//	init TxHandler with the pool above
	    TxHandler handlerToTest = new TxHandler(pool);
	    
	//    t1
		Transaction t1 = new Transaction();
		t1.addInput(MadeUpTx.getHash(), 0);
		t1.addOutput(100, kpB.getPublic());
		t1.addOutput(500, kpB.getPublic());
		t1.addOutput(100, kpB.getPublic());
		
	//	sign for t1
		byte[] sig1 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
	        sig.initSign(kpA.getPrivate());
	        sig.update(t1.getRawDataToSign(0));
	        sig1 = sig.sign();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
	    t1.getInput(0).addSignature(sig1);
	    t1.finalize();
	    
	//    t2
	    Transaction t2 = new Transaction();
	    t2.addInput(t1.getHash(), 1);
	    t2.addOutput(1, kpA.getPublic());
	    t2.addOutput(1, kpA.getPublic());
	    t2.addOutput(100, kpA.getPublic());
		
	//	sign for t2
		byte[] sig2 = null;
		try {
			Signature sig = Signature.getInstance("SHA256withRSA");
	        sig.initSign(kpB.getPrivate());
	        sig.update(t2.getRawDataToSign(0));
	        sig2 = sig.sign();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		t2.getInput(0).addSignature(sig2);
		t2.finalize();
		
		Transaction [] acceptedTx = handlerToTest.handleTxs(new Transaction[] { t1,t2 });
		assertEquals(acceptedTx.length, 2);
		
	}

}
