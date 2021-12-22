import smbus
import sys
import paho.mqtt.client as mqtt
import paho.mqtt.publish as publish
import time
from pathlib import Path
import time,board,busio
import numpy as np
import adafruit_mlx90640
import matplotlib.pyplot as plt
from scipy import ndimage
import base64
import os

DEVICE_BUS = 1
DEVICE_ADDR = 0x17
TEMP_REG = 0x01
LIGHT_REG_L = 0x02
LIGHT_REG_H = 0x03
STATUS_REG = 0x04
ON_BOARD_TEMP_REG = 0x05
ON_BOARD_HUMIDITY_REG = 0x06
ON_BOARD_SENSOR_ERROR = 0x07
BMP280_TEMP_REG = 0x08
BMP280_PRESSURE_REG_L = 0x09
BMP280_PRESSURE_REG_M = 0x0A
BMP280_PRESSURE_REG_H = 0x0B
BMP280_STATUS = 0x0C
HUMAN_DETECT = 0x0D
MQTT_IP = "192.168.0.101"

i2c = busio.I2C(board.SCL, board.SDA, frequency=400000) # setup I2C
mlx = adafruit_mlx90640.MLX90640(i2c) # begin MLX90640 with I2C comm
mlx.refresh_rate = adafruit_mlx90640.RefreshRate.REFRESH_16_HZ # set refresh rate
mlx_shape = (24,32) # mlx90640 shape
mlx_interp_val = 10 # interpolate # on each dimension
mlx_interp_shape = (mlx_shape[0]*mlx_interp_val,
                    mlx_shape[1]*mlx_interp_val) # new shape
fig = plt.figure(figsize=(12,9)) # start figure
ax = fig.add_subplot(111) # add subplot
fig.subplots_adjust(0.05,0.05,0.95,0.95) # get rid of unnecessary padding
therm1 = ax.imshow(np.zeros(mlx_interp_shape),interpolation='none',
                cmap=plt.cm.bwr,vmin=25,vmax=45) # preemptive image
cbar = fig.colorbar(therm1) # setup colorbar

def readSensorData():
    sys.stdout = open('output.txt', 'w')
    bus = smbus.SMBus(DEVICE_BUS)
    aReceiveBuf = []
    aReceiveBuf.append(0x00)
    for i in range(TEMP_REG, HUMAN_DETECT + 1):
        aReceiveBuf.append(bus.read_byte_data(DEVICE_ADDR, i))
    if aReceiveBuf[STATUS_REG] & 0x01:
        print("Off-chip temperature sensor overrange!")
    elif aReceiveBuf[STATUS_REG] & 0x02:
        print("No external temperature sensor!")
    else:
        print("Current off-chip sensor temperature = %d Celsius" %
              aReceiveBuf[TEMP_REG])
    if aReceiveBuf[STATUS_REG] & 0x04:
        print("Onboard brightness sensor overrange!")
    elif aReceiveBuf[STATUS_REG] & 0x08:
        print("Onboard brightness sensor failure!")
    else:
        print("Current onboard sensor brightness = %d Lux" %
              (aReceiveBuf[LIGHT_REG_H] << 8 | aReceiveBuf[LIGHT_REG_L]))
    print("Current onboard sensor temperature = %d Celsius" %
          aReceiveBuf[ON_BOARD_TEMP_REG])
    print("Current onboard sensor humidity = %d %%" %
          aReceiveBuf[ON_BOARD_HUMIDITY_REG])
    if aReceiveBuf[ON_BOARD_SENSOR_ERROR] != 0:
        print("Onboard temperature and humidity sensor data may not be up to date!")
    if aReceiveBuf[BMP280_STATUS] == 0:
        print("Current barometer temperature = %d Celsius" %
              aReceiveBuf[BMP280_TEMP_REG])
        print("Current barometer pressure = %d pascal" % (
            aReceiveBuf[BMP280_PRESSURE_REG_L] | aReceiveBuf[BMP280_PRESSURE_REG_M] << 8 | aReceiveBuf[BMP280_PRESSURE_REG_H] << 16))
    else:
        print("Onboard barometer works abnormally!")
    if aReceiveBuf[HUMAN_DETECT] == 1:
        print("Live body detected within 5 seconds!")
    else:
        print("No humans detected!")
    sys.stdout = sys.__stdout__
	
# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")
	
# Publishes every 10 second the Data read from the sensors
def on_message(client, userdata, msg):
    readSensorData()
    readThermalData()
    txt = open('output.txt').read()
    with open("thermalcam.png", "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    publish.single("temperature", txt, hostname=MQTT_IP)
    publish.single("Schwarzi", encoded_string, hostname=MQTT_IP)
    time.sleep(10)
	
def readThermalData():
    cbar.set_label('Temperature [$^{\circ}$C]',fontsize=14) # colorbar label
    fig.canvas.draw() # draw figure to copy background
    ax_background = fig.canvas.copy_from_bbox(ax.bbox) # copy background
    fig.show() # show the figure before blitting
    frame = np.zeros(mlx_shape[0]*mlx_shape[1]) # 768 pts
    fig.canvas.restore_region(ax_background) # restore background
    mlx.getFrame(frame) # read mlx90640
    data_array = np.fliplr(np.reshape(frame,mlx_shape)) # reshape, flip data
    data_array = ndimage.zoom(data_array,mlx_interp_val) # interpolate
    therm1.set_array(data_array) # set data
    therm1.set_clim(vmin=np.min(data_array),vmax=np.max(data_array)) # set bounds
    cbar.update_normal(therm1) # update colorbar range
    ax.draw_artist(therm1) # draw new thermal image
    fig.canvas.blit(ax.bbox) # draw background
    #fig.canvas.flush_events() # show the new image
    fig.savefig('thermalcam.png', dpi=300, facecolor='#FCFCFC')
	
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect(MQTT_IP, 1883, 60)
client.loop_forever()