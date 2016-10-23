// Arduino Brain Library
// Serial out example, 	grabs the brain data and sends CSV out over the hardware serial.
// Eric Mika, 2010

#include "Brain.h"

// Set up the brain parser, pass it the hardware serial object you want to listen on.
Brain brain(Serial1);

int packet_delay = 2500;
int t1 = 0;
int t2 = 0;
String last_read;

const String EMPTY = "";

void setup() {
  Serial1.begin(9600);
}

void loop() {
  if (brain.update()) {
    last_read = brain.readJSON();

    if (last_read == EMPTY) {
      t2 = millis();
    }
  }


  if (packet_delay >= 0) {
    t1 = millis();

    boolean delay_met = (t1 - t2) > packet_delay;

    if (delay_met && last_read != EMPTY) {
      Serial.println(last_read);
      last_read = EMPTY;
    }
  }
}

