#include "common.h"
#include "dev/button-sensor.h"

/*Defining the resources that are present in the sensor node*/
#include "dev/temperature-sensor.h"
extern resource_t res_temperature;

#include "dev/battery-sensor.h"
extern resource_t res_battery;

//For debug purposes
extern resource_t res_hello;

static struct uip_udp_conn *client_conn;
static uip_ipaddr_t server_ipaddr;


/*---------------------------------------------------------------------------*/
//The rest server
PROCESS(rest_server, "Erbium Server");

AUTOSTART_PROCESSES(&rest_server);
/*---------------------------------------------------------------------------*/


static void set_global_address(void){
  uip_ipaddr_t ipaddr;

  uip_ip6addr(&ipaddr, UIP_DS6_DEFAULT_PREFIX, 0, 0, 0, 0, 0, 0, 0);
  uip_ds6_set_addr_iid(&ipaddr, &uip_lladdr);
  uip_ds6_addr_add(&ipaddr, 0, ADDR_AUTOCONF);

  uip_ip6addr(&server_ipaddr, UIP_DS6_DEFAULT_PREFIX, 0, 0, 0, 0x0250, 0xc2ff, 0xfea8, 0xcd1a); //redbee-econotag

}


PROCESS_THREAD(rest_server, ev, data){
  PROCESS_BEGIN();

  /*
   * Initializing IP address and connecting to the border router
   */

  PROCESS_PAUSE();

  set_global_address();

  /* new connection with remote host */
  client_conn = udp_new(NULL, UIP_HTONS(UDP_SERVER_PORT), NULL);
  if(client_conn == NULL) {
    PRINTF("No UDP connection available, exiting the process!\n");
    PROCESS_EXIT();
  }
  udp_bind(client_conn, UIP_HTONS(UDP_CLIENT_PORT));

  UIP_HTONS(client_conn->lport), UIP_HTONS(client_conn->rport));


  /*
   * Starting Erbium Server
   */

  PRINTF("Starting Erbium Server\n");

  /* Initialize the REST engine. */
  rest_init_engine();

  /* Activate the application-specific resources. */
  rest_activate_resource(&res_battery, "sensors/battery");
  SENSORS_ACTIVATE(battery_sensor);

  rest_activate_resource(&res_temperature, "sensors/temperature");
  SENSORS_ACTIVATE(temperature_sensor);

  //Resource used only for debug purposes
  rest_activate_resource(&res_hello, "sensors/hello");

  //Used only for Testing phase
  printf("Time,IPAddress,Value,Type,Critic,Observe\n");



  SENSORS_ACTIVATE(button_sensor);
  while(1) {
    PROCESS_WAIT_EVENT_UNTIL(ev==sensors_event && data==&button_sensor);
    //Used to force the battery to go in the only critic connection accepted phase
    critic_battery();
  }



  PROCESS_END();
}
