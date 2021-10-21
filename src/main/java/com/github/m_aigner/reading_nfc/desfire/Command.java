package com.github.m_aigner.reading_nfc.desfire;

import javax.smartcardio.CommandAPDU;

class Command {
	byte[] bytes;

	Command(byte datalessCommand) {
		this.bytes = new byte[] {(byte) 0x90, datalessCommand, (byte) 0x00, (byte) 0x00, (byte) 0x00};
	}

	// TODO move from "byte command" to an enum or something
	Command(byte command, byte[] data) {
		if (data.length > 54) {
			// this was discovered (?) by Andreas; I have not verified it
			throw new IllegalArgumentException("data-argument too long for DESFire (i. e., longer than 54 bytes)");
		}

		this.bytes = new byte[data.length + 6];
		this.bytes[0] = (byte) 0x90;
		this.bytes[1] = command;

		// this.bytes[2], this.bytes[3] are zero since Java zero-initializes byte arrays

		this.bytes[4] = (byte) data.length;
		System.arraycopy(data, 0, this.bytes, 5, data.length);

		// this.bytes[this.bytes.length - 1] is zero since Java zero-initializes byte arrays
	}

	CommandAPDU toCommandApdu() {
		// no need to handle either of the exceptions CommandAPDU's constructor can throw -- we know the array is
		// non-null and a valid command, since the only way to get an object of this class is to call a constructor
		// which enforces these
		return new CommandAPDU(bytes);
	}
}
