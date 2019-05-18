#include "../common.h"
#include "dev/temperature-sensor.h"

static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void periodic_handler(void);

//Used to handle the variable max_age
static uint32_t variable_max_age = RESOURCE_MAX_AGE;
//Used to know when we are near to the end of the validity of the previous data
static uint32_t interval_counter = 0;

//Vectors of temperature values, used to simulate the temperature
#define VALUES 6
//int TEMPERATURE_VALUES[VALUES] = {-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
/*int TEMPERATURE_VALUES[VALUES] = {10,   2,  -5, -11, -16, -19, -19, -18, -15, -10,  -3,   3,  11,
        19,  26,  32,  36,  39,  39,  38,  34,  29,  22,  14,   6,   0,
        -7, -13, -17, -19, -19, -17, -13,  -7,   0,   6,  14,  22,  29,
        34,  38,  39,  39,  36,  32,  26,  19,  11,   3,  -4, -10, -15,
       -18, -19, -19, -16, -11,  -5,   2,  10};
*/
int TEMPERATURE_VALUES[VALUES] = {40,   41,  42, 43, 42, 41};

uint32_t indexTemperatureValues = 1;

static int temperature_old = 10;
static uint32_t dataLevel; //NON_CRITICAL, CRITICAL
static uint8_t requestedLevel; //NON_CRITICAL all, CRITICAL only criticals

//Used to know if there is at least one subscriber to the resource
static uint8_t temperatureRequestedByObserver = 0;
static uint32_t temperatureObserver = 0;

//Initialization of the resource temperature as an observable resource, with a periodic handler function
PERIODIC_RESOURCE(res_temperature,
         "title=\"Temperature\";rt=\"Temperature\";obs",
         get_handler,
         NULL,
         NULL,
         NULL,
         RESOURCES_SENSING_PERIOD*CLOCK_SECOND,
         periodic_handler);

static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){

  uint32_t requestLevel;
  coap_get_header_observe(request, &requestLevel);

  //Only for the first request we check the observe field to see the type of messages requested
  if(requestLevel == 0 || requestLevel == CRITICAL){
    coap_set_header_observe(request, 0);
    if(requestLevel == CRITICAL){
      requestedLevel = 1;
    }else{
      requestedLevel = 0;
    }
    //We let the node to sense for the data, because there is at least one observer
    temperatureRequestedByObserver = 1;
    //Done to have the actual real value
    indexTemperatureValues = (indexTemperatureValues+1)%VALUES;
    temperature_old = TEMPERATURE_VALUES[indexTemperatureValues];
    //In this way we answer to the registration to all the observers -- REVIEW NEEDED
    if(temperature_old > TEMPERATURE_CRITICAL_THRESHOLD)
      dataLevel = CRITICAL;
    else
      dataLevel = NON_CRITICAL;
  }

  //If we receive a message with the field observer equal to 1, we know that the registration has been canceled
  if(requestLevel == 1){
      temperatureRequestedByObserver = 0;
      return;
  }
  unsigned int accept = -1;
  REST.get_header_accept(request, &accept);

  if(accept == -1 || accept == REST.type.TEXT_PLAIN) {
    REST.set_header_content_type(response, REST.type.TEXT_PLAIN);
    if(dataLevel == CRITICAL)
      snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%d!", temperature_old);
    else
      snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%d", temperature_old);

    REST.set_response_payload(response, (uint8_t *)buffer, strlen((char *)buffer));

  }else {

    REST.set_response_status(response, REST.status.NOT_ACCEPTABLE);
    const char *msg = "Supporting content-types text/plain";
    REST.set_response_payload(response, msg, strlen(msg));
  
  }

  //Change the default Max Age to the variable max age computed in the periodic handler
  REST.set_header_max_age(response, variable_max_age);
  //Reduce the battery to simulate the consumption of sending a message
  battery = reduceBattery(TRANSMITTING_DRAIN);

  //Call the log function - TESTING PHASE
  stampa(temperature_old, "temperature", dataLevel);

  if(requestLevel == 0 || requestLevel == CRITICAL){
    temperatureObserver = 0;
  }else{
    temperatureObserver++;
  }
  printf("%lu\n", temperatureObserver);
  
}


/*
 * Additionally, a handler function named [resource name]_handler must be implemented for each PERIODIC_RESOURCE.
 * It will be called by the REST manager process with the defined period.
 */
static void periodic_handler(){
  if(temperatureRequestedByObserver == 0 || battery <= 0){
    return;
  }

  //Formula to get the real temperature//
  // USE THIS FOR THE REAL SENSOR NODE//
  //int temperature = (temperature_sensor.value(0)/10-396)/10;
  //int temperature = temperature_sensor.value(0);


  // USED ONLY FOR THE SIMULATIONS ON COOJA //
  indexTemperatureValues = (indexTemperatureValues+1)%VALUES;
  int temperature = TEMPERATURE_VALUES[indexTemperatureValues%VALUES];

  interval_counter += RESOURCES_SENSING_PERIOD;
  //Used to simulate the drain of performing the sensing
  battery = reduceBattery(SENSING_DRAIN);

  //If the old data is not anymore valid
  if(interval_counter+RESOURCES_SENSING_PERIOD >= variable_max_age) {
      //Reset the counter
      interval_counter = 0;
      //Chek if the value is a critical one, without watching the old value
      if(temperature >= TEMPERATURE_CRITICAL_THRESHOLD)
        dataLevel = CRITICAL;
      else
        //If the value is not critical and the observer has requested all the values, we know that is a NON_CRITICAL value
        if(requestedLevel == 0)
          dataLevel = NON_CRITICAL;
        else
          //Otherwise we do not set any type of level and nothing will be send to the observer
          dataLevel = -1;
  }else{
    //The old packet is still valid, so we must see if the new value is different from the previous one
    if(temperature >= TEMPERATURE_CRITICAL_THRESHOLD && abs(temperature - temperature_old) >= TEMPERATURE_CRITICAL_CHANGE){
      dataLevel = CRITICAL;
    }else{
      if( requestedLevel == 0 && 
          abs(temperature - temperature_old) >= TEMPERATURE_NON_CRITICAL_CHANGE &&
          battery > 30){
            dataLevel = NON_CRITICAL;
      }else{
            dataLevel = -1;
      }
    }
  }

  //If there is a dataLevel it means that a new valid data has been sensed so it must be sent
  if(dataLevel != -1){
    //We put the recorded old value as the new one
    temperature_old = temperature;
    //We check if there are any spurios non critical data detected, that should not be sent, maybe because of the change of the
    //level of the battery
    if(requestedLevel == 1 && dataLevel == NON_CRITICAL){
      return;
    }

    //HANDLING THE MAX AGE
    if(dataLevel == CRITICAL){
      variable_max_age = CRITICAL_MAX_AGE;
    }else{
      if(dataLevel == NON_CRITICAL){
        if(variable_max_age == CRITICAL_MAX_AGE)
          variable_max_age = 10;
        else{
          variable_max_age += 10;
          if(variable_max_age > RESOURCE_MAX_AGE)
            variable_max_age = RESOURCE_MAX_AGE;
        }
      }
    }
    /* Notify the registered observers which will trigger the res_get_handler to create the response. */
    REST.notify_subscribers(&res_temperature);
  }
}