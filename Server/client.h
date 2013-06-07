


#define MAX_CLIENTS 100


struct client {
    int socket;
    int id;
    char ip[3*4+3+1];
    char mode;
    char * name;
    sem_t semaphore; 
};


void client_init(struct ledPanel * pan);
void client_add(int sock, char *ip);
void *client_thread(void *arg);
void client_send(struct client * cli, char type, char *buf, int len);
void client_drop(struct client * cli);
void client_activate(struct client * cli, char active);
void client_send_info(struct client * cli);
void client_choose_next(struct client * cli);
void client_handle_data(struct client * cli);
