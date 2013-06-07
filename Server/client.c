
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <unistd.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/socket.h>

#include "led_utils.h"
#include "client.h"


struct client clients[MAX_CLIENTS];
struct client * activeClient;

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
    
    sem_init(&(cli->semaphore), 0, 0);

    cli->socket = sock;

    
    strcpy(cli->ip, ip);

    cli->name = 0;
    
    if (activeClient == 0){
        activeClient = cli;
    }
    
    
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
   

    while(42){
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
                cli->mode = recv_buffer[0]; //0: Background | 1: Foreground
                char *name = recv_buffer+1;
                cli->name = malloc(length);
                memcpy(cli->name, name, length-1);
                cli->name[length-1] = '\0';
                
                ////bei Login mit Info antworten 
                client_send_info(cli);
                
                break;
            case 'Y':   //Yield
                yield =1;
                break;
            case 'D':
                if(cli == activeClient){
                    client_handle_data(cli);
                }

                break;
            default:
                client_drop(cli);
                return 0;
        }
        
        //wenn name == 0 drop client
        if(cli->name == 0){
            client_drop(cli);
            return 0;
        } 
        
        //wenn yield erhalten oder ggf. timer abgelaufen
        if(yield == 1){     //TODO: Timer
            //deactivate client
            client_activate(cli, 0);

            //choose and activate next client / semaphore up
            client_choose_next(cli); 
            
        }
        
        if(cli != activeClient){
            //wait for reactivation / semaphore down
            sem_wait(&(cli->semaphore));
        }
        
        //send ready
        client_send(cli, 'R', 0, 0); 
    } 
    
}

void client_send(struct client * cli, char type, char *buf, int len){
    
    char buffer[len+5];
    
    buffer[0] = type;
    memcpy(buffer+1, &len, 4);
    memcpy(buffer+5, buf, len);


    int n = write(cli->socket, buffer, len+5);
    if (n<0) client_drop(cli);    
    
    
}

void client_drop(struct client * cli){
    
    close(cli->socket); //Stop both reception and transmission. 
    //free all resources
    //reset in array
    //ggf. choose new client
    
    sem_destroy(&(cli->semaphore));

}

void client_send_info(struct client * cli){
    
   unsigned int width = panel->width;
   unsigned int height = panel->stripLen * 8 / panel->width;

   char buffer[8];
   memcpy(buffer, &width ,4);
   memcpy(buffer+4, &height ,4);
   
   client_send(cli, 'I', buffer, 8);

}

void client_activate(struct client * cli, char active){
    client_send(cli, 'A', &active, 1); 
}

void client_choose_next(struct client * cli){
    
    int i;
    for(i=0;i<MAX_CLIENTS;i++){
        int id = (i+cli->id)%MAX_CLIENTS;
        if(clients[id].name == 0){
            client_activate(&(clients[id]), 1);
            activeClient = &(clients[id]);
            sem_post(&(clients[id].semaphore)); 
            break;
        }

    }
}

void client_handle_data(struct client * cli){

    int i;
    for(i=0; i<(panel->stripLen*8); i++){

        setPixel(panel, i, (recv_buffer[i*3] << 16) | (recv_buffer[i*3+1] << 8) | recv_buffer[i*3+2]);

    }
    
    update(panel);
}
