#include "Arduino.h"
#include "Brain.h"

Brain::Brain(Stream &_brainStream) {
  brainStream = &_brainStream;

  // Keep the rest of the initialization process in a separate method in case
  // we overload the constructor.
  init();
}

void Brain::init() {
  // It's up to the calling code to start the stream
  // Usually Serial.begin(9600);
  freshPacket = false;
  inPacket = false;
  packetIndex = 0;
  packetLength = 0;
  eegPowerLength = 0;
  hasPower = false;
  checksum = 0;
  checksumAccumulator = 0;

  signalQuality = 200;
  attention = 0;
  meditation = 0;

  clearEegPower();
}

boolean Brain::update() {
  if (brainStream->available()) {
    latestByte = brainStream->read();

    // Build a packet if we know we're and not just listening for sync bytes.
    if (inPacket) {

      // First byte after the sync bytes is the length of the upcoming packet.
      if (packetIndex == 0) {
        packetLength = latestByte;

        // Catch error if packet is too long
        if (packetLength > MAX_PACKET_LENGTH) {
          // Packet exceeded max length
          // Send an error
          sprintf(latestError, "ERROR: Packet too long %i", packetLength);
          inPacket = false;
        }
      }
      else if (packetIndex <= packetLength) {
        // Run of the mill data bytes.

        // Print them here

        // Store the byte in an array for parsing later.
        packetData[packetIndex - 1] = latestByte;

        // Keep building the checksum.
        checksumAccumulator += latestByte;
      }
      else if (packetIndex > packetLength) {
        // We're at the end of the data payload.

        // Check the checksum.
        checksum = latestByte;
        checksumAccumulator = 255 - checksumAccumulator;

        // Do they match?
        if (checksum == checksumAccumulator) {
          boolean parseSuccess = parsePacket();

          if (parseSuccess) {
            freshPacket = true;
          }
          else {
            // Parsing failed, send an error.
            sprintf(latestError, "ERROR: Could not parse");
            // good place to print the packet if debugging
          }
        }
        else {
          // Checksum mismatch, send an error.
          sprintf(latestError, "ERROR: Checksum");
          // good place to print the packet if debugging
        }
        // End of packet

        // Reset, prep for next packet
        inPacket = false;
      }

      packetIndex++;
    }

    // Look for the start of the packet
    if ((latestByte == 170) && (lastByte == 170) && !inPacket) {
      // Start of packet
      inPacket = true;
      packetIndex = 0;
      checksumAccumulator = 0;
    }

    // Keep track of the last byte so we can find the sync byte pairs.
    lastByte = latestByte;
  }

  if (freshPacket) {
    freshPacket = false;
    return true;
  }
  else {
    return false;
  }

}

void Brain::clearPacket() {
  for (uint8_t i = 0; i < MAX_PACKET_LENGTH; i++) {
    packetData[i] = 0;
  }
}

void Brain::clearEegPower() {
  // Zero the power bands.
  for (uint8_t i = 0; i < EEG_POWER_BANDS; i++) {
    eegPower[i] = 0;
  }
}

