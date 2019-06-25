/*res-humidity.c
 *Simulated resource
 *The value is considered as a non-critical value if it stays inside a specific interval, it is sent as a non critical value if it differs from the previous one
 *of a specific quantity; when the value is outside the interval it is immediately considered as critical and sent to the proxy, it will be sent again as a critical
 *if it differs from the previous one of a specific quantity and it is still outside the interval
 **/
#include "../common.h"

#define HUMIDITY_CRITICAL_CHANGE 1
#define HUMIDITY_NON_CRITICAL_CHANGE 3
#define HUMIDITY_CRITICAL_MAX_THRESHOLD 70//% percentage value
#define HUMIDITY_CRITICAL_MIN_THRESHOLD 65//% percentage value


static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void periodic_handler(void);

//Used to handle the variable max_age
static uint32_t variable_max_age = RESOURCE_MAX_AGE;
//Used to know when we are near to the end of the validity of the previous data
static uint32_t interval_counter = 0;

//Vectors of HUMIDITY_NON_CRITICAL_CHANGE values, used to simulate the humidity
#define VALUES 3

int HUMIDITY_VALUES[VALUES] = {60, 74, 61};

uint32_t indexHumidityValues = 1;

static int humidity_old = 10;
static uint32_t dataLevel; //NON_CRITICAL, CRITICAL
static uint8_t requestedLevel; //NON_CRITICAL all, CRITICAL only criticals

//Used to know if there is at least one subscriber to the resource
static uint8_t humidityRequestedByObserver = 0;

static uint32_t humidityObserver = 0;
//Initialization of the resource humidity as an observable resource, with a periodic handler function
PERIODIC_RESOURCE(res_humidity,
         "title=\"Humidity\";rt=\"Humidity\";obs",
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
    humidityRequestedByObserver = 1;
    //Done to have the actual real value
    indexHumidityValues = (indexHumidityValues+1)%VALUES;
    humidity_old = HUMIDITY_VALUES[indexHumidityValues];
    //In this way we answer to the registration to all the observers -- REVIEW NEEDED
    dataLevel = NON_CRITICAL;
  }

  //If we receive a message with the field observer equal to 1, we know that the registration has been canceled
  if(requestLevel == 1){
      humidityRequestedByObserver = 0;
      return;
  }
  unsigned int accept = -1;
  REST.get_header_accept(request, &accept);

  if(accept == -1 || accept == REST.type.TEXT_PLAIN) {
    REST.set_header_content_type(response, REST.type.TEXT_PLAIN);
    if(dataLevel == CRITICAL)
      snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%d!", humidity_old);
    else
      snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%d", humidity_old);

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
  stampa(humidity_old, "humidity", dataLevel);

  if(requestLevel == 0 || requestLevel == CRITICAL){
    humidityObserver = 0;
  }else{
    humidityObserver++;
  }
  printf("%lu\n", humidityObserver);

}


/*
 * Additionally, a handler function named [resource name]_handler must be implemented for each PERIODIC_RESOURCE.
 * It will be called by the REST manager process with the defined period.
 */
static void periodic_handler(){
  if(!humidityRequestedByObserver || battery <= 0)
    return;

  // USED ONLY FOR THE SIMULATIONS ON COOJA //
  indexHumidityValues = (indexHumidityValues+1)%VALUES;
  int humidity = HUMIDITY_VALUES[indexHumidityValues%VALUES];

  interval_counter += RESOURCES_SENSING_PERIOD;
  //Used to simulate the drain of performing the sensing
  battery = reduceBattery(SENSING_DRAIN);

  //If the old data is not anymore valid
  if(interval_counter+RESOURCES_SENSING_PERIOD >= variable_max_age) {
      //Reset the counter
      interval_counter = 0;
      //Chek if the value is a critical one, without watching the old value
      if(humidity <= HUMIDITY_CRITICAL_MIN_THRESHOLD || humidity >= HUMIDITY_CRITICAL_MAX_THRESHOLD)
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
    if( ( humidity <= HUMIDITY_CRITICAL_MIN_THRESHOLD || humidity >= HUMIDITY_CRITICAL_MAX_THRESHOLD) 
    	&& (abs(humidity - humidity_old) >= HUMIDITY_CRITICAL_CHANGE)
    	){
      dataLevel = CRITICAL;
    }else{
      if( requestedLevel == 0 && 
          abs(humidity - humidity_old) >= HUMIDITY_NON_CRITICAL_CHANGE &&
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
    humidity_old = humidity;
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
    REST.notify_subscribers(&res_humidity);
  }
}