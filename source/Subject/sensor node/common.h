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

#define INITIAL_BATTERY 1000
#define CRITICAL_BATTERY 300
#define SENSING_DRAIN 1
#define TRANSMITTING_DRAIN 5

#define UDP_CLIENT_PORT 8765
#define UDP_SERVER_PORT 5678

#define UDP_EXAMPLE_ID  190

#ifndef PERIOD
#define PERIOD 60
#endif

#define START_INTERVAL    (15 * CLOCK_SECOND)
#define SEND_INTERVAL   (PERIOD * CLOCK_SECOND)
#define SEND_TIME   (random_rand() % (SEND_INTERVAL))
#define MAX_PAYLOAD_LEN   30

/*****************************************************
  PROJECT ANAWS util variables
******************************************************/


static uint32_t battery = INITIAL_BATTERY;

uint32_t reduceBattery(uint32_t drain);

void stampa(int value, char* resourceName, uint32_t dataLevel);

//For testing purposes
void critic_battery();