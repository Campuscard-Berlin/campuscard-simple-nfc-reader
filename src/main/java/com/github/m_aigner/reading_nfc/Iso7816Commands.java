package com.github.m_aigner.reading_nfc;

import javax.smartcardio.CommandAPDU;

public class Iso7816Commands {
	public static CommandAPDU selectApplicationByAid(byte[] aid) throws IllegalArgumentException {
		if (aid.length == 0 || aid.length > 255) {
			throw new IllegalArgumentException();
		}

		byte[] commandBytes = new byte[aid.length + 5];
		commandBytes[1] = (byte) 0xA4; // ISO select
		commandBytes[2] = (byte) 0x04; // ...by AID

		// commandBytes[0] == 0 && commandBytes[3] == 0, intentionally

		commandBytes[4] = (byte) aid.length;
		System.arraycopy(aid, 0, commandBytes, 5, aid.length);

		// we know this won't throw
		return new CommandAPDU(commandBytes);
	}
}