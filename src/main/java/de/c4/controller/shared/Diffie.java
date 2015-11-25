package main.java.de.c4.controller.shared;

import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;

import main.java.de.c4.model.messages.PubKey;

public class Diffie {

	private static final String SECRET_KEY = "Sec";
	private static final String AGREE_KEY = "kAgree";
	private static DHParameterSpec PARAM_SPEC = null;
	static {
		try {
			AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
			paramGen.init(512);
			AlgorithmParameters params = paramGen.generateParameters();
			PARAM_SPEC = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static byte[] start(Connection c) throws Exception  {
		 /*
		 * Alice creates her own DH key pair, using the DH parameters from
		 * earlier code
		 */
//		 System.out.println("ALICE: Generate DH keypair ...");
		 KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
		 aliceKpairGen.initialize(PARAM_SPEC);
		 KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

		 // Alice creates and initializes her DH KeyAgreement object
//		 System.out.println("ALICE: Initialization ...");
		 KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
		 aliceKeyAgree.init(aliceKpair.getPrivate());
		 
		 c.getEndPoint().getKryo().getContext().put(AGREE_KEY, aliceKeyAgree);

		 // Alice encodes her public key, and sends it over to Bob.
		 byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
		 
		 c.sendTCP(new PubKey(alicePubKeyEnc));
		 
		 return alicePubKeyEnc;
		 
		 

	}

	@SuppressWarnings("unchecked")
	public static void finalize(Connection c, byte[] pubKey) throws Exception  {
		if(c.getEndPoint().getKryo().getContext().containsKey(AGREE_KEY)){
			alice2(c, pubKey);
		} else {
			bob(c, pubKey);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void alice2(Connection c, byte[] pubKey) throws Exception  {
		byte[] bobPubKeyEnc = pubKey;
		 
		 /*
		 * Alice uses Bob's public key for the first (and only) phase
		 * of her version of the DH
		 * protocol.
		 * Before she can do so, she has to instanticate a DH public key
		 * from Bob's encoded key material.
		 */
		 KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
		 PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
//		 System.out.println("ALICE: Execute PHASE1 ...");
		 KeyAgreement aliceKeyAgree = (KeyAgreement)c.getEndPoint().getKryo().getContext().get(AGREE_KEY);
		 aliceKeyAgree.doPhase(bobPubKey, true);

		
		 /*
		 * At this stage, both Alice and Bob have completed the DH key
		 * agreement protocol.
		 * Both generate the (same) shared secret.
		 */
		 byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
		 c.getEndPoint().getKryo().getContext().put(SECRET_KEY, aliceSharedSecret);;
	}

	@SuppressWarnings("unchecked")
	private static void bob(Connection c, byte[] pubKey) throws Exception {
		/*
		 * Let's turn over to Bob. Bob has received Alice's public key in
		 * encoded format. He instantiates a DH public key from the encoded key
		 * material.
		 */
		KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
		PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

		/*
		 * Bob gets the DH parameters associated with Alice's public key. He
		 * must use the same parameters when he generates his own key pair.
		 */
		DHParameterSpec dhParamSpec = ((DHPublicKey) alicePubKey).getParams();

		// Bob creates his own DH key pair
//		System.out.println("BOB: Generate DH keypair ...");
		KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
		bobKpairGen.initialize(dhParamSpec);
		KeyPair bobKpair = bobKpairGen.generateKeyPair();

		// Bob creates and initializes his DH KeyAgreement object
//		System.out.println("BOB: Initialization ...");
		KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
		bobKeyAgree.init(bobKpair.getPrivate());

		// Bob encodes his public key, and sends it over to Alice.
		byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();

		/*
		 * Bob uses Alice's public key for the first (and only) phase of his
		 * version of the DH protocol.
		 */
		// System.out.println("BOB: Execute PHASE1 ...");
		bobKeyAgree.doPhase(alicePubKey, true);

		byte[] bobSharedSecret = bobKeyAgree.generateSecret();
		c.getEndPoint().getKryo().getContext().put(SECRET_KEY, bobSharedSecret);
//		System.out.println("Bsecret: " + toHexString(bobSharedSecret));

		c.sendTCP(new PubKey(bobPubKeyEnc));
	}

	@SuppressWarnings("unchecked")
	public static byte[] getSecretKey(Kryo kryo) {
		return (byte[]) kryo.getContext().get(SECRET_KEY);
	}
	
	public static boolean isReady(Connection c) {
		return isReady(c.getEndPoint().getKryo());
	}
	
	@SuppressWarnings("unchecked")
	public static boolean isReady(Kryo kryo) {
		return kryo.getContext().containsKey(SECRET_KEY);
	}

	public static void wait(Client client) {
		try {
			while (!isReady(client)) {
				Thread.sleep(5);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Converts a byte to hex digit and writes to the supplied buffer
	 */

	/*
	 * Converts a byte array to hex string
	 */
//	private static String toHexString(byte[] block) {
//		StringBuffer buf = new StringBuffer();
//
//		int len = block.length;
//
//		for (int i = 0; i < len; i++) {
//			byte2hex(block[i], buf);
//			if (i < len - 1) {
//				buf.append(":");
//			}
//		}
//		return buf.toString();
//	}
}
