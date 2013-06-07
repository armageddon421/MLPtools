

#include <stdio.h>
#include <stdlib.h>
#include <sys/fcntl.h>
#include <unistd.h>
#include <stdint.h>
#include <string.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include "led_utils.h"
#include "client.h"


void error(const char *msg)
{
    perror(msg);
    exit(1);
}

int main(int argc, char *argv[]){
    
    int listenfd;
    
    struct sockaddr_in serv_addr, cli_addr;

    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) 
               error("ERROR opening socket");
    bzero((char *) &serv_addr, sizeof(serv_addr));    

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(1254);
    if (bind(listenfd, (struct sockaddr *) &serv_addr,
        sizeof(serv_addr)) < 0) 
        error("ERROR on binding");
    
    listen(listenfd,5); 
    socklen_t clilen = sizeof(cli_addr);


    //LOOP

    while(42){
        int newsockfd = accept(listenfd, (struct sockaddr *) &cli_addr, &clilen); 
        if (newsockfd < 0) 
            error("ERROR on accept");
        
        client_add(newsockfd, inet_ntoa(cli_addr.sin_addr));
    }

    return 0;
}













/*
int main(void){

    struct ledPanel * panel1 = openConnection("/dev/ttyACM0", 300, 60);
  


    int i;
    for(i=0;i<2400;i++){
        int j;
        for(j=0;j<3;j++){
            setPixel(panel1, (i+j)%2400,  0xFF9800);
            setPixel(panel1, (i-1-j)%2400,0x221000);
        }
        update(panel1); 
    }
    
    return 0;
}
*/



