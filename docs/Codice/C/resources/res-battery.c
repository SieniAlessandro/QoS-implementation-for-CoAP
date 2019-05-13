#include "../common.h"
#include "dev/battery-sensor.h"

static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void periodic_handler(void);

int8_t sendedCritical = 0;

//Defining the battery resource as an observable one
PERIODIC_RESOURCE(res_battery,
         "title=\"Battery status\";rt=\"Battery\";obs",
         get_handler,
         NULL,
         NULL,
         NULL,
         BATTERY_SENSING_PERIOD*CLOCK_SECOND,
         periodic_handler);

//Used to see when the old packet is going to expire
static int32_t interval_counter = 0;

static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
  //USE THIS IN REAL SENSOR
  //int battery = battery_sensor.value(0);

  unsigned int accept = -1;
  coap_get_header_accept(request, &accept);

  //USED A SIMULATED VALUE FOR COOJA
  battery /= BATTERY_MULTIPLIER;

  if(accept == -1 || accept == REST.type.TEXT_PLAIN) {
    REST.set_header_content_type(response, REST.type.TEXT_PLAIN);
    snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%lu", battery);



    REST.set_response_payload(response, (uint8_t *)buffer, strlen((char *)buffer));
  } else {
    REST.set_response_status(response, REST.status.NOT_ACCEPTABLE);
    const char *msg = "Supporting content-types text/plain and application/json";
    REST.set_response_payload(response, msg, strlen(msg));
  }
  //We set a prefixed value for the max age
  REST.set_header_max_age(response, BATTERY_MAX_AGE);

  //Simulating the transimssion drain of the battery
  battery = reduceBattery(TRANSMITTING_DRAIN)/BATTERY_MULTIPLIER;
}

static void periodic_handler(){
  if(sendedCritical == 1)
    //if the sensor node has sent the critical battery level it must stop sensing the battery
    return;
  //USE THIS IN REAL SENSOR
  //int battery = battery_sensor.value(0);

  //SIMULATED BATTERY DRAIN FOR COOJA
  battery = reduceBattery(SENSING_DRAIN);
  interval_counter += BATTERY_SENSING_PERIOD;

  if(battery == 0){
    //IF THE BATTERY IS ENDED WE ABORT ALL, THE SENSOR NODE WILL NOT PERFORM ANY OTHER ACTION
    //abort();
    stampa(battery, "BATTERY FINISHED", 0);
  }

  //Check if the battery must be sent or not
  if((battery <= CRITICAL_BATTERY) || interval_counter+BATTERY_SENSING_PERIOD >= BATTERY_INTERVAL_MAX) {
    interval_counter = 0;
    sendedCritical = 1;
    /* Notify the registered observers which will trigger the res_get_handler to create the response. */
    REST.notify_subscribers(&res_battery);
  }
}