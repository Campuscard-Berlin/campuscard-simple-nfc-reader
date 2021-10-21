This is a very simple Java application which drives a PC/SC NFC terminal. It
uses `javax.smartcardio`, and does not do very much. It is mainly intended for
testing our HCE Capacitor plugin. The license is GPLv3.

# Requirements
Java 11, Maven, a card terminal and drivers for that terminal. Your operating
system shouldn't matter. Just run `mvn package` and call `java -jar` on the
resulting jar with dependencies.

# What it does
It connects to a PC/SC terminal whose name and/or model contains "Elatec". It
waits for a card; when it finds a card, it:
  - selects the Mifare DESFire AID with an ISO 7816 command,
  - selects a DESFire application using a wrapped DESFire command,
  - authenticates, and
  - reads several bytes from one of the application's files.

# Limitations

Everything is hardcoded: if you want to select a different DESFire application
than the one we're selecting for testing, or authenticate with a different key
number, or authenticate with a different key, or read a different amount, or
from a different file, or from a different offset, you must modify the source.
This will change soon.

Most importantly, if you want to use a terminal not produced by Elatec, you'll
need to change the (hardcoded) model name the application searches for. And if
you want to use this application to read any card which is not a Mifare DESFire,
you will need to rewrite it completely -- it uses the wrapped command set which
no other card recognizes.

However, the application has been tested both on real cards and on a phone
running our NFC plugin's sample application, and it worked perfectly fine in
both cases (using an Elatec TWN4, on Ubuntu 18.04). If it doesn't work for you
with a Mifare DESFire card, feel very free to file an issue.
