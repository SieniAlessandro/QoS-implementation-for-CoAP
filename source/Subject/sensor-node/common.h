/*common.h*/

/*******************************************************
 IMPORTED LIBRARIES
 *******************************************************/
#include "contiki.h"

//#ifdef SUBSCRIBER

//#include "lib/random.h"
//#include "sys/ctimer.h"
#include "net/ip/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ip/uip-udp-packet.h"

#ifdef WITH_COMPOWER
#include "powertrace.h"
#endif

#include "contiki-net.h"

#include "net/ipv6/uip-ds6-route.h"

//#else
//FOR ALL THE RESOURCES
#include <limits.h>
#include "er-coap.h"

//#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "rest-engine.h"

#undef PRINTF
#undef PRINT6ADDR
#undef PRINTLLADDR
#undef DEBUG
#define DEBUG 0
#if DEBUG
#define PRINTF(...) printf(__VA_ARGS__)
#define PRINT6ADDR(addr) PRINTF("[%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x]", ((uint8_t *)addr)[0], ((uint8_t *)addr)[1], ((uint8_t *)addr)[2], ((uint8_t *)addr)[3], ((uint8_t *)addr)[4], ((uint8_t *)addr)[5], ((uint8_t *)addr)[6], ((uint8_t *)addr)[7], ((uint8_t *)addr)[8], ((uint8_t *)addr)[9], ((uint8_t *)addr)[10], ((uint8_t *)addr)[11], ((uint8_t *)addr)[12], ((uint8_t *)addr)[13], ((uint8_t *)addr)[14], ((uint8_t *)addr)[15])
#define PRINTLLADDR(lladdr) PRINTF("[%02x:%02x:%02x:%02x:%02x:%02x]",(lladdr)->addr[0], (lladdr)->addr[1], (lladdr)->addr[2], (lladdr)->addr[3],(lladdr)->addr[4], (lladdr)->addr[5])
#else
#define PRINTF(...)
#define PRINT6ADDR(addr)
#define PRINTLLADDR(addr)
#endif

/*****************************************************
  PROJECT ANAWS CONSTANTS
******************************************************/

#define CRITICAL_MAX_AGE 255
#define CRITICAL 0x800000
#define NON_CRITICAL 0

#define BATTERY_MULTIPLIER 10
#define INITIAL_BATTERY 100*BATTERY_MULTIPLIER
#define CRITICAL_BATTERY 30*BATTERY_MULTIPLIER

#define SENSING_DRAIN 1
#define TRANSMITTING_DRAIN 4

#define RESOURCES_SENSING_PERIOD 2

#define BATTERY_MAX_AGE      255
#define BATTERY_INTERVAL_MAX (BATTERY_MAX_AGE - 1)
#define BATTERY_SENSING_PERIOD 10

#define RESOURCE_MAX_AGE 60

//Temperature Constants

#define TEMPERATURE_NON_CRITICAL_CHANGE 4
#define TEMPERATURE_CRITICAL_CHANGE 1
#define TEMPERATURE_CRITICAL_THRESHOLD 30

//Sinusoid Constants
#define SINUSOID_NON_CRITICAL_CHANGE       4
#define SINUSOID_CRITICAL_CHANGE 1
#define SINUSOID_CRITICAL_THRESHOLD 30 //TO DEFINE


/*****************************************************
  PROJECT ANAWS util variables
******************************************************/


static uint32_t battery = INITIAL_BATTERY*BATTERY_MULTIPLIER;

uint32_t reduceBattery(uint32_t drain);

void stampa(int value, char* resourceName, uint32_t dataLevel);

//For testing purposes
void critic_battery();