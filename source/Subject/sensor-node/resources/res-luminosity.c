/*res-luminosity.c
 *Simulated resource
 *Here we do not use a threshold value but we watch only the variation with the previous sent value, if it is above a certain treshold then we can consider 
 *the new value as a non-critical or a critical value and send it
 **/
#include "../common.h"

#define LUMINOSITY_CRITICAL_CHANGE 20
#define LUMINOSITY_NON_CRITICAL_CHANGE 5


static void get_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void periodic_handler(void);

//Used to handle the variable max_age
static uint32_t variable_max_age = RESOURCE_MAX_AGE;
//Used to know when we are near to the end of the validity of the previous data
static uint32_t interval_counter = 0;

static uint32_t luminosityObserver = 0;
//Vectors of luminosity values, used to simulate the luminosity
#define VALUES 3
int LUMINOSITY_VALUES[VALUES] = {20, 60, 25};

uint32_t indexLuminosityValues = 1;

static int luminosity_old = 10;
static uint32_t dataLevel; //NON_CRITICAL, CRITICAL
static uint8_t requestedLevel; //NON_CRITICAL all, CRITICAL only criticals

//Used to know if there is at least one subscriber to the resource
static uint8_t luminosityRequestedByObserver = 0;

//Initialization of the resource luminosity as an observable resource, with a periodic handler function
PERIODIC_RESOURCE(res_luminosity,
         "title=\"Luminosity\";rt=\"Luminosity\";obs",
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
    luminosityRequestedByObserver = 1;
    //Done to have the actual real value
    indexLuminosityValues = (indexLuminosityValues+1)%VALUES;
    luminosity_old = LUMINOSITY_VALUES[indexLuminosityValues];
    //In this way we answer to the registration to all the observers -- REVIEW NEEDED
    dataLevel = NON_CRITICAL;
  }

  //If we receive a message with the field observer equal to 1, we know that the registration has been canceled
  if(requestLevel == 1){
      luminosityRequestedByObserver = 0;
      return;
  }
  unsigned int accept = -1;
  REST.get_header_accept(request, &accept);

  if(accept == -1 || accept == REST.type.TEXT_PLAIN) {
    REST.set_header_content_type(response, REST.type.TEXT_PLAIN);
    if(dataLevel == CRITICAL)
      snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%d!", luminosity_old);
    else
      snprintf((char *)buffer, REST_MAX_CHUNK_SIZE, "%d", luminosity_old);

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
  stampa(luminosity_old, "luminosity", dataLevel);

  
  if(requestLevel == 0 || requestLevel == CRITICAL){
    luminosityObserver = 0;
  }else{
    luminosityObserver++;
  }
  printf("%lu\n", luminosityObserver);

}


/*
 * Additionally, a handler function named [resource name]_handler must be implemented for each PERIODIC_RESOURCE.
 * It will be called by the REST manager process with the defined period.
 */
static void periodic_handler(){
  if(!luminosityRequestedByObserver || battery <= 0)
    return;

  // USED ONLY FOR THE SIMULATIONS ON COOJA //
  indexLuminosityValues = (indexLuminosityValues+1)%VALUES;
  int luminosity = LUMINOSITY_VALUES[indexLuminosityValues%VALUES];

  interval_counter += RESOURCES_SENSING_PERIOD;
  //Used to simulate the drain of performing the sensing
  battery = reduceBattery(SENSING_DRAIN);

  //If the old data is not anymore valid
  if(interval_counter+RESOURCES_SENSING_PERIOD >= variable_max_age) {
      //Reset the counter
      interval_counter = 0;
    //if the observer has requested all the values, we know that is a NON_CRITICAL value
    if(requestedLevel == 0)
      dataLevel = NON_CRITICAL;
    else
      //Otherwise we do not set any type of level and nothing will be send to the observer
      dataLevel = -1;
  }else{
    //The old packet is still valid, so we must see if the new value is different from the previous one
    if(abs(luminosity - luminosity_old) >= LUMINOSITY_CRITICAL_CHANGE){
      dataLevel = CRITICAL;
    }else{
      if( requestedLevel == 0 && 
          abs(luminosity - luminosity_old) >= LUMINOSITY_NON_CRITICAL_CHANGE &&
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
    luminosity_old = luminosity;
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
    REST.notify_subscribers(&res_luminosity);
  }
}