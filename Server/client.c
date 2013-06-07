
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/socket.h>


#include "led_utils.h"
#include "client.h"


struct client clients[MAX_CLIENTS];

char *recv_buffer;
int recv_buffer_size;

struct ledPanel * panel;


void client_add(int sock, char *ip){

    //struct client * cli = malloc(sizeof(struct client)); 
    
    struct client * cli = 0;

    int i;
    for(i=0;i<MAX_CLIENTS;i++){
        if(clients[i].socket == 0){
            cli = &(clients[i]);
            cli->id = i;
            break;
        }
    }
    if (cli == 0){
        close(sock);
        return;
    }
    


    cli->socket = sock;

    
    strcpy(cli->ip, ip);

    cli->name = 0;
    
    
    
    
    //thread starten
    pthread_t thread;
    pthread_create(&thread, NULL, client_thread, cli);

    
    
    
}


void client_init(struct ledPanel * pan){
    panel = pan;
    recv_buffer_size = -1;
   
    int i;
    for(i=0;i<MAX_CLIENTS;i++){
        clients[i].socket = 0;
    }
}


void *client_thread(void *arg){
    
    struct client * cli = (struct client *)arg;
    int n;

    char type;
    unsigned int length;
    
    //1 char lesen, nachrichtentyp bestimmen
    n = read(cli->socket, &type, sizeof(char));
    //4 byte/uint32 lesen, laenge bestimmen
    n = read(cli->socket, &length, sizeof(int));
    
    if(length > recv_buffer_size){
        free(recv_buffer);
        recv_buffer = malloc(length);
        recv_buffer_size = length;
    }
    
    //'laenge' byte lesen
    n = read(cli->socket, recv_buffer, length);
    
    char yield = 0;

    //switch/case mit den nachrichten, daten uebergeben
    switch(type){
        case 'L':   //Login
            ////bei Login mit Info antworten 
            cli->mode = recv_buffer[0]; //0: Background | 1: Foreground
            char *name = recv_buffer+1;
            cli->name = malloc(length);
            memcpy(cli->name, name, length-1);
            cli->name[length-1] = '\0';
            
            //TODO: Send info 
            
            break;
        case 'Y':   //Yield
            yield =1;
            break;
        case 'D':
            
            //TODO: Write data into buffer, convert and display.

            break;
        default:
            //TODO: drop client
            break;
    }
    
    //wenn name == 0 drop client
    if(cli->name == 0){
        client_drop(cli);
        return 0;
    } 
    
    //wenn yield erhalten oder ggf. timer abgelaufen
    if(yield == 1){     //TODO: Timer
        //deactivate client
        //TODO: deactivate client

        //choose and activate next client / semaphore up
        //TODO: choose and activate next client
        

        //wait for reactivation / semaphore down
        //TODO: wait for reactivation / semaphore down
        
    }
    
    //send ready
    //TODO: send ready
    
    
    
}

void client_send(struct client * cli, char *buf, int len){
    
    int n = write(cli->socket, buf, len);
    if (n<0) client_drop(cli);    
    
    
}

void client_drop(struct client * cli){
    
    close(cli->socket); //Stop both reception and transmission. 
    //free all resources
    //reset in array
    //ggf. choose new client
    


}

void client_activate(struct client * cli, char active){
}

void client_choose_next(struct client * cli){
}

void client_handle_data(struct client * cli){
}
