#ifndef Brain_h
#define Brain_h

#include "Arduino.h"

#define MAX_PACKET_LENGTH 32
#define EEG_POWER_BANDS 8

class Brain {
    public:
        Brain(Stream &_brainStream);    

        // Run this in the main loop.
        boolean update();

        // String with most recent error.
        char* readErrors();

        // Returns comme-delimited string of all available brain data.
        // Sequence is as below.
        char* readCSV();

        // Same as readCSV but formatted as JSON.
        char* readJSON();
        
    private:
        Stream* brainStream;        
        uint8_t packetData[MAX_PACKET_LENGTH];
        boolean inPacket;
        uint8_t latestByte;
        uint8_t lastByte;
        uint8_t packetIndex;
        uint8_t packetLength;
        uint8_t checksum;
        uint8_t checksumAccumulator;
        uint8_t eegPowerLength;
        boolean hasPower;
        void clearPacket();
        void clearEegPower();
        boolean parsePacket();
        
        void printPacket();
        void init();    
        void printDebug();

        // 3 x 3 char uint8_t
        // 8 x 10 char uint32_t
        // 10 x 1 char commas
        // 1 x 180 char JSON string with empty values
        // 1 x 1 char 0 (string termination)
        // -------------------------
        // 280 characters   
        char jsonBuffer[280];
        
        // Longest error is
        // 22 x 1 char uint8_ts
        // 1 x 1 char 0 (string termination)
        char latestError[23];       
        
        uint8_t signalQuality;
        uint8_t attention;
        uint8_t meditation;

        boolean freshPacket;
        
        // Lighter to just make this public, instead of using the getter?
        uint32_t eegPower[EEG_POWER_BANDS];
};

#endif

