package com.github.m_aigner.reading_nfc;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;

import com.github.m_aigner.reading_nfc.desfire.DesfireCommands;

public class Main {
	final static byte[] MIFARE_DESFIRE_AID = {(byte) 0xD2, (byte) 0x76, (byte) 0x00,
			(byte) 0x00, (byte) 0x85, (byte) 0x01, (byte) 0x00};
	final static byte[] key = toByteArray("FF112233445566778899AABBCCDDEEFF");

	public static void main(String[] args) throws Exception {
		var elatec = findElatec();

		if (elatec == null) {
			System.out.println("Failed to find any terminal with \"Elatec\" in its name, bailing.");
			return;
		}

		var ch = waitForChannel(elatec);

		if (ch == null) {
			System.out.println("Found card but failed to acquire channel; bailing.");
			return;
		}

		var channelWrapper = new ChannelWrapper(ch);

		channelWrapper.transmitCommand(Iso7816Commands.selectApplicationByAid(MIFARE_DESFIRE_AID));
		channelWrapper.transmitCommand(DesfireCommands.selectApplication(0x112233));

		if (!channelWrapper.authenticate((byte) 1, key)) {
			System.out.println("Authentication failed");
		}

		var readResponse = channelWrapper.transmitCommand(DesfireCommands.readData((byte) 0, 10, 11));
		var data = new String(Arrays.copyOfRange(readResponse.getBytes(), 0, 11), Charset.forName("UTF-8"));

		System.out.println("We ignore the CRC. We read: " + data);
	}

	private static CardTerminal findElatec() throws Exception {
		var terms = TerminalFactory.getInstance("PC/SC", null).terminals().list();

		for (var t : terms) {
			if (t.getName().contains("Elatec")) {
				return t;
			}
		}

		return null;
	}

	private static CardChannel waitForChannel(CardTerminal t) throws Exception {
		t.waitForCardPresent(0);

		try {
			var c = t.connect("T=1");
			return c.getBasicChannel();
		} catch (Exception e) {
			return null;
		}
	}


	private static byte[] toByteArray(String s) throws IllegalArgumentException {
		int len = s.length();
		if (len % 2 == 1) {
			throw new IllegalArgumentException("Hex string must have even number of characters");
		}
		byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
		for (int i = 0; i < len; i += 2) {
			// Convert each character into a integer (base-16), then bit-shift into place
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
