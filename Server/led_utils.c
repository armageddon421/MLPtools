

#include <stdio.h>
#include <stdlib.h>
#include <sys/fcntl.h>
#include <unistd.h>
#include <stdint.h>

#include "led_utils.h"

struct ledPanel * openConnection(char *device, unsigned int stripLen, unsigned int width){
    
    struct ledPanel * panel = malloc(sizeof(struct ledPanel));
    

    panel->fd = open(device, O_RDWR | O_NOCTTY);
    panel->width = width;
    panel->stripLen = stripLen;
    panel->buffer = malloc(sizeof(int)*stripLen*6);


}

void update(struct ledPanel * panel){
    
    write(panel->fd, panel->buffer, panel->stripLen*24);
    
}





void setPixel(struct ledPanel * panel, uint32_t num, int color){
    if((num/60)%2 == 0){
        num = (num/60)*60+59-(num%60);
    }
   
   
    uint32_t strip, offset, mask;
    uint8_t bit, *p;
    
    color = ((color<<8)&0xFF0000) | ((color>>8)&0x00FF00) | (color&0x0000FF);
    strip = num / panel->stripLen;  // Cortex-M4 has 2 cycle unsigned divide :-)
    offset = num % panel->stripLen;
    bit = (1<<strip);
    p = ((uint8_t *)panel->buffer) + offset * 24;
    for (mask = (1<<23) ; mask ; mask >>= 1) {
        if (color & mask) {
            *p++ |= bit;
        } else {
            *p++ &= ~bit;
        }
    }
}




