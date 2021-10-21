package com.github.m_aigner.reading_nfc.desfire;

import javax.smartcardio.CommandAPDU;

public class DesfireCommands {
	public static CommandAPDU getApplicationIds() {
		return new Command((byte) 0x6A).toCommandApdu();
	}

	public static CommandAPDU selectApplication(int applicationId) throws IllegalArgumentException {
		try {
			return new Command((byte) 0x5A, to24BitLittleEndianUnsignedInt(applicationId)).toCommandApdu();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Application ID negative or greater than 2 ^ 24");
		}
	}

	public static CommandAPDU authenticateAes(byte keyNumber) throws IllegalArgumentException {
		if (keyNumber < 0 || keyNumber > 13) {
			throw new IllegalArgumentException("Key number negative or greater than 13");
		}

		return new Command((byte) 0xAA, new byte[] {keyNumber}).toCommandApdu();
	}

	public static CommandAPDU additionalFrame(byte[] data) throws IllegalArgumentException {
		try {
			return new Command((byte) 0xAF, data).toCommandApdu();
		} catch (IllegalArgumentException e) {
			// rethrow since there's no need for the package-private class to show up in stacktraces
			throw e;
		}
	}

	public static CommandAPDU readData(byte fileNumber, int offset, int length)  throws IllegalArgumentException {
		if (fileNumber < 0 || fileNumber > 31) {
			throw new IllegalArgumentException("File number is below 0 or above 31");
		}

		byte[] offsetForCommand;
		byte[] lengthForCommand;

		try {
			offsetForCommand = to24BitLittleEndianUnsignedInt(offset);
			lengthForCommand = to24BitLittleEndianUnsignedInt(length);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Offset, length or both are below 0 or above 2 ^ 24");
		}

		byte[] allData = new byte[7];
		allData[0] = fileNumber;
		System.arraycopy(offsetForCommand, 0, allData, 1, 3);
		System.arraycopy(lengthForCommand, 0, allData, 4, 3);

		// this line cannot throw, given the spec for the constructor and the construction
		// of the arguments
		return new Command((byte) 0xBD, allData).toCommandApdu();
	}

	private static byte[] to24BitLittleEndianUnsignedInt(int javaInt) throws IllegalArgumentException {
		if ((javaInt >>> 24) != 0) {
			throw new IllegalArgumentException();
		}

		return new byte[] {(byte) javaInt, (byte) (javaInt >> 8), (byte) (javaInt >> 16)};
	}
}
