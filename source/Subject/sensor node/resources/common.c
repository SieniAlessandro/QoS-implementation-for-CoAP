/*common.c*/
#include "common.h"

uint32_t reduceBattery(uint32_t drain){
  battery = (battery != 0 && (battery - drain) < battery) ? battery-drain : 0;
  //printf("Actual Battery:%lu\n", battery);
  return battery;
}


static void
print_local_addresses(void)
{
  int i;
  uint8_t state;

  printf(",");
  for(i = 0; i < UIP_DS6_ADDR_NB/2; i++) {
    state = uip_ds6_if.addr_list[i].state;
    if(uip_ds6_if.addr_list[i].isused &&
       (state == ADDR_TENTATIVE || state == ADDR_PREFERRED)) {
      uip_debug_ipaddr_print(&uip_ds6_if.addr_list[i].ipaddr);
    }
  }
}

void stampa(int value, char* resourceName, uint32_t dataLevel){
  dataLevel = (dataLevel == 0) ? 0 : 1;
  print_local_addresses();
  printf(",%d,%s,%lu,", value, resourceName, dataLevel);// asctime(localtime(timestamp)));
}