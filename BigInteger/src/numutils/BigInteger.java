package numutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class BigInteger implements Comparable<BigInteger>, Cloneable {

	private int nbits;
	private BitSet bits;

	// epsilon used for rsa key generation
	private static final float EPSILON = 1e-16f;
	private static final Random RANDOM = new Random();
	private static final BigInteger ZERO = new BigInteger("0x0");
	private static final BigInteger ONE = new BigInteger("0x1");
	// list of primes used for rsa key generation
	private static BigInteger[] PRIMES = new BigInteger[] {
			new BigInteger("0x2"), new BigInteger("0x3"),
			new BigInteger("0x5"), new BigInteger("0x7"),
			new BigInteger("0xb"), new BigInteger("0xd"),
			new BigInteger("0x11"), new BigInteger("0x13"),
			new BigInteger("0x17"), new BigInteger("0x1d"),
			new BigInteger("0x1f"), new BigInteger("0x25"),
			new BigInteger("0x29"), new BigInteger("0x2b"),
			new BigInteger("0x2f"), new BigInteger("0x35"),
			new BigInteger("0x3b"), new BigInteger("0x3d"),
			new BigInteger("0x43"), new BigInteger("0x47"),
			new BigInteger("0x49"), new BigInteger("0x4f"),
			new BigInteger("0x53"), new BigInteger("0x59"),
			new BigInteger("0x61"), new BigInteger("0x65"),
			new BigInteger("0x67"), new BigInteger("0x6b"),
			new BigInteger("0x6d"), new BigInteger("0x71"),
			new BigInteger("0x7f"), new BigInteger("0x83"),
			new BigInteger("0x89"), new BigInteger("0x8b"),
			new BigInteger("0x95"), new BigInteger("0x97"),
			new BigInteger("0x9d"), new BigInteger("0xa3"),
			new BigInteger("0xa7"), new BigInteger("0xad"),
			new BigInteger("0xb3"), new BigInteger("0xb5"),
			new BigInteger("0xbf"), new BigInteger("0xc1"),
			new BigInteger("0xc5"), new BigInteger("0xc7"),
			new BigInteger("0xd3"), new BigInteger("0xdf"),
			new BigInteger("0xe3"), new BigInteger("0xe5") };

	private BigInteger(BitSet bits) {
		this.bits = bits.get(0, nbits = bits.length());
	}

	private BigInteger(Complex[] coefficients) {
		coefficients = Arrays.copyOf(coefficients, coefficients.length + 1);
		coefficients[coefficients.length - 1] = new Complex(0, 0);
		bits = new BitSet();
		for (int i = 0; i < coefficients.length; i++) {
			int d = (int) Math.round(coefficients[i].re());
			if (i != coefficients.length - 1) {
				coefficients[i + 1] = coefficients[i + 1].plus(new Complex(
						d / 16, 0));
				d %= 16;
			}
			for (int j = 4 * i; d > 0; j++) {
				bits.set(j, (d & 1) == 1);
				d >>= 1;
			}
		}
		nbits = bits.length();
	}

	BigInteger(String hex) {
		hex = hex.replaceFirst("^(0x)?0*", "");
		if (hex.length() == 0) {
			hex = "0";
		}
		nbits = (hex.length() - 1)
				* 4
				+ Integer.SIZE
				- Integer.numberOfLeadingZeros(Character.digit(hex.charAt(0),
						16));
		bits = new BitSet(nbits);
		for (int i = hex.length() - 1, j = 0; i >= 0; i--, j += 4) {
			int h = Character.digit(hex.charAt(i), 16);
			for (int k = 0; k < 4; k++) {
				bits.set(j + k, (h & 1) == 1);
				h >>= 1;
			}
		}
	}

	BigInteger add(BigInteger term, boolean discardOverflow) {
		BigInteger sum = (BigInteger) clone();
		sum.nbits = discardOverflow ? sum.nbits : Math.max(this.nbits,
				term.nbits);
		boolean carry = false;
		for (int i = 0; i < sum.nbits; i++) {
			boolean a = bits.get(i), b = term.bits.get(i);
			sum.bits.set(i, a ^ b ^ carry);
			carry = (carry && (a || b)) || (a && b);
		}
		if (carry && !discardOverflow) {
			sum.bits.set(sum.nbits++);
		}
		return sum;
	}

	public BigInteger add(BigInteger term) {
		return add(term, false);
	}

	public BigInteger and(BigInteger conjunction) {
		BitSet bits = (BitSet) this.bits.clone();
		bits.and(conjunction.bits);
		return new BigInteger(bits);
	}

	@Override
	protected Object clone() {
		BigInteger clone = new BigInteger(bits);
		clone.nbits = nbits;
		return clone;
	}

	@Override
	public int compareTo(BigInteger N) {
		int cmp = Integer.compare(nbits, N.nbits);
		if (cmp != 0) {
			return cmp;
		}
		for (int i = nbits - 1; i >= 0; i--) {
			boolean a = bits.get(i), b = N.bits.get(i);
			if (!a && b) {
				return -1;
			} else if (a && !b) {
				return 1;
			}
		}
		return 0;
	}

	public BigInteger decrypt(BigInteger N, BigInteger d) {
		return modExp(d, N);
	}

	static String decryptString(String in, BigInteger N, BigInteger d) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			sb.append(String
					.format("%8s", Integer.toBinaryString(in.charAt(i)))
					.replace(' ', '0'));
		}
		sb.delete(sb.length() - sb.length() % N.nbits, sb.length());
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < sb.length(); i += N.nbits) {
			BitSet b = new BitSet(N.nbits);
			for (int j = 0; j < N.nbits; j++) {
				b.set(j, sb.charAt(i + j) == '1');
			}
			BigInteger cipher = new BigInteger(b);
			BigInteger message = cipher.decrypt(N, d);
			for (int j = 0; j < N.nbits - 1; j++) {
				sb2.append(message.bits.get(j) ? '1' : '0');
			}
		}
		sb2.reverse().delete(0, sb2.length() % 8);
		StringBuilder sb3 = new StringBuilder();
		for (int i = 0; i < sb2.length(); i += 8) {
			sb3.append((char) Integer.parseInt(sb2.substring(i, i + 8), 2));
		}
		return sb3.reverse().toString();
	}

	public BigInteger divConquerMultiply(BigInteger factor) {
		if (equals(BigInteger.ZERO) || factor.equals(BigInteger.ZERO)) {
			return BigInteger.ZERO;
		}
		if (equals(BigInteger.ONE)) {
			return factor;
		}
		if (factor.equals(BigInteger.ONE)) {
			return this;
		}
		int n = Math.max(nbits, factor.nbits);
		BigInteger L = new BigInteger(bits.get(n / 2, n));
		BigInteger R = new BigInteger(bits.get(0, n / 2));
		BigInteger factorL = new BigInteger(factor.bits.get(n / 2, n));
		BigInteger factorR = new BigInteger(factor.bits.get(0, n / 2));
		BigInteger P1 = L.divConquerMultiply(factorL);
		BigInteger P2 = R.divConquerMultiply(factorR);
		BigInteger P3 = L.add(R).divConquerMultiply(factorL.add(factorR));
		return P1.leftShift(n - (n & 1))
				.add(P3.subtract(P1).subtract(P2).leftShift(n / 2)).add(P2);
	}

	public BigInteger encrypt(BigInteger N, BigInteger e) {
		return modExp(e, N);
	}

	static String encryptString(String in, BigInteger N, BigInteger e) {
		BitSet bits = BitSet.valueOf(in.getBytes());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < in.getBytes().length * Byte.SIZE; i += N.nbits - 1) {
			BitSet b = bits.get(i, i + N.nbits - 1);
			BigInteger message = new BigInteger(b);
			BigInteger cipher = message.encrypt(N, e);
			for (int j = 0; j < N.nbits; j++) {
				sb.append(cipher.bits.get(j) ? '1' : '0');
			}
		}
		StringBuilder sb2 = new StringBuilder();
		while (sb.length() % 8 != 0) {
			sb.append('0');
		}
		for (int i = 0; i < sb.length(); i += 8) {
			sb2.append((char) Integer.parseInt(sb.substring(i, i + 8), 2));
		}
		return sb2.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BigInteger)) {
			return false;
		}
		return compareTo((BigInteger) obj) == 0;
	}

	public BigInteger[] euclid(BigInteger y) {
		// notice that the signs of a and b swap with each recursive step, with
		// a starting non-negative
		if (y.equals(BigInteger.ZERO)) {
			return new BigInteger[] { BigInteger.ONE, BigInteger.ZERO, this,
					BigInteger.ZERO };
		}
		BigInteger[] e = y.euclid(quadDivision(y)[1]);
		e[1] = e[0].add(quadDivision(y)[0].quadMultiply(e[0] = e[1]));
		e[3] = BigInteger.ONE.subtract(e[3]);
		return e;
	}

	public BigInteger fftMultiply(BigInteger factor) {
		if (equals(BigInteger.ZERO) || factor.equals(BigInteger.ZERO)) {
			return BigInteger.ZERO;
		}
		Complex[] a = toComplexArray();
		Complex[] b = factor.toComplexArray();
		int N = a.length + b.length - 1;
		// find next highest power of two:
		// http://graphics.stanford.edu/~seander/bithacks.html
		int M = N;
		M--;
		for (int i = 1; i < Integer.SIZE; i *= 2) {
			M |= M >> i;
		}
		M++;
		Complex[] A = Arrays.copyOf(a, M);
		for (int i = a.length; i < M; i++) {
			A[i] = new Complex(0, 0);
		}
		Complex[] B = Arrays.copyOf(b, M);
		for (int i = b.length; i < M; i++) {
			B[i] = new Complex(0, 0);
		}
		int[] c = { 0xAAAAAAAA, 0xCCCCCCCC, 0xF0F0F0F0, 0xFF00FF00, 0xFFFF0000 };
		// find log base two:
		// http://graphics.stanford.edu/~seander/bithacks.html
		int n = 0;
		for (int i = 0; i < c.length; i++) {
			n |= ((M & c[i]) != 0 ? 1 : 0) << i;
		}
		Complex[] fft = Complex.fft(A, n);
		Complex[] fft_ = Complex.fft(B, n);
		for (int i = 0; i < M; i++) {
			fft[i] = fft[i].times(fft_[i]);
		}
		return new BigInteger(Arrays.copyOf(Complex.ifft(fft, n), N));
	}

	public static BigInteger[] generateRSA(int n) {
		BigInteger p = BigInteger.newPrime(n, EPSILON);
		BigInteger q = BigInteger.newPrime(n, EPSILON);
		return generateRSA(p, q);
	}

	public static BigInteger[] generateRSA(BigInteger p, BigInteger q) {
		BigInteger N = p.quadMultiply(q);
		BigInteger phi = N.subtract(p).subtract(q).add(BigInteger.ONE);
		BigInteger e = null;
		for (int i = 0; i < PRIMES.length; i++) {
			if ((e = PRIMES[i]).euclid(phi)[2].equals(BigInteger.ONE)) {
				break;
			}
		}
		if (e == null) {
			throw new RuntimeException("no rsa key found");
		}
		BigInteger d = e.modInverse(phi);
		return new BigInteger[] { N, e, d };
	}

	public BigInteger leftShift(int n) {
		BitSet bits = new BitSet(nbits + n);
		for (int i = 0; i < nbits; i++) {
			bits.set(n + i, this.bits.get(i));
		}
		return new BigInteger(bits);
	}

	public BigInteger modAdd(BigInteger term, BigInteger modulus) {
		return add(term).quadDivision(modulus)[1];
	}

	public BigInteger modDivide(BigInteger divisor, BigInteger modulus)
			throws NotInvertibleException {
		return modQuadMultiply(divisor.modInverse(modulus), modulus);
	}

	public BigInteger modExp(BigInteger exp, BigInteger modulus) {
		if (exp.equals(BigInteger.ZERO)) {
			return BigInteger.ONE;
		}
		BigInteger x = modExp(exp.rightShift(1), modulus);
		x = x.quadMultiply(x);
		if (exp.and(BigInteger.ONE).equals(BigInteger.ONE)) {
			x = x.quadMultiply(this);
		}
		return x.quadDivision(modulus)[1];
	}

	public BigInteger modInverse(BigInteger modulus)
			throws NotInvertibleException {
		BigInteger[] e = euclid(modulus);
		if (!e[2].equals(BigInteger.ONE)) {
			throw new NotInvertibleException("no modular inverse exists");
		}
		return e[3].equals(BigInteger.ONE) ? modulus.subtract(e[0]) : e[0];
	}

	public BigInteger modQuadMultiply(BigInteger factor, BigInteger modulus) {
		return quadMultiply(factor).quadDivision(modulus)[1];
	}

	public static BigInteger newPrime(int n, float prob) {
		// we generate random numbers until we find one which passes our
		// iterated miller-rabin primality test. since we specify our failure
		// probability for the primality test, we can be sure that a number
		// returned by this algorithm is prime with probability prob, since it
		// will only return if it passes the test.
		BigInteger x = BigInteger.random(n, null);
		while (!x.prime(prob)) {
			x = BigInteger.random(n, null);
		}
		return x;
	}

	public boolean prime(float prob) {
		// since the miller-rabin primality test used by this method has
		// probability 1/4 of giving a false positive, applying the test k times
		// has a probability 1/4^k of giving a false positive. we thus start
		// with a failure probability of 1, and then each time the algorithm is
		// applied, we divide that probability by 4. once the probability falls
		// below the argument prob, we are done. the loop used here is
		// essentially just reversed, i.e. we start with prob and multiply it by
		// 4 after each iteration, terminating when we rise above 1.
		for (int i = 0; prob < 1.0; i++, prob *= 4.0) {
			if (!primeTest(PRIMES[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean primeTest(BigInteger a) {
		// miller-rabin primality test: probability 1/4 of giving a false
		// positive, not susceptible to carmichael numbers
		if (compareTo(BigInteger.ONE) <= 0) {
			return false;
		}
		int s = 0;
		BigInteger d = subtract(BigInteger.ONE);
		BigInteger q = d.rightShift(1);
		BigInteger r = d.and(BigInteger.ONE);
		while (r.equals(BigInteger.ZERO)) {
			d = q;
			s++;
			q = d.rightShift(1);
			r = d.and(BigInteger.ONE);
		}
		BigInteger x = a.modExp(d, this);
		if (!x.equals(BigInteger.ONE) && !x.equals(subtract(BigInteger.ONE))) {
			for (int i = 1; i < s; i++) {
				x = x.modQuadMultiply(x, this);
				if (x.equals(subtract(BigInteger.ONE))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public BigInteger[] quadDivision(BigInteger divisor) {
		if (divisor.equals(BigInteger.ZERO)) {
			throw new ArithmeticException("division or modulo by zero");
		}
		if (equals(BigInteger.ZERO)) {
			return new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };
		}
		BigInteger[] qr = rightShift(1).quadDivision(divisor);
		qr[0] = qr[0].add(qr[0]);
		qr[1] = qr[1].add(qr[1]);
		if (and(BigInteger.ONE).equals(BigInteger.ONE)) {
			qr[1] = qr[1].add(BigInteger.ONE);
		}
		if (qr[1].compareTo(divisor) >= 0) {
			qr[1] = qr[1].subtract(divisor);
			qr[0] = qr[0].add(BigInteger.ONE);
		}
		return qr;
	}

	public BigInteger quadMultiply(BigInteger factor) {
		BigInteger product = BigInteger.ZERO;
		for (int i = 0; i < nbits; i++) {
			if (bits.get(i)) {
				product = product.add(factor);
			}
			factor = factor.add(factor);
		}
		return product;
	}

	public static BigInteger random(int n, Random gen) {
		if (gen == null) {
			gen = RANDOM;
		}
		BitSet bits = new BitSet(n);
		for (int i = 0; i < n; i++) {
			bits.set(i, gen.nextBoolean());
		}
		return new BigInteger(bits);
	}

	public BigInteger rightShift(int n) {
		if (n >= nbits) {
			return BigInteger.ZERO;
		}
		return new BigInteger(bits.get(n, nbits));
	}

	public BigInteger subtract(BigInteger term) {
		if (compareTo(term) < 0) {
			throw new ArithmeticException("subtraction yields negative number");
		}
		BigInteger difference = (BigInteger) term.clone();
		difference.bits.flip(0, difference.nbits = nbits);
		difference = difference.add(BigInteger.ONE, true).add(this, true);
		difference.bits = difference.bits.get(0,
				difference.nbits = difference.bits.length());
		return difference;
	}

	private Complex[] toComplexArray() {
		Complex[] coefficients = new Complex[(nbits + 3) / 4];
		for (int i = 0; i < coefficients.length; i++) {
			int c = 0;
			for (int j = 3; j >= 0; j--) {
				c <<= 1;
				if (bits.get(i * 4 + j)) {
					c |= 1;
				}
			}
			coefficients[i] = new Complex(c, 0);
		}
		return coefficients;
	}

	public String toString() {
		if (equals(BigInteger.ZERO)) {
			return "0";
		}
		StringBuilder hexBuilder = new StringBuilder((nbits + 3) / 4);
		for (int i = 0; i < nbits; i += 4) {
			int h = 0;
			for (int j = 3; j >= 0; j--) {
				h <<= 1;
				h |= bits.get(i + j) ? 1 : 0;
			}
			hexBuilder.append(Integer.toHexString(h));
		}
		hexBuilder.reverse();
		return hexBuilder.toString();
	}

	public static void main(String[] args) throws IOException {
		String theAnswerToLifeTheUniverseAndEverything = "42";
		BigInteger N = new BigInteger("0x131d50c588d428939c8ddec0212e450d");
		BigInteger e = new BigInteger("0xb");
		BigInteger d = new BigInteger("0x6f363308ed8c8efa77fd78cbe51c573"); // lol
		String encryptString = BigInteger.encryptString(
				theAnswerToLifeTheUniverseAndEverything, N, e);
		BufferedWriter bw = new BufferedWriter(new FileWriter("bonus.msg"));
		bw.write(encryptString);
		bw.close();
		BufferedReader br = new BufferedReader(new FileReader("bonus.msg"));
		System.out.println(BigInteger.decryptString(br.readLine(), N, d));
		br.close();
	}
}
