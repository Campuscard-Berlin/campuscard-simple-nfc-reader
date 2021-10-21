package com.github.m_aigner.reading_nfc;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import com.github.m_aigner.reading_nfc.desfire.DesfireCommands;

public class ChannelWrapper {
	private CardChannel channel;

	public ChannelWrapper(CardChannel channel) {
		this.channel = channel;
	}

	public ResponseAPDU transmitCommand(CommandAPDU command) throws CardException {
		var ret = channel.transmit(command);

		System.out.println("--> " + asHexString(command.getBytes()));
		System.out.println("<-- " + asHexString(ret.getBytes()));

		return ret;
	}

	public boolean authenticate(byte keyNumber, byte[] key) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		Key aes = new SecretKeySpec(key, "AES");
		IvParameterSpec ivParam = new IvParameterSpec(new byte[16]);
		cipher.init(Cipher.DECRYPT_MODE, aes, ivParam);

		byte[] A = new byte[16];
		new SecureRandom().nextBytes(A);

		var authenticateAESCommand = DesfireCommands.authenticateAes((byte) 1);
		var authenticateAESResponse = transmitCommand(authenticateAESCommand);

		byte[] challenge = Arrays.copyOfRange(authenticateAESResponse.getBytes(), 0, authenticateAESResponse.getBytes().length - 2);
		byte[] B = cipher.doFinal(challenge);

		byte[] rotatedB = rotateOneLeft(B);

		byte[] C = appendByteArrays(A, rotatedB);

		var ivParam2 = new IvParameterSpec(challenge);

		cipher.init(Cipher.ENCRYPT_MODE, aes, ivParam2);
		byte[] D = cipher.doFinal(C);

		var command = DesfireCommands.additionalFrame(D);
		var response = transmitCommand(command);

		byte[] challenge2 = Arrays.copyOfRange(response.getBytes(), 0, response.getBytes().length - 2);
		if (response.getBytes()[17] != (byte) 0x00) {
			System.err.println("Card didn't like our response to its first AES challenge");
			return false;
		}

		var ivParam3 = new IvParameterSpec(last16Bytes(D));
		cipher.init(Cipher.DECRYPT_MODE, aes, ivParam3);
		byte[] E = cipher.doFinal(challenge2);

		if (!Arrays.equals(rotateOneLeft(A), E)) {
			System.err.println("We didn't like the card's response to our response to its first AES challenge");
			System.err.println("Expected: " + asHexString(rotateOneLeft(A)));
			System.err.println("Got:      " + asHexString(E));
			return false;
		}

		return true;
	}

	private static final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	private static String asHexString(byte[] bs) {
		var sb = new StringBuilder();

		for (int i = 0; i < bs.length; i++) {
			sb.append(hexArray[(bs[i] & 0xF0) >> 4]);
			sb.append(hexArray[bs[i] & 0x0F]);
		}

		return sb.toString();
	}

	private static byte[] rotateOneLeft(byte[] a) {
		final byte[] rotated = new byte[a.length];
		if (a.length - 1 >= 0) System.arraycopy(a, 1, rotated, 0, a.length - 1);
		rotated[rotated.length - 1] = a[0];
		return rotated;
	}

	private static byte[] last16Bytes(byte[] a) {
		return Arrays.copyOfRange(a, a.length - 16, a.length);
	}

	private static byte[] appendByteArrays(byte[] a, byte[] b) {
		var retval = new byte[a.length + b.length];

		System.arraycopy(a, 0, retval, 0, a.length);
		System.arraycopy(b, 0, retval, a.length, b.length);

		return retval;
	}
}
