/*
 * Copyright (c) 2013, Institute for Pervasive Computing, ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 */

/**
 * \file
 *      Example resource
 * \author
 *      Matthias Kovatsch <kovatsch@inf.ethz.ch>
 */


#include "../common.h"
#include "dev/battery-sensor.h"


/*
#include "contiki.h"

#include <string.h>
#include "rest-engine.h"
#include "dev/battery-sensor.h"
#include "er-coap.h"
*/

static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void periodic_handler(void);

#define MAX_AGE      255
#define INTERVAL_MAX (MAX_AGE - 1)
/* A simple getter example. Returns the reading from light sensor with a simple etag */
PERIODIC_RESOURCE(res_battery,
         "title=\"Battery status\";rt=\"Battery\";obs",
         get_handler,
         NULL,
         NULL,
         NULL,
         5*CLOCK_SECOND,
         periodic_handler);

static int32_t interval_counter = INTERVAL_MAX;

static void
get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  //int battery = battery_sensor.value(0);
  //printf("Battery sensed%lu\n", battery);
  unsigned int accept = -1;
  coap_get_header_accept(request, &accept);

  battery /= 10;
  if(accept == -1 || accept == REST.type.TEXT_PLAIN) {
    REST.set_header_content_type(response, REST.type.TEXT_PLAIN);
    snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%lu", battery);

    REST.set_response_payload(response, (uint8_t *)buffer, strlen((char *)buffer));
  } else if(accept == REST.type.APPLICATION_JSON) {
    REST.set_header_content_type(response, REST.type.APPLICATION_JSON);
    snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "{'battery':%lu}", battery);

    REST.set_response_payload(response, buffer, strlen((char *)buffer));
  } else {
    REST.set_response_status(response, REST.status.NOT_ACCEPTABLE);
    const char *msg = "Supporting content-types text/plain and application/json";
    REST.set_response_payload(response, msg, strlen(msg));
  }
  REST.set_header_max_age(response, MAX_AGE);

  battery = reduceBattery(TRANSMITTING_DRAIN);
}


uint32_t thresholdLevel = 31;

static void
periodic_handler()
{

  //int battery = battery_sensor.value(0);
  battery = reduceBattery(SENSING_DRAIN);
  ++interval_counter;
  if(battery == 0){
    //char* data = "BATTERIA FINITA";
    //printf("%s\n", data);
    //process_post(&rest_server, BATTERY_END_EVENT, data);
    abort();
  }

  if(battery/10 <= thresholdLevel || interval_counter >= INTERVAL_MAX) {
     thresholdLevel /= 2;
     interval_counter = 0;
    /* Notify the registered observers which will trigger the res_get_handler to create the response. */
    REST.notify_subscribers(&res_battery);
  }
}