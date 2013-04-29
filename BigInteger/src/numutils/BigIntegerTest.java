package numutils;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class BigIntegerTest {

	@Test
	public void testAdd() {
		//special cases
		BigInteger b1, b2;
		java.math.BigInteger r1, r2;
		b1 = new BigInteger("0");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.add(b2).toString(), h);
		}
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b1 = new BigInteger(h);
			b2 = new BigInteger(h);
			assertEquals(b1.add(b2).toString(), Integer.toHexString(i + i));
		}
		//brute force
		for (int i = 0; i < 100; i++) {
			b1 = BigInteger.random(1024, null);
			b2 = BigInteger.random(1024, null);
			r1 = new java.math.BigInteger(b1.toString(), 16);
			r2 = new java.math.BigInteger(b2.toString(), 16);
			assertEquals(b1.add(b2).toString(), r1.add(r2).toString(16));
		}
	}
	
	@Test
	public void testSubtract() {
		//special cases
		BigInteger b1, b2;
		java.math.BigInteger r1, r2;
		b2 = new BigInteger("0");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b1 = new BigInteger(h);
			assertEquals(b1.subtract(b2).toString(), h);
		}
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b1 = new BigInteger(h);
			b2 = new BigInteger(h);
			assertEquals(b1.subtract(b2).toString(), "0");
		}
		//brute force
		for (int i = 0; i < 100; i++) {
			b1 = BigInteger.random(1024, null);
			b2 = BigInteger.random(1024, null);
			if (b1.compareTo(b2) < 0) {
				BigInteger t = b1;
				b1 = b2;
				b2 = t;
			}
			r1 = new java.math.BigInteger(b1.toString(), 16);
			r2 = new java.math.BigInteger(b2.toString(), 16);
			assertEquals(b1.subtract(b2).toString(), r1.subtract(r2).toString(16));
		}
	}
	
	@Test
	public void testQuadMultiply() {
		//special cases
		BigInteger b1, b2;
		java.math.BigInteger r1, r2;
		b1 = new BigInteger("1");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.quadMultiply(b2).toString(), h);
		}
		b1 = new BigInteger("0");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.quadMultiply(b2).toString(), "0");
		}
		//brute force
		for (int i = 0; i < 100; i++) {
			b1 = BigInteger.random(128, null);
			b2 = BigInteger.random(128, null);
			r1 = new java.math.BigInteger(b1.toString(), 16);
			r2 = new java.math.BigInteger(b2.toString(), 16);
			assertEquals(b1.quadMultiply(b2).toString(), r1.multiply(r2).toString(16));
		}
	}
	
	@Test
	public void testQuadDivision() {
		String s1 = "44637aabbbeeabdaabbeedd312a";
		String s2 = "bbeaabaaeeebeeddab";
		String s3 = "ffffffffffaab331afbe";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test2 = new BigInteger(s2);
		BigInteger test3 = new BigInteger(s3);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r2 = new java.math.BigInteger(s2, 16);
		java.math.BigInteger r3 = new java.math.BigInteger(s3, 16);
		BigInteger[] result1 = test1.quadDivision(test2);
		BigInteger[] result2 = test1.quadDivision(test3);
		java.math.BigInteger[] compare1 = r1.divideAndRemainder(r2);
		java.math.BigInteger[] compare2 = r1.divideAndRemainder(r3);
		assertEquals(compare1[0].toString(16), result1[0].toString());
		assertEquals(compare1[1].toString(16), result1[1].toString());
		assertEquals(compare2[0].toString(16), result2[0].toString());
		assertEquals(compare2[1].toString(16), result2[1].toString());
	}
	
	@Test
	public void testModAdd() {
		String s1 = "abeda2526aebbeeee3a3aafff34a312a";
		String s2 = "1";
		String s5 = "b339bca9";
		String s6 = "aabe21cb";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test2 = new BigInteger(s2);
		BigInteger test5 = new BigInteger(s5);
		BigInteger test6 = new BigInteger(s6);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r2 = new java.math.BigInteger(s2, 16);
		java.math.BigInteger r5 = new java.math.BigInteger(s5, 16);
		java.math.BigInteger r6 = new java.math.BigInteger(s6, 16);
		assertEquals(r1.add(r2).mod(r5).toString(16), test1.modAdd(test2, test5).toString());
		assertEquals(r6.add(r2).mod(r5).toString(16), test6.modAdd(test2, test5).toString());
	}

	@Test
	public void testModQuadMultiply() {
		String s1 = "abeda2526aebbee773eed221abbe2b3b4eaee3a3aafff34a312a";
		String s3 = "ffffaab331ddde443fbe";
		String s4 = "1";
		String s5 = "b339bca9";
		String s6 = "aabe21cb";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test3 = new BigInteger(s3);
		BigInteger test4 = new BigInteger(s4);
		BigInteger test5 = new BigInteger(s5);
		BigInteger test6 = new BigInteger(s6);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r3 = new java.math.BigInteger(s3, 16);
		java.math.BigInteger r4 = new java.math.BigInteger(s4, 16);
		java.math.BigInteger r5 = new java.math.BigInteger(s5, 16);
		java.math.BigInteger r6 = new java.math.BigInteger(s6, 16);
		assertEquals(r1.multiply(r3).mod(r6).toString(16), test1.modQuadMultiply(test3, test6).toString());
		assertEquals(r5.multiply(r6).mod(r4).toString(16), test5.modQuadMultiply(test6, test4).toString());
	}
	
	@Test
	public void testModExp() {
		String s1 = "abe2233dbe2123";
		String s2 = "aab34eefb42";
		String s3 = "5542aaaaaaaaaaabbee3123";
		String s4 = "1";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test2 = new BigInteger(s2);
		BigInteger test3 = new BigInteger(s3);
		BigInteger test4 = new BigInteger(s4);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r2 = new java.math.BigInteger(s2, 16);
		java.math.BigInteger r3 = new java.math.BigInteger(s3, 16);
		java.math.BigInteger r4 = new java.math.BigInteger(s4, 16);
		assertEquals(r1.modPow(r3, r2).toString(16), test1.modExp(test3, test2).toString());
		assertEquals(r3.modPow(r2, r1).toString(16), test3.modExp(test2, test1).toString());
		assertEquals(r4.modPow(new java.math.BigInteger("2"), r2).toString(16), test4.modExp(new BigInteger("2"), test2).toString());
	}

	@Test
	public void testEuclidOnlyGCD() {
		String s1 = "abeaaeebba34b2cb123deadbe2123";
		String s2 = "aab34aaaeeefadb42";
		String s3 = "55ddeb42aaaaaaaaaaabbee3123";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test2 = new BigInteger(s2);
		BigInteger test3 = new BigInteger(s3);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r2 = new java.math.BigInteger(s2, 16);
		java.math.BigInteger r3 = new java.math.BigInteger(s3, 16);
		assertEquals(r1.gcd(r2).toString(16), test1.euclid(test2)[2].toString());
		assertEquals(r1.gcd(r3).toString(16), test1.euclid(test3)[2].toString());
	}

	@Test
	public void testModInverse() {
		String s1 = "3";
		String s2 = "1c";
		String s3 = "2";
		String s4 = "6";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test2 = new BigInteger(s2);
		BigInteger test3 = new BigInteger(s3);
		BigInteger test4 = new BigInteger(s4);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r2 = new java.math.BigInteger(s2, 16);
		assertEquals(r1.modInverse(r2).toString(16), test1.modInverse(test2).toString());
		try {
			test3.modInverse(test4).toString();
		} catch (NotInvertibleException e) {
			assertTrue(true);
			return;
		}
		fail("Exception should have occured.");
	}
	
	@Test
	public void testModDivide() {
		String s1 = "abbc3deeaadeadbe2123";
		String s2 = "aab34aaaeeefadb42";
		String s3 = "55ddeb42aaaaaaaaaaabbee3123";
		BigInteger test1 = new BigInteger(s1);
		BigInteger test2 = new BigInteger(s2);
		BigInteger test3 = new BigInteger(s3);
		java.math.BigInteger r1 = new java.math.BigInteger(s1, 16);
		java.math.BigInteger r2 = new java.math.BigInteger(s2, 16);
		java.math.BigInteger r3 = new java.math.BigInteger(s3, 16);
		try {
			assertEquals(r3.multiply(r1.modInverse(r2)).mod(r2).toString(16), test3.modDivide(test1, test2).toString());
		} catch (NotInvertibleException e) {
			e.printStackTrace();
			fail("Exception");
		}
	}
	
	@Test
	public void testPrimeTest() {
		BigInteger t1 = new BigInteger("b");
		BigInteger t2 = new BigInteger("dd");
		assertTrue(t1.primeTest(new BigInteger("a")));
		assertTrue(t2.primeTest(new BigInteger("ae")));  //Strong liar, "dd" is composite!
	}

	@Test
	public void testPrime() {
		
		float prob = (float)0.00001;
		
		BigInteger t1 = new BigInteger("D4F");
		BigInteger t2 = new BigInteger("dd");
		BigInteger t3 = new BigInteger("231");
		BigInteger t4 = new BigInteger("2EB2563693");
		BigInteger t5 = new BigInteger("641B9");
		assertTrue(t1.prime(prob));
		assertFalse(t2.prime(prob));
		assertTrue(t4.prime(prob));		
		assertFalse(t5.prime(prob));
		assertFalse(t3.prime(prob));      //Carmichael number!
		
	}
	
	@Test
	public void testNewPrime() {
		
		float prob = (float)0.000001;
		assertTrue(BigInteger.newPrime(10, prob).prime(prob));
		assertTrue(BigInteger.newPrime(20, prob).prime(prob));
		assertTrue(BigInteger.newPrime(10, prob).prime(prob));
		
	}
	
	@Test
	public void testEncryptionFuncs() {
		
		BigInteger N = new BigInteger("37");
		BigInteger e = new BigInteger("3");
		BigInteger d = new BigInteger("1b");
		
		BigInteger test1 = new BigInteger("20");		
		
		assertEquals(test1.toString(), test1.encrypt(N, e).decrypt(N, d).toString());
		
	}
	
	@Test
	public void testRSA() {
		
		BigInteger[] keys = BigInteger.generateRSA(10);
		
		BigInteger[] keys2 = BigInteger.generateRSA(new BigInteger("7"), new BigInteger("b"));
		System.out.println(Arrays.toString(keys2));
		
		System.out.println(Arrays.toString(keys));
		
		BigInteger test1 = new BigInteger("2000");        //Assume this will be less than n.
		
		BigInteger test2 = test1.encrypt(keys[0], keys[1]);
		System.out.println(test2);
		
		assertEquals(test1.toString(), test2.decrypt(keys[0], keys[2]).toString());
		
	}
	
	@Test
	public void testStringEncryption() {
		
		BigInteger N = new BigInteger("37");
		BigInteger e = new BigInteger("3");
		BigInteger d = new BigInteger("1b");
		
		String tstr = "The quick brown fox jupmed over the lazy dog.";
		String enc = BigInteger.encryptString(tstr, N, e);
		System.out.println(BigInteger.decryptString(enc, N, d));
		assertEquals(tstr, BigInteger.decryptString(enc, N, d));
	}
	
	@Test
	public void testDivConquerMultiply() {
		//special cases
		BigInteger b1, b2;
		java.math.BigInteger r1, r2;
		b1 = new BigInteger("1");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.divConquerMultiply(b2).toString(), h);
		}
		b1 = new BigInteger("0");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.divConquerMultiply(b2).toString(), "0");
		}
		//brute force
		for (int i = 0; i < 100; i++) {
			b1 = BigInteger.random(128, null);
			b2 = BigInteger.random(128, null);
			r1 = new java.math.BigInteger(b1.toString(), 16);
			r2 = new java.math.BigInteger(b2.toString(), 16);
			assertEquals(b1.divConquerMultiply(b2).toString(), r1.multiply(r2).toString(16));
		}
	}
	
	@Test
	public void testfftMultiply() {
		//special cases
		BigInteger b1, b2;
		java.math.BigInteger r1, r2;
		b1 = new BigInteger("1");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.fftMultiply(b2).toString(), h);
		}
		b1 = new BigInteger("0");
		for (int i = 0; i < 128; i++) {
			String h = Integer.toHexString(i);
			b2 = new BigInteger(h);
			assertEquals(b1.fftMultiply(b2).toString(), "0");
		}
		//brute force
		for (int i = 0; i < 100; i++) {
			b1 = BigInteger.random(128, null);
			b2 = BigInteger.random(128, null);
			r1 = new java.math.BigInteger(b1.toString(), 16);
			r2 = new java.math.BigInteger(b2.toString(), 16);
			assertEquals(b1.fftMultiply(b2).toString(), r1.multiply(r2).toString(16));
		}
	}
	
}
