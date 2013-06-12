
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <unistd.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "led_utils.h"
#include "client.h"


//#define DEBUG

struct client clients[MAX_CLIENTS];
struct client * activeClient;

char *recv_buffer;
int recv_buffer_size;

struct ledPanel * panel;



void client_add(int sock, char *ip){

    //struct client * cli = malloc(sizeof(struct client)); 
   
    printf("Adding client from %s\n", ip);
    fflush(stdout);

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
    
    //sem_init(&(cli->semaphore), 0, 0);

    cli->socket = sock;

    
    strcpy(cli->ip, ip);

    cli->name = 0;
    
    
    
#ifdef DEBUG
    printf("starting client thread\n");
    fflush(stdout);
#endif
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
    int length;
   

    while(42){
        fflush(stdout);
        
#ifdef DEBUG
        printf("%3d %20s | ............\n", cli->id, cli->name); 
#endif

        //1 char lesen, nachrichtentyp bestimmen
        n = read(cli->socket, &type, sizeof(char));
        if(n <0){
            client_drop(cli);
            return 0;
        }
        //4 byte/uint32 lesen, laenge bestimmen
        n = read(cli->socket, &length, sizeof(int));
        if(n <0){
            client_drop(cli);
            return 0;
        }
        
        length = ntohl(length);

        
        if(length > recv_buffer_size){
            if(recv_buffer != 0){
                free(recv_buffer);
            }
            recv_buffer = malloc(length);
            recv_buffer_size = length;
        }
        
        //'laenge' byte lesen
        int rb = 0;
        while(rb < length){
            n = read(cli->socket, recv_buffer+rb, length-rb);
            if(n <0){
                client_drop(cli);
                return 0;
            }
            rb += n;
        }

#ifdef DEBUG
        printf("%3d %20s | ^^^^^^^^^^^^\n", cli->id, cli->name); 
#endif

        char yield = 0;

        //switch/case mit den nachrichten, daten uebergeben
        switch(type){
            case 'L':   //Login
                cli->mode = recv_buffer[0]; //0: Background | 1: Foreground
                char *name = recv_buffer+1;
                cli->name = malloc(length);
                memcpy(cli->name, name, length-1);
                cli->name[length-1] = '\0';
                
                printf("%3d %20s | Registered with mode %d\n", cli->id, cli->name, cli->mode); 
                fflush(stdout);

                ////bei Login mit Info antworten 
                client_send_info(cli);
                
                //Falls noch kein aktiver client da ist, einen neuen aussuchen (diesen)
                if (activeClient == 0 || cli->mode == 1){
                    client_choose_next(cli);
                }
                
                break;
            case 'Y':   //Yield
#ifdef DEBUG
                printf("%3d %20s | Received Yield\n", cli->id, cli->name); 
                fflush(stdout);
#endif
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
        
        /*if(cli != activeClient){
            //wait for reactivation / semaphore down
            printf("%3d %20s | Waiting for Semaphore\n", cli->id, cli->name); 
            fflush(stdout);
            sem_wait(&(cli->semaphore));
            printf("%3d %20s | Done waiting for Semaphore\n", cli->id, cli->name); 
            fflush(stdout);
        }*/
        
        //send ready
        if(cli == activeClient){
#ifdef DEBUG
            printf("%3d %20s | Sending RDY\n", cli->id, cli->name); 
            fflush(stdout);
#endif
            client_send(cli, 'R', 0, 0);
        }
    } 
    
}

void client_send(struct client * cli, char type, char *buf, int len){
    //printf("Sending message type %c\n", type); 
    char buffer[len+5];
    
    int nlen = htonl(len);

    buffer[0] = type;
    memcpy(buffer+1, &nlen, 4);
    memcpy(buffer+5, buf, len);


    int n = write(cli->socket, buffer, len+5);
    if (n<0) client_drop(cli);    
    
    
}

void client_drop(struct client * cli){
    printf("%3d %20s | Dropping client\n", cli->id, cli->name); 
    fflush(stdout);
    
    if(cli->name != 0){
        free(cli->name);
        cli->name = 0;
    }
    
    if(cli == activeClient){
        client_choose_next(cli);
    }

    close(cli->socket); //Stop both reception and transmission. 
    cli->socket = 0;
    //free all resources
    //reset in array
    //ggf. choose new client
    
    //sem_destroy(&(cli->semaphore));
   
}

void client_send_info(struct client * cli){
    
    unsigned int width = htonl(panel->width);
    unsigned int height = htonl(panel->stripLen * 8 / panel->width);
    
    

    char buffer[8];
    memcpy(buffer, &width ,4);
    memcpy(buffer+4, &height ,4);
   
    client_send(cli, 'I', buffer, 8);

}

void client_activate(struct client * cli, char active){
    
    if(active == 1)
        printf("%3d %20s | Activating client\n", cli->id, cli->name); 
#ifdef DEBUG
    else
        printf("%3d %20s | Deactivating client\n", cli->id, cli->name); 
#endif
    
    fflush(stdout);
    
    client_send(cli, 'A', &active, 1); 
}

void client_choose_next(struct client * cli){
    
#ifdef DEBUG
    printf("%3d %20s | Choosing next client\n", cli->id, cli->name); 
    fflush(stdout);
#endif

    int newId = -1;

    
    if(cli != 0 && cli-> name != 0 && (activeClient == 0 || activeClient->name == 0 || (activeClient->mode == 0 && cli->mode == 1))){
        newId = cli->id;
    }
    else if(activeClient == 0 || activeClient->name == 0 || activeClient->mode == 0){
        int i;
        for(i=0;i<MAX_CLIENTS;i++){
            int id = (i+cli->id+1)%MAX_CLIENTS;
            if(clients[id].name != 0 && clients[id].mode == 1){
                newId = id;
                break;
            }
        }
        for(i=0;i<MAX_CLIENTS;i++){
            int id = (i+cli->id+1)%MAX_CLIENTS;
            if(clients[id].name != 0){
                newId = id;
                break;
            }
        }
    }
    activeClient = 0;
    if (newId >= 0 && !(activeClient->mode == 1 && cli->mode)){
        activeClient = &(clients[newId]);
        client_activate(&(clients[newId]), 1);

#ifdef DEBUG
        printf("%3d %20s | Sending RDY to %d\n", cli->id, cli->name, newId); 
        fflush(stdout);
#endif
        
        client_send(activeClient, 'R', 0, 0);
    }

}

void client_handle_data(struct client * cli){
#ifdef DEBUG
    printf("%3d %20s | Received Data from client\n", cli->id, cli->name); 
    fflush(stdout);
#endif
    int i;
    for(i=0; i<(panel->stripLen*8); i++){

        setPixel(panel, i, (recv_buffer[i*3] << 16) | (recv_buffer[i*3+1] << 8) | recv_buffer[i*3+2]);
    }
    
    update(panel);
}
