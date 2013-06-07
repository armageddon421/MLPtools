
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "client.h"


void client_add(int sock, char *ip){

    struct client * cli = malloc(sizeof(struct client)); 

    cli->socket = sock;

    char *ip2 = malloc(sizeof(ip));
    memcpy(ip2, ip, sizeof(ip));
    
    cli->ip = ip2;

    cli->name = 0;

    //thread starten
}


void client_thread(struct client * cli){
    
    //1 char lesen, nachrichtentyp bestimmen
    //4 byte/uint32 lesen, laenge bestimmen
    //
    //'laenge' byte lesen
    //switch/case mit den nachrichten, daten uebergeben
    ////bei Login mit Info antworten 
    //
    //
    //wenn name == 0 drop client
    //
    //
    //
    //
    //mutex 
    
    
    
    
}