boolean Brain::parsePacket() {
  // Loop through the packet, extracting data.
  // Based on mindset_communications_protocol.pdf from the Neurosky Mindset SDK.
  // Returns true if passing succeeds
  hasPower = false;
  boolean parseSuccess = true;
  int rawValue = 0;

  clearEegPower();    // clear the eeg power to make sure we're honest about missing values

  for (uint8_t i = 0; i < packetLength; i++) {
    switch (packetData[i]) {
      case 0x2:
        signalQuality = packetData[++i];
        break;
      case 0x4:
        attention = packetData[++i];
        break;
      case 0x5:
        meditation = packetData[++i];
        break;
      case 0x83:
        // ASIC_EEG_POWER: eight big-endian 3-uint8_t unsigned integer values representing delta, theta, low-alpha high-alpha, low-beta, high-beta, low-gamma, and mid-gamma EEG band power values
        // The next uint8_t sets the length, usually 24 (Eight 24-bit numbers... big endian?)
        // We dont' use this value so let's skip it and just increment i
        i++;

        // Extract the values
        for (int j = 0; j < EEG_POWER_BANDS; j++) {
          eegPower[j] = ((uint32_t)packetData[++i] << 16) | ((uint32_t)packetData[++i] << 8) | (uint32_t)packetData[++i];
        }

        hasPower = true;
        // This seems to happen once during start-up on the force trainer. Strange. Wise to wait a couple of packets before
        // you start reading.
        break;
      case 0x80:
        // We dont' use this value so let's skip it and just increment i
        // uint8_t packetLength = packetData[++i];
        i++;
        rawValue = ((int)packetData[++i] << 8) | packetData[++i];
        break;
      default:
        // Broken packet ?

        Serial.print(F("parsePacket UNMATCHED data 0x"));
        Serial.print(packetData[i], HEX);
        Serial.print(F(" in position "));
        Serial.print(i, DEC);
        printPacket();

        parseSuccess = false;
        break;
    }
  }
  return parseSuccess;
}

char* Brain::readJSON() {

  // JSON string without values = 
  char* jsonFormattable = "{\n"
                          "\t\"has_power\":%s,\n"
                          "\t\"signal_quality\":%d,\n"
                          "\t\"attention\":%d,\n"
                          "\t\"meditation\":%d,\n"
                          "\t\"delta\":%lu,\n"
                          "\t\"theta\":%lu,\n"
                          "\t\"low_alpha\":%lu,\n"
                          "\t\"high_alpha\":%lu,\n"
                          "\t\"low_beta\":%lu,\n"
                          "\t\"high_beta\":%lu,\n"
                          "\t\"low_gamma\":%lu,\n"
                          "\t\"mid_gamma\":%lu\n"
                          "}";

  sprintf(jsonBuffer, jsonFormattable,
          (hasPower ? "true" : "false"),
          signalQuality,
          attention,
          meditation,
          eegPower[0],
          eegPower[1],
          eegPower[2],
          eegPower[3],
          eegPower[4],
          eegPower[5],
          eegPower[6],
          eegPower[7]
         );

  return jsonBuffer;
}

// For debugging, print the entire contents of the packet data array.
void Brain::printPacket() {
  Serial.print("[");
  for (uint8_t i = 0; i < MAX_PACKET_LENGTH; i++) {
    Serial.print(packetData[i], DEC);

    if (i < MAX_PACKET_LENGTH - 1) {
      Serial.print(", ");
    }
  }
  Serial.println("]");
}

void Brain::printDebug() {
  Serial.println("");
  Serial.println("--- Start Packet ---");
  Serial.print("Signal Quality: ");
  Serial.println(signalQuality, DEC);
  Serial.print("Attention: ");
  Serial.println(attention, DEC);
  Serial.print("Meditation: ");
  Serial.println(meditation, DEC);

  if (hasPower) {
    Serial.println("");
    Serial.println("EEG POWER:");
    Serial.print("Delta: ");
    Serial.println(eegPower[0], DEC);
    Serial.print("Theta: ");
    Serial.println(eegPower[1], DEC);
    Serial.print("Low Alpha: ");
    Serial.println(eegPower[2], DEC);
    Serial.print("High Alpha: ");
    Serial.println(eegPower[3], DEC);
    Serial.print("Low Beta: ");
    Serial.println(eegPower[4], DEC);
    Serial.print("High Beta: ");
    Serial.println(eegPower[5], DEC);
    Serial.print("Low Gamma: ");
    Serial.println(eegPower[6], DEC);
    Serial.print("Mid Gamma: ");
    Serial.println(eegPower[7], DEC);
  }

  Serial.println("");
  Serial.print("Checksum Calculated: ");
  Serial.println(checksumAccumulator, DEC);
  Serial.print("Checksum Expected: ");
  Serial.println(checksum, DEC);

  Serial.println("--- End Packet ---");
  Serial.println("");
}
