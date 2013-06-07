


struct ledPanel {
    unsigned int stripLen;
    unsigned int width;
    int fd;
    int *buffer;
};


struct ledPanel * openConnection(char *device, unsigned int stripLen, unsigned int width);
void update(struct ledPanel * panel);
void setPixel(struct ledPanel * panel, uint32_t num, int color);




