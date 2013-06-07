

#include <stdio.h>
#include <stdlib.h>
#include <sys/fcntl.h>
#include <unistd.h>
#include <stdint.h>


#include "led_utils.h"



unsigned char buf[60*40*3];


int main(void){

    struct ledPanel * panel1 = openConnection("/dev/ttyACM0", 300, 60);
  


    int i;
    for(i=0;i<2400;i++){
        int j;
        for(j=0;j<3;j++){
            setPixel(panel1, (i+j)%2400, 0x3F3F3F);
            setPixel(panel1, (i-1-j)%2400, 0x0000);
        }
        update(panel1); 
    }
    
    return 0;
}




